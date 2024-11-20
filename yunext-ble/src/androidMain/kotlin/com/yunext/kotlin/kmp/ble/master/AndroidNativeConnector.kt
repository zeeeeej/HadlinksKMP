package com.yunext.kotlin.kmp.ble.master

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.clj.fastble.BleManager
import com.yunext.kotlin.kmp.ble.core.BluetoothException
import com.yunext.kotlin.kmp.ble.core.NotifyDescriptorUUID
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.asPlatformBase
import com.yunext.kotlin.kmp.ble.core.display
import com.yunext.kotlin.kmp.ble.core.findCharacteristic
import com.yunext.kotlin.kmp.ble.core.findService
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryOwner
import com.yunext.kotlin.kmp.ble.history.bleIn
import com.yunext.kotlin.kmp.ble.history.bleOut
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.display
import com.yunext.kotlin.kmp.ble.util.findCharacteristic
import com.yunext.kotlin.kmp.ble.util.findService
import com.yunext.kotlin.kmp.ble.util.refreshDeviceCache
import com.yunext.kotlin.kmp.ble.util.w
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi

internal class AndroidNativeConnector(
    ctx: Context, private val device: BluetoothDevice,
    historyOwner: BluetoothHistoryOwner,
) :
    PlatformConnector, BluetoothHistoryOwner by historyOwner {
    private val context: Context = ctx.applicationContext
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter

    private val _status: MutableStateFlow<PlatformConnectorStatus> = MutableStateFlow(
        PlatformConnectorStatus.Idle(device.asPlatformBase())
    )
    override val status: StateFlow<PlatformConnectorStatus> = _status.asStateFlow()

    private var curGatt: BluetoothGatt? = null

    private var connectJob: Job? = null

    private fun doTask(task: () -> Unit) {
        masterScope.launch() {
            task()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @SuppressLint("MissingPermission")
    override fun connect() {
        d("connect")
        clearJobs()
        connectJob = masterScope.launch {
            when (status.value) {
                is PlatformConnectorStatus.Idle -> {}
                is PlatformConnectorStatus.Connected -> {
                    w("已经连接")
                    return@launch
                }

                is PlatformConnectorStatus.Connecting -> {
                    w("连接中...")
                    return@launch
                }

                is PlatformConnectorStatus.Disconnected -> {}
                is PlatformConnectorStatus.Disconnecting -> {}
                is PlatformConnectorStatus.ServiceDiscovered -> {
                    w("已经发现服务")
                    return@launch
                }
            }
            curGatt?.close()
            curGatt?.refreshDeviceCache()
            curGatt = null
            delay(1000)
            curGatt = device.connectGatt(context, false,
                object : BluetoothGattCallback() {
                    val tag = "[GattCallback]"
                    override fun onPhyUpdate(
                        gatt: BluetoothGatt?,
                        txPhy: Int,
                        rxPhy: Int,
                        status: Int
                    ) {
                        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
                        doTask {
                            d("${tag}onPhyUpdate gatt=${gatt} txPhy=$txPhy rxPhy=$rxPhy status=$status")
                            bleIn("${tag}onPhyUpdate gatt=${gatt} txPhy=$txPhy rxPhy=$rxPhy status=$status")
                        }
                    }

                    override fun onPhyRead(
                        gatt: BluetoothGatt?,
                        txPhy: Int,
                        rxPhy: Int,
                        status: Int
                    ) {
                        super.onPhyRead(gatt, txPhy, rxPhy, status)
                        doTask {
                            d("${tag}onPhyRead gatt=${gatt} txPhy=$txPhy rxPhy=$rxPhy status=$status")
                            bleIn("${tag}onPhyRead gatt=${gatt} txPhy=$txPhy rxPhy=$rxPhy status=$status")

                        }
                    }

                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt?,
                        status: Int,
                        newState: Int
                    ) {
                        super.onConnectionStateChange(gatt, status, newState)
                        doTask {
                            d("${tag}onConnectionStateChange gatt=${gatt} newState=$newState status=$status")
                            bleIn("${tag}onConnectionStateChange gatt=${gatt} newState=$newState status=$status")
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                when (newState) {
                                    BluetoothProfile.STATE_CONNECTED -> {
                                        discoverServices(gatt)
                                        _status.value =
                                            PlatformConnectorStatus.Connected(device = device.asPlatformBase())
                                    }

                                    BluetoothProfile.STATE_DISCONNECTED -> {
                                        gatt?.close()
//                                gatt?.refreshDeviceCache()
                                        _status.value =
                                            PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                    }


                                }
                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }


                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        super.onServicesDiscovered(gatt, status)
                        doTask {
                            d("${tag}onServicesDiscovered gatt=${gatt} status=$status")
                            bleIn("${tag}onServicesDiscovered gatt=${gatt} status=$status")
                            gatt ?: return@doTask
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                onServicesChangedInternal(gatt)

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt.close()
                            }
                        }

                    }

                    override fun onCharacteristicRead(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic?,
                        status: Int
                    ) {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        doTask {
                            d("${tag}onCharacteristicRead gatt=${gatt} characteristic=$characteristic status=$status")
                            bleIn("${tag}onCharacteristicRead gatt=${gatt} characteristic=$characteristic status=$status")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onCharacteristicRead(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray,
                        status: Int
                    ) {
                        super.onCharacteristicRead(gatt, characteristic, value, status)
                        doTask {
                            @OptIn(ExperimentalStdlibApi::class)
                            d("${tag}onCharacteristicRead gatt=${gatt} characteristic=$characteristic value=${value.toHexString()} status=$status")
                            @OptIn(ExperimentalStdlibApi::class)
                            bleIn("${tag}onCharacteristicRead gatt=${gatt} characteristic=$characteristic value=${value.toHexString()} status=$status")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onCharacteristicWrite(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic?,
                        status: Int
                    ) {
                        super.onCharacteristicWrite(gatt, characteristic, status)
                        doTask {
                            d("${tag}onCharacteristicWrite gatt=${gatt} characteristic=$characteristic status=$status")
                            bleIn("${tag}onCharacteristicWrite gatt=${gatt} characteristic=$characteristic <${characteristic?.value?.toHexString()}> status=$status")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic?
                    ) {
                        super.onCharacteristicChanged(gatt, characteristic)
                        doTask {
                            @OptIn(ExperimentalStdlibApi::class)
                            d("${tag}onCharacteristicChanged gatt=${gatt} characteristic=${characteristic?.value?.toHexString()}")
                            @OptIn(ExperimentalStdlibApi::class)
                            bleIn("${tag}onCharacteristicChanged gatt=${gatt} characteristic=${characteristic?.value?.toHexString()}")
                        }

                    }

                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray
                    ) {
                        super.onCharacteristicChanged(gatt, characteristic, value)
                        doTask {
                            @OptIn(ExperimentalStdlibApi::class)
                            d("${tag}onCharacteristicChanged gatt=${gatt} characteristic=$characteristic value=${value.toHexString()}")
                            @OptIn(ExperimentalStdlibApi::class)
                            bleIn("${tag}onCharacteristicChanged gatt=${gatt} characteristic=$characteristic value=${value.toHexString()}")
                        }
                    }

                    override fun onDescriptorRead(
                        gatt: BluetoothGatt?,
                        descriptor: BluetoothGattDescriptor?,
                        status: Int
                    ) {
                        super.onDescriptorRead(gatt, descriptor, status)
                        doTask {
                            d("${tag}onDescriptorRead gatt=${gatt} descriptor=$descriptor status=${status}")
                            bleIn("${tag}onDescriptorRead gatt=${gatt} descriptor=$descriptor status=${status}")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onDescriptorRead(
                        gatt: BluetoothGatt,
                        descriptor: BluetoothGattDescriptor,
                        status: Int,
                        value: ByteArray
                    ) {
                        super.onDescriptorRead(gatt, descriptor, status, value)
                        doTask {
                            @OptIn(ExperimentalStdlibApi::class)
                            d("${tag}onDescriptorRead gatt=${gatt} descriptor=$descriptor status=${status} value=${value.toHexString()}")
                            @OptIn(ExperimentalStdlibApi::class)
                            bleIn("${tag}onDescriptorRead gatt=${gatt} descriptor=$descriptor status=${status} value=${value.toHexString()}")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }

                    }

                    override fun onDescriptorWrite(
                        gatt: BluetoothGatt?,
                        descriptor: BluetoothGattDescriptor?,
                        status: Int
                    ) {
                        super.onDescriptorWrite(gatt, descriptor, status)
                        doTask {
                            @OptIn(ExperimentalStdlibApi::class)
                            d("${tag}onDescriptorWrite gatt=${gatt} descriptor=$descriptor status=${status} value=${descriptor?.value?.toHexString()}")
                            @OptIn(ExperimentalStdlibApi::class)
                            bleIn("${tag}onDescriptorWrite gatt=${gatt} descriptor=$descriptor status=${status} value=${descriptor?.value?.toHexString()}")
                            gatt ?: return@doTask
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                onServicesChangedInternal(gatt)

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
                        super.onReliableWriteCompleted(gatt, status)
                        doTask {
                            d("${tag}onReliableWriteCompleted gatt=${gatt}  status=${status}")
                            bleIn("${tag}onReliableWriteCompleted gatt=${gatt}  status=${status}")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                        super.onReadRemoteRssi(gatt, rssi, status)
                        doTask {
                            d("${tag}onReadRemoteRssi gatt=${gatt}  rssi=${rssi} status=${status}")
                            bleIn("${tag}onReadRemoteRssi gatt=${gatt}  rssi=${rssi} status=${status}")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                        super.onMtuChanged(gatt, mtu, status)
                        doTask {
                            d("${tag}onReadRemoteRssi gatt=${gatt}  mtu=${mtu} status=${status}")
                            bleIn("${tag}onReadRemoteRssi gatt=${gatt}  mtu=${mtu} status=${status}")
                            if (status == BluetoothGatt.GATT_SUCCESS) {

                            } else {
                                _status.value =
                                    PlatformConnectorStatus.Disconnected(device = device.asPlatformBase())
                                gatt?.close()
                            }
                        }
                    }

                    override fun onServiceChanged(gatt: BluetoothGatt) {
                        super.onServiceChanged(gatt)
                        doTask {
                            d("${tag}onReadRemoteRssi gatt=${gatt}")
                            bleIn("${tag}onReadRemoteRssi gatt=${gatt}")
                            onServicesChangedInternal(gatt)
                        }
                    }
                })
            onStatusChanged(PlatformConnectorStatus.Connecting(device.asPlatformBase()))
            d("connect curGatt=$curGatt")
        }
    }

    private fun onServicesChangedInternal(gatt: BluetoothGatt) {
        // startDebug(gatt.services)
        println("gatt.services.display :${gatt.services.display}")
        _status.value =
            PlatformConnectorStatus.ServiceDiscovered(
                device = device.asPlatformBase(),
                gatt.services.map {
                    it.asPlatformBase()
                })
    }

    private var discoverServicesJob: Job? = null

    @SuppressLint("MissingPermission")
    private fun discoverServices(gatt: BluetoothGatt?) {
        discoverServicesJob?.cancel()
        discoverServicesJob = masterScope.launch {
            delay(500)
            gatt?.discoverServices()
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        d("disconnect")
        clearJobs()
        onStatusChanged(PlatformConnectorStatus.Disconnecting(device = device.asPlatformBase()))
//        curGatt?.disconnect()
        curGatt?.close()
        curGatt?.refreshDeviceCache()
        curGatt = null
        onStatusChanged(PlatformConnectorStatus.Disconnected(device = device.asPlatformBase()))
    }

    @SuppressLint("MissingPermission")
    override fun enableNotify(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean,
        useCharacteristicDescriptor: Boolean
    ): Boolean {
        @OptIn(ExperimentalUuidApi::class)
        bleOut(
            "${device.display} ${service.uuid.toString()}/${characteristic.uuid.toHexString()} enable:${enable}",
            tag = "enableNotify"
        )
        return setCharacteristicNotificationInternal(
            service,
            characteristic,
            enable,
            false,
            useCharacteristicDescriptor
        )
    }

    @SuppressLint("MissingPermission")
    private fun setCharacteristicNotificationInternal(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean,
        indicate: Boolean = false,
        useCharacteristicDescriptor: Boolean = false
    ): Boolean {
        @OptIn(ExperimentalUuidApi::class)
        val serviceUUID = service.uuid.toString()

        @OptIn(ExperimentalUuidApi::class)
        val chUUID = characteristic.uuid.toString()
        d("setCharacteristicNotificationInternal $enable $serviceUUID/$chUUID")
        return curGatt?.let { gatt ->
            val status = status.value
            if (status !is PlatformConnectorStatus.ServiceDiscovered) return false
//            val services = status.services
//            val findService = services.findService(serviceUUID) ?: run {
//                w("setting 没有此Service")
//                return false
//            }
//            findService.findCharacteristic(chUUID) ?: run {
//                w("setting 没有此characteristic")
//                return false
//            }
            val gattService = gatt.findService(serviceUUID) ?: run {
                w("gatt 没有此Service")
                return false
            }

            val gattCharacteristic =
                gattService.findCharacteristic(chUUID) ?: run {
                    w("gatt 没有此characteristic")
                    return false
                }

            if (characteristic.properties.contains(
                    if (indicate)
                        PlatformBluetoothGattCharacteristic.Property.Indicate
                    else
                        PlatformBluetoothGattCharacteristic.Property.Notify
                )
            ) {
                val r = gatt.setCharacteristicNotification(gattCharacteristic, enable)
                d(" ->setCharacteristicNotificationInternal setCharacteristicNotification result:$r")
                val descriptor: BluetoothGattDescriptor? = if (useCharacteristicDescriptor) {
                    gattCharacteristic.getDescriptor(gattCharacteristic.uuid)
                } else {
                    gattCharacteristic.getDescriptor(UUID.fromString(NotifyDescriptorUUID))
                }
                @OptIn(ExperimentalStdlibApi::class)
                d(" ->setCharacteristicNotificationInternal descriptor value:${descriptor?.value?.toHexString()}")
                val descriptorNotNull = descriptor ?: return false
                descriptorNotNull.setValue(
                    if (enable) {
                        if (indicate) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    } else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                )
                gatt.writeDescriptor(descriptorNotNull).also {
                    d(" ->setCharacteristicNotificationInternal writeDescriptor result:$it")
                }
            } else run {
                w("setCharacteristicNotificationInternal 不支持 Property")
                false
            }//throw BluetoothException("不支持Notify Property")
        } ?: false
    }

    @SuppressLint("MissingPermission")
    override fun enableIndicate(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean,
        useCharacteristicDescriptor: Boolean
    ): Boolean {
        @OptIn(ExperimentalUuidApi::class)
        bleOut(
            "${device.display} ${service.uuid.toString()}/${characteristic.uuid.toHexString()} enable:${enable}",
            tag = "enableIndicate"
        )
        return setCharacteristicNotificationInternal(
            service,
            characteristic,
            enable,
            true,
            useCharacteristicDescriptor
        )
    }

    override fun close() {
        d("close")
        disconnect()
    }


    @OptIn(ExperimentalStdlibApi::class)
    @SuppressLint("MissingPermission")
    override fun write(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {

        @OptIn(ExperimentalUuidApi::class)
        val serviceUUID = service.uuid.toString()


        @OptIn(ExperimentalUuidApi::class)

        val chUUID = characteristic.uuid.toString()
        @OptIn(ExperimentalUuidApi::class)
        bleOut(
            "${device.display} ${service.uuid.toString()}/${characteristic.uuid.toHexString()} value:<${data.toHexString()}>",
            tag = "write"
        )
        @OptIn(ExperimentalStdlibApi::class)
        val str = data.toHexString()
        d("write $str $serviceUUID/$chUUID")
        return curGatt?.let { gatt ->
            val status = status.value
            if (status !is PlatformConnectorStatus.ServiceDiscovered) return false
            val gattService = gatt.findService(serviceUUID) ?: run {
                w("gatt 没有此Service")
                return false
            }

            val gattCharacteristic =
                gattService.findCharacteristic(chUUID) ?: run {
                    w("gatt 没有此characteristic")
                    return false
                }

            if (characteristic.properties.contains(
                    PlatformBluetoothGattCharacteristic.Property.Write
                ) || characteristic.properties.contains(
                    PlatformBluetoothGattCharacteristic.Property.WriteNoResponse
                )
            ) {
                gattCharacteristic.setValue(data)
                gatt.writeCharacteristic(gattCharacteristic)
            } else run {
                w("不支持 Property")
                false
            }
        } ?: false
    }

    @SuppressLint("MissingPermission")
    override fun read(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic
    ): Boolean {
        @OptIn(ExperimentalUuidApi::class)
        val serviceUUID = service.uuid.toString()

        @OptIn(ExperimentalUuidApi::class)

        val chUUID = characteristic.uuid.toString()
        @OptIn(ExperimentalUuidApi::class)
        bleOut(
            "${device.display} ${service.uuid.toString()}/${characteristic.uuid.toHexString()}",
            tag = "read"
        )
        d("read $serviceUUID/$chUUID")
        return curGatt?.let { gatt ->
            val status = status.value
            if (status !is PlatformConnectorStatus.ServiceDiscovered) return false
            val gattService = gatt.findService(serviceUUID) ?: run {
                w("gatt 没有此Service")
                return false
            }

            val gattCharacteristic =
                gattService.findCharacteristic(chUUID) ?: run {
                    w("gatt 没有此characteristic")
                    return false
                }

            if (characteristic.properties.contains(
                    PlatformBluetoothGattCharacteristic.Property.Read
                )
            ) {
                gatt.readCharacteristic(gattCharacteristic)
            } else run {
                w("不支持 Property")
                false
            }
        } ?: false
    }

    private fun clearJobs() {
        connectJob?.cancel()
        discoverServicesJob?.cancel()
        connectJob = null
        discoverServicesJob = null
        startDebugJob?.cancel()
        startDebugJob = null
    }

    private fun onStatusChanged(status: PlatformConnectorStatus) {
        _status.value = status
    }

    private var startDebugJob: Job? = null
    private fun startDebug(
        services: List<BluetoothGattService>
    ) {
        startDebugJob?.cancel()
        startDebugJob = null
        startDebugJob = masterScope.launch {
            if (services.isNotEmpty()) {
                var find: Pair<PlatformBluetoothGattService, PlatformBluetoothGattCharacteristic>? =
                    null
                services.map { it.asPlatformBase() }.forEach { service ->
                    service.characteristics.forEach { ch ->
                        if (ch.properties.contains(PlatformBluetoothGattCharacteristic.Property.Write) ||
                            (ch.properties.contains(PlatformBluetoothGattCharacteristic.Property.WriteNoResponse))
                        ) {
                            find = service to ch
                        }
                    }
                }
                val (s, c) = find ?: return@launch
                while (isActive) {
                    delay(3000)
                    write(service = s, characteristic = c, Random.Default.nextBytes(4))
                }
            }
        }
    }


}