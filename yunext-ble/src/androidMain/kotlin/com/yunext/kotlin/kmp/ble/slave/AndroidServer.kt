package com.yunext.kotlin.kmp.ble.slave

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Looper
import com.yunext.kotlin.kmp.ble.core.BleConfig
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.asNativeBase
import com.yunext.kotlin.kmp.ble.core.asPlatformBase
import com.yunext.kotlin.kmp.ble.master.masterScope
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.display
import com.yunext.kotlin.kmp.ble.util.w
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

internal sealed interface ServerEvent {
    data class AddServiceSuccess(val services: List<BluetoothGattService>) : ServerEvent
    data object AddServiceFail : ServerEvent
    data object OnDisconnected : ServerEvent
    data class OnServiceAdded(val status: Int, val service: BluetoothGattService?) :
        ServerEvent

    data class OnConnected(
        val device: BluetoothDevice?,
        val status: Int,
        val newState: Int,
    ) : ServerEvent
}

internal sealed interface ConnectState {
    data object NaN : ConnectState {
        override fun toString(): String {
            return "NaN"
        }
    }

    data class Disconnected(val device: BluetoothDevice) : ConnectState {
        override fun toString(): String {
            return "Disconnected ${device.display}"
        }
    }

    data class Connected(
        val device: BluetoothDevice,
        val services: List<BluetoothGattService> = emptyList()
    ) : ConnectState {
        override fun toString(): String {
            return "Connected ${device.display} ${services.size}"
        }
    }
}


internal class AndroidPlatformServer(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val callback: (ServerEvent) -> Unit,
    private val eventCallback: (AndroidSlaveEvent) -> Unit,
) :
    PlatformServer {
    private val context: Context = context.applicationContext
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val _connectState: MutableStateFlow<ConnectState> = MutableStateFlow(ConnectState.NaN)
    val connectState: StateFlow<ConnectState> = _connectState.asStateFlow()
    private val handler = android.os.Handler(Looper.getMainLooper())

    private fun injectCallback(block: () -> Unit) {
        onSeverChanged()
        block()
    }

    private val bluetoothGattServerCallback: BluetoothGattServerCallback =
        object : BluetoothGattServerCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(
                device: BluetoothDevice?,
                status: Int,
                newState: Int,
            ) {
                super.onConnectionStateChange(device, status, newState)
                injectCallback {
                    d("[AndroidPlatformServer]BluetoothGattServerCallback onConnectionStateChange device=$device ,status=$status ,newState=$newState")
                    val currentDevice = device ?: return@injectCallback
                    handler.post {
                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                // 连接成功后，需要调用connect

                                bluetoothGattServer?.connect(currentDevice, false)
                                // 检查当前state
                                when (val s = connectState.value) {
                                    is ConnectState.Connected -> {
                                        if (s.device.address == currentDevice.address) {
                                            // ignore
                                        } else {
                                            bluetoothGattServer?.cancelConnection(s.device)
                                            _connectState.value =
                                                ConnectState.Connected(currentDevice)
                                        }
                                    }

                                    is ConnectState.Disconnected -> {
                                        if (s.device.address == currentDevice.address) {
                                            bluetoothGattServer?.cancelConnection(s.device)
                                            _connectState.value =
                                                ConnectState.Connected(currentDevice)
                                        } else {
                                            bluetoothGattServer?.cancelConnection(s.device)
                                            _connectState.value =
                                                ConnectState.Connected(currentDevice)
                                        }
                                    }

                                    ConnectState.NaN -> {
                                        _connectState.value = ConnectState.Connected(currentDevice)
                                    }
                                }
                            }

                            BluetoothProfile.STATE_DISCONNECTED -> {
                                // 检查当前state
                                when (val s = connectState.value) {
                                    is ConnectState.Connected -> {
                                        if (s.device.address == currentDevice.address) {
                                            bluetoothGattServer?.cancelConnection(s.device)
                                            _connectState.value =
                                                ConnectState.Disconnected(currentDevice)
                                        } else {
                                            bluetoothGattServer?.cancelConnection(currentDevice)
                                        }
                                    }

                                    is ConnectState.Disconnected -> {
                                        if (s.device.address == currentDevice.address) {
                                            bluetoothGattServer?.cancelConnection(s.device)
                                        } else {
                                            bluetoothGattServer?.cancelConnection(currentDevice)
                                        }
                                    }

                                    ConnectState.NaN -> {
                                        bluetoothGattServer?.cancelConnection(currentDevice)
                                    }
                                }
                            }

                            else -> {
                                w("newState is $newState")
                            }
                        }
                    }
                }
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onServiceAdded status=$status ,service=${service?.uuid} ${bluetoothGattServer?.services?.size}")

                injectCallback {
                    callback(ServerEvent.OnServiceAdded(status, service))
                }

            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?,
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                onSeverChanged()
                d("[AndroidPlatformServer]BluetoothGattServerCallback onCharacteristicReadRequest ${characteristic?.uuid} ")
                val d = device ?: return
                injectCallback {
                    eventCallback(
                        BleSlaveOnCharacteristicReadRequest(
                            d,
                            requestId,
                            offset,
                            characteristic
                        )
                    )
                }

            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?,
            ) {
                super.onCharacteristicWriteRequest(
                    device,
                    requestId,
                    characteristic,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value
                )

                d("[AndroidPlatformServer]BluetoothGattServerCallback onCharacteristicWriteRequest ${characteristic?.uuid}")
                val d = device ?: return
                injectCallback {
                    val r = BleSlaveOnCharacteristicWriteRequest(
                        d,
                        requestId,
                        characteristic,
                        preparedWrite,
                        responseNeeded,
                        offset,
                        value
                    )
                    eventCallback(r)
                }
            }

            override fun onDescriptorReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor?,
            ) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor)

                d("[AndroidPlatformServer]BluetoothGattServerCallback onDescriptorReadRequest ${descriptor?.uuid}")
                val d = device ?: return
                injectCallback {
                    val r = BleSlaveOnDescriptorReadRequest(d, requestId, offset, descriptor)
                    eventCallback(r)
                }
            }

            override fun onDescriptorWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                descriptor: BluetoothGattDescriptor?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?,
            ) {
                super.onDescriptorWriteRequest(
                    device,
                    requestId,
                    descriptor,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value
                )
                d("[AndroidPlatformServer]BluetoothGattServerCallback onDescriptorWriteRequest ${descriptor?.uuid} ")

                val d = device ?: return

//                bluetoothGattServer?.services?.let { serviceList ->
//                    w("刷新测试：\n${serviceList.display}")
//                }

                injectCallback {
                    val r = BleSlaveOnDescriptorWriteRequest(
                        d,
                        requestId,
                        descriptor,
                        preparedWrite,
                        responseNeeded,
                        offset,
                        value
                    )
                    eventCallback(r)
                }
            }

            override fun onExecuteWrite(
                device: BluetoothDevice?,
                requestId: Int,
                execute: Boolean,
            ) {
                super.onExecuteWrite(device, requestId, execute)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onExecuteWrite ")

                val d = device ?: return
                injectCallback {
                    val r = BleSlaveOnExecuteWrite(d, requestId, execute)
                    eventCallback(r)
                }
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                super.onNotificationSent(device, status)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onNotificationSent ")

                val d = device ?: return

                injectCallback {
                    val r = BleSlaveOnNotificationSent(d, status)
                    eventCallback(r)
                }

            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                super.onMtuChanged(device, mtu)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onMtuChanged $mtu")
                val d = device ?: return
                injectCallback {
                    val r = BleSlaveOnMtuChanged(d, mtu)
                    eventCallback(r)
                }
            }

            override fun onPhyUpdate(
                device: BluetoothDevice?,
                txPhy: Int,
                rxPhy: Int,
                status: Int,
            ) {
                super.onPhyUpdate(device, txPhy, rxPhy, status)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onPhyUpdate ")
                val d = device ?: return
                injectCallback {
                    val r = BleSlaveOnPhyUpdate(d, txPhy, rxPhy, status)
                    eventCallback(r)
                }
            }

            override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
                super.onPhyRead(device, txPhy, rxPhy, status)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onPhyRead ")
                val d = device ?: return
                injectCallback {
                    val r = BleSlaveOnPhyRead(d, txPhy, rxPhy, status)
                    eventCallback(r)
                }
            }
        }

    private fun onSeverChanged() {
        bluetoothGattServer?.let { server ->
            when (val old = connectState.value) {
                is ConnectState.Connected -> {
                    _connectState.value = old.copy(services = server.services)

                }

                is ConnectState.Disconnected -> {

                }

                ConnectState.NaN -> {}
            }
        }

    }

    private var addServiceInStartJob: Job? = null

    @SuppressLint("MissingPermission")
    fun start(services: Array<PlatformBluetoothGattService>) {

        d("[AndroidPlatformServer]addService services:${services.size}")
        println("******************** a")
        services.forEach {
            println(it.display)
        }
        println("******************** b")
        addServiceInStartJob?.cancel()
        bluetoothGattServer?.cancel()
        bluetoothGattServer = null
        val bluetoothGattServices = services.map { it.asNativeBase() }
        println("-------------------- c")
        bluetoothGattServices.forEach {
            println(it.display)
        }
        println("-------------------- d")
        addServiceInStartJob = coroutineScope.launch {
            delay(BleConfig.OPT_INTERVAL_TIMESTAMP)
        val curBluetoothGattServer =
            bluetoothManager.openGattServer(context, bluetoothGattServerCallback)
                ?: throw IllegalStateException("openGattServer result null")
        var success = true

            bluetoothGattServices.forEach {
                ensureActive()
                d("====add before==== ${curBluetoothGattServer.services.size}")

                val addService = curBluetoothGattServer.addService(it)
                delay(BleConfig.OPT_INTERVAL_TIMESTAMP)
                if (!addService) {
                    success = false
                    return@forEach
                }
            }


            if (success) {

                bluetoothGattServer = curBluetoothGattServer
                callback(ServerEvent.AddServiceSuccess(curBluetoothGattServer.services))
                d("====add after==== ${curBluetoothGattServer.services.size}")

//                delay(3000)
//                bluetoothGattServices.forEach {
//                    it.characteristics.forEach {
//                        ch->
//                        ch.descriptors.forEach {
//                            de->
//                            de.value = byteArrayOf(0x01)
//                        }
//                        write(ch, byteArrayOf(),false)
//                    }
//                }
            } else {
                curBluetoothGattServer.cancel()
                callback(ServerEvent.AddServiceFail)
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun cancelConnectedDevice() {
        try {
            when (val s = connectState.value) {
                is ConnectState.Connected -> {
                    bluetoothGattServer?.cancelConnection(s.device)
                }

                is ConnectState.Disconnected -> {
                    bluetoothGattServer?.cancelConnection(s.device)
                }

                ConnectState.NaN -> {

                }
            }
        } catch (e: Exception) {
            w("cancelDevice error = $e")
        } finally {
            _connectState.value = ConnectState.NaN
        }
    }


    fun stop() {
        d("[AndroidPlatformServer]close")
        cancelConnectedDevice()
        addServiceInStartJob?.cancel()
        bluetoothGattServer?.cancel()
        bluetoothGattServer = null
    }

    @SuppressLint("MissingPermission")
    fun response(
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?,
    ): Boolean {
        val device = when (val s = connectState.value) {
            is ConnectState.Connected -> s.device
            is ConnectState.Disconnected -> return false
            ConnectState.NaN -> return false
        }
        return bluetoothGattServer?.let { server ->
            server.sendResponse(device, requestId, status, offset, value)
        } ?: false
    }


    @SuppressLint("MissingPermission")
    fun write(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        confirm: Boolean
    ): Boolean {
        @OptIn(ExperimentalStdlibApi::class)
        d("[AndroidPlatformServer]write ${characteristic.uuid} ${value.toHexString()} $confirm")
        val device = when (val s = connectState.value) {
            is ConnectState.Connected -> s.device
            is ConnectState.Disconnected -> return false
            ConnectState.NaN -> return false
        }

        return bluetoothGattServer?.let { server ->
            val uuid = characteristic.uuid
            var ch: BluetoothGattCharacteristic? = null
            server.services.forEach { s ->
                s.characteristics.forEach { c ->
                    if (c.uuid.toString() == uuid.toString()) {
                        ch = c
                    }
                }
            }
            val final = ch
            if (final == null) {
                w("characteristic 不存在")
                return false
            }
            final.value = value
            server.notifyCharacteristicChanged(device, final, confirm)
        } ?: false
    }

    private fun doTask(task: () -> Unit) {
        masterScope.launch() {
            task()
        }
    }
}

@SuppressLint("MissingPermission")
private fun BluetoothGattServer.cancel() {
    d("BluetoothGattServer::cancel")
    try {
        this.clearServices()
        this.close()
    } catch (e: Exception) {
        w("BluetoothGattServer::cancel error:$e")
    }
}