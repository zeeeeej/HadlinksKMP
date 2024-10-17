package com.yunext.kotlin.kmp.ble.master

import com.yunext.kotlin.kmp.ble.core.AndroidBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.display
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryImpl
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryOwner
import com.yunext.kotlin.kmp.ble.history.bleOpt
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.e
import com.yunext.kotlin.kmp.ble.util.i
import com.yunext.kotlin.kmp.ble.util.w
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

actual fun PlatformMaster(context: PlatformBluetoothContext): PlatformMaster {
    return AndroidMaster(context as AndroidBluetoothContext)
}

internal val masterScope by lazy {
    CoroutineScope(Dispatchers.Main.immediate + SupervisorJob() + CoroutineName("slaveScope"))
}


private class AndroidMaster(
    platformContext: AndroidBluetoothContext,
    override val historyOwner: BluetoothHistoryOwner = BluetoothHistoryImpl()
) : PlatformMaster, BluetoothHistoryOwner by historyOwner {
    private val platformContext: AndroidBluetoothContext = platformContext
    private val context = (platformContext).context.applicationContext

    //        private val scanner = FastBleScanner()
    private val scanner = AndroidNativeScanner(context, historyOwner)
    override val status: StateFlow<PlatformMasterScanStatus> = scanner.status
    override val scanResults: StateFlow<List<AndroidMasterScanResult>> = scanner.scanResults

    //    private val connectedMap: MutableMap<String, PlatformConnector> = mutableMapOf()
    private val _connectConnectorMap: MutableStateFlow<Map<String, PlatformConnector>> =
        MutableStateFlow(
            emptyMap()
        )

    private val _connectStatusMap: MutableStateFlow<Map<String, PlatformConnectorStatus>> =
        MutableStateFlow(
            emptyMap()
        )

    private val _connectServicesMap: MutableStateFlow<Map<String, List<PlatformBluetoothGattService>>> =
        MutableStateFlow(
            emptyMap()
        )

    override val connectStatusMap: StateFlow<Map<String, PlatformConnectorStatus>> =
        _connectStatusMap.asStateFlow()

//        _connectStatusMap.map {
//            e("_connectStatusMap.flatMapConcat")
//            val newMap = it.map { (k, v) ->
//                e("_connectStatusMap.flatMapConcat k=$k v=${v.status.value}")
//                k to v.status.value
//            }.toMap()
//            newMap
//        }.stateIn(masterScope, SharingStarted.Eagerly, mapOf())

    override val connectServicesMap: StateFlow<Map<String, List<PlatformBluetoothGattService>>>
        get() = _connectServicesMap.asStateFlow()

    override fun startScan() {
        bleOpt("startScan")
        scanner.startScan()
    }

    override fun stopScan() {
        bleOpt("stopScan")
        scanner.stopScan()
    }

    private var connectJob: Job? = null
    override fun connect(device: PlatformBluetoothDevice): Boolean {
        bleOpt("connect ${device.display}")
        val size = _connectConnectorMap.value.size
        i("connect size=${size}")
        if (size >= 4) {
            w("连接的设备超过4个")
            return false
        }
        stopScan()
        val requireAndroidConnector = requireAndroidConnector(device)
        if (requireAndroidConnector != null) {
            requireAndroidConnector.connect()
            return true
        }
        val result = requireAndroidMasterScanResult(device)
        check(result != null) {
            "没有此设备${device.name}/${device.address}"
        }
        val connector = AndroidNativeConnector(ctx = context, result.device, historyOwner)
//        val connector = FastBleConnector(ctx = context, result.device)
        connectJob?.cancel()
        connectJob =
            masterScope.launch {
                launch {
                    connector.status.collect() {
                        e("_connectStatusMap collect $it")
                        val old = _connectConnectorMap.value.toMutableMap()
                        when (it) {
                            is PlatformConnectorStatus.Connected -> {

                            }

                            is PlatformConnectorStatus.Connecting -> {
//                                old[device.address] = connector
                            }

                            is PlatformConnectorStatus.Disconnected -> {
                                old.remove(device.address)
                                i("删除->${device.address}")
                                clearJobs()

                            }

                            is PlatformConnectorStatus.Disconnecting -> {

                            }

                            is PlatformConnectorStatus.Idle -> {
                                //old.remove(device.address)
                                //i("idle 删除${device.address}")
                                old[device.address] = connector
                            }

                            is PlatformConnectorStatus.ServiceDiscovered -> {

                            }
                        }
                        _connectConnectorMap.value = old.toMap()
                        onConnectDeviceChanged()
                    }
                }
            }

        connector.connect()

//        val old = _connectConnectorMap.value.toMutableMap()
//        old[device.address] = connector
//        _connectConnectorMap.value = old

        onConnectDeviceChanged()
        return true
    }

    private fun onConnectDeviceChanged() {
        _connectStatusMap.value = _connectConnectorMap.value.map { (address, connector) ->
            address to connector.status.value
        }.toMap()
    }

    private fun requireAndroidConnector(device: PlatformBluetoothDevice): PlatformConnector? =
        _connectConnectorMap.value[device.address]


    private fun requireAndroidMasterScanResult(device: PlatformBluetoothDevice): AndroidMasterScanResult? {
        d("device:${device.display} scanResults:${scanResults.value.joinToString { it.address.toString() }}")
        return scanResults.value.singleOrNull() {
            it.address == device.address
        }
    }


    override fun disconnect(device: PlatformBluetoothDevice): Boolean {
        bleOpt("disconnect ${device.display}")
        clearJobs()
        val old = _connectConnectorMap.value.toMutableMap()
        val androidConnector = old[device.address]
        old.remove(device.address)
        androidConnector?.disconnect()
        androidConnector?.close()
        _connectConnectorMap.value = old
        onConnectDeviceChanged()
        return true
    }

//    public void enableCharacteristicNotify(BleNotifyCallback bleNotifyCallback, String uuid_notify,
//    boolean userCharacteristicDescriptor) {
//        if (mCharacteristic != null
//            && (mCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//
//            handleCharacteristicNotifyCallback(bleNotifyCallback, uuid_notify);
//            setCharacteristicNotification(mBluetoothGatt, mCharacteristic, userCharacteristicDescriptor, true, bleNotifyCallback);
//        } else {
//            if (bleNotifyCallback != null)
//                bleNotifyCallback.onNotifyFailure(new OtherException("this characteristic not support notify!"));
//        }
//    }


    override fun enableNotify(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean {
        bleOpt("enableNotify")
        val androidConnector = _connectConnectorMap.value[device.address] ?: return false
        onConnectDeviceChanged()
        return androidConnector.enableNotify(service, characteristic, enable)
    }

    override fun enableIndicate(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean {
        bleOpt("enableIndicate")
        val androidConnector = _connectConnectorMap.value[device.address] ?: return false
        onConnectDeviceChanged()
        return androidConnector.enableIndicate(service, characteristic, enable)
    }

    override fun read(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic
    ): Boolean {
        bleOpt("read")
        val androidConnector = _connectConnectorMap.value[device.address] ?: return false
        return androidConnector.read(service, characteristic)
    }

    override fun write(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        bleOpt("write")
        val androidConnector = _connectConnectorMap.value[device.address] ?: return false
        return androidConnector.write(service, characteristic, data)
    }

    private fun clearJobs() {
        connectJob?.cancel()
        connectJob = null
    }

    override fun close() {
        bleOpt("close")
        clearJobs()
        scanner.close()
        _connectConnectorMap.value.forEach { (address, connector) ->
            connector.close()
        }
        _connectConnectorMap.value = mapOf()
        onConnectDeviceChanged()
    }


}

