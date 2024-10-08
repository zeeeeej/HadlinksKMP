package com.yunext.kotlin.kmp.ble.slave

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import com.yunext.kotlin.kmp.ble.core.BleConfig
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.asNativeBase
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.w
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

internal sealed interface ServerEvent {
    data object AddServiceSuccess : ServerEvent
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

internal class AndroidPlatformServer(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val callback: (ServerEvent) -> Unit,
    private val eventCallback: (AndroidSlaveEvent) -> Unit,
) :
    PlatformBroadcaster {
    private val context: Context = context.applicationContext
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var connectedDevice: BluetoothDevice? = null
    private val bluetoothGattServerCallback: BluetoothGattServerCallback =
        object : BluetoothGattServerCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(
                device: BluetoothDevice?,
                status: Int,
                newState: Int,
            ) {
                super.onConnectionStateChange(device, status, newState)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onConnectionStateChange device=$device ,status=$status ,newState=$newState")
                val tmp = connectedDevice
                if (tmp != null) {
                    if (tmp.address == device?.address) {
                        if (newState == 2) {
                            // 同设备连接 ignore
                        } else if (newState == 0) {
                            callback(ServerEvent.OnDisconnected)
                        }
                    } else {
                        if (newState == 2) {
                            cancelConnectedDevice()
                            callback(ServerEvent.OnConnected(device, status, newState))
                        } else if (newState == 0) {
                            // 非当前设备断开连接 ignore
                        }
                    }

                } else {
                    // 第一次连接
                    connectedDevice = device
                    callback(ServerEvent.OnConnected(device, status, newState))
                }
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onServiceAdded status=$status ,service=${service?.uuid}")
                callback(ServerEvent.OnServiceAdded(status, service))
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?,
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onCharacteristicReadRequest ${characteristic?.uuid} ")
                val d = device?:return
                eventCallback(BleSlaveOnCharacteristicReadRequest(d, requestId, offset, characteristic))

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
                val d = device?:return
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

            override fun onDescriptorReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor?,
            ) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onDescriptorReadRequest ${descriptor?.uuid}")
                val d = device?:return
                val r = BleSlaveOnDescriptorReadRequest(d, requestId, offset, descriptor)
                eventCallback(r)
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
                val d = device?:return
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

            override fun onExecuteWrite(
                device: BluetoothDevice?,
                requestId: Int,
                execute: Boolean,
            ) {
                super.onExecuteWrite(device, requestId, execute)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onExecuteWrite ")
                val d = device?:return
                val r = BleSlaveOnExecuteWrite(d, requestId, execute)
                eventCallback(r)
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                super.onNotificationSent(device, status)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onNotificationSent ")
                val d = device?:return
                val r = BleSlaveOnNotificationSent(d, status)
                eventCallback(r)

            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                super.onMtuChanged(device, mtu)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onMtuChanged $mtu")
                val d = device?:return
                val r = BleSlaveOnMtuChanged(d, mtu)
                eventCallback(r)
            }

            override fun onPhyUpdate(
                device: BluetoothDevice?,
                txPhy: Int,
                rxPhy: Int,
                status: Int,
            ) {
                super.onPhyUpdate(device, txPhy, rxPhy, status)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onPhyUpdate ")
                val d = device?:return
                val r = BleSlaveOnPhyUpdate(d, txPhy, rxPhy, status)
                eventCallback(r)
            }

            override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
                super.onPhyRead(device, txPhy, rxPhy, status)
                d("[AndroidPlatformServer]BluetoothGattServerCallback onPhyRead ")
                val d = device?:return
                val r = BleSlaveOnPhyRead(d, txPhy, rxPhy, status)
                eventCallback(r)
            }
        }

    private var addServiceInStartJob: Job? = null

    @SuppressLint("MissingPermission")
    fun start(services: Array<PlatformBluetoothGattService>) {

        d("[AndroidPlatformServer]addService services:$services")
        addServiceInStartJob?.cancel()
        bluetoothGattServer?.cancel()
        bluetoothGattServer = null
        val bluetoothGattServices = services.map { it.asNativeBase() }
        val curBluetoothGattServer =
            bluetoothManager.openGattServer(context, bluetoothGattServerCallback)
                ?: throw IllegalStateException("openGattServer result null")
        var success = true
        addServiceInStartJob = coroutineScope.launch {
            bluetoothGattServices.forEach {
                ensureActive()
                val addService = curBluetoothGattServer.addService(it)
                delay(BleConfig.OPT_INTERVAL_TIMESTAMP)
                if (!addService) {
                    success = false
                    return@forEach
                }
            }
            if (success) {
                bluetoothGattServer = curBluetoothGattServer
                callback(ServerEvent.AddServiceSuccess)
            } else {
                curBluetoothGattServer.cancel()
                callback(ServerEvent.AddServiceFail)
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun cancelConnectedDevice() {
        try {
            val tmp = connectedDevice
            if (tmp != null) {
                bluetoothGattServer?.cancelConnection(tmp)
            }
        } catch (e: Exception) {
            w("cancelDevice error = $e")
        } finally {
            connectedDevice = null
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
        val device = connectedDevice ?: return false
        return bluetoothGattServer?.let { server ->
            server.sendResponse(device, requestId, status, offset, value)
        } ?: false
    }


    @SuppressLint("MissingPermission")
    fun write(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        confirm:Boolean
    ): Boolean {
        @OptIn(ExperimentalStdlibApi::class)
        d("[AndroidPlatformServer]write ${characteristic.uuid} ${value.toHexString()} $confirm")
        val device = connectedDevice ?: return false

        return bluetoothGattServer?.let { server ->
            characteristic.value = value
            server.notifyCharacteristicChanged(device, characteristic,confirm )
        }?:false
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