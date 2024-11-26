package yunext.kotlin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformPermission
import com.yunext.kotlin.kmp.ble.core.PlatformPermissionStatus
import com.yunext.kotlin.kmp.ble.core.platformBluetoothContext
import com.yunext.kotlin.kmp.ble.history.BluetoothHistory
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnCharacteristicReadRequest
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnCharacteristicWriteRequest
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnDescriptorReadRequest
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnDescriptorWriteRequest
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnExecuteWrite
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnMtuChanged
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnNotificationSent
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnPhyRead
import com.yunext.kotlin.kmp.ble.slave.PlatformBleSlaveOnPhyUpdate
import com.yunext.kotlin.kmp.ble.slave.PlatformSlave
import com.yunext.kotlin.kmp.ble.slave.SlaveState
import com.yunext.kotlin.kmp.ble.slave.SlaveWriteParam
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import yunext.kotlin.repository.SettingDataSource2
import kotlin.uuid.ExperimentalUuidApi

data class SlaveVMState(
    val enable: Boolean = false,
    val location: Boolean = false,
    val permissions: List<Pair<PlatformPermission, PlatformPermissionStatus>> = emptyList(),
    val slaveState: SlaveState,
    val histories:List<BluetoothHistory> = emptyList()
)

@ExperimentalUuidApi
class SlaveVM : ViewModel() {
    private val setting = SettingDataSource2.createWaterSetting()
    private val platformBluetoothContext: PlatformBluetoothContext = platformBluetoothContext()
    private val _state: MutableStateFlow<SlaveVMState> =
        MutableStateFlow(SlaveVMState(slaveState = SlaveState.Idle(setting = setting)))

    val state: StateFlow<SlaveVMState> = _state.asStateFlow()
    private val slave by lazy {
        PlatformSlave(platformBluetoothContext,setting)
    }

    init {
        viewModelScope.launch {

            launch {
                platformBluetoothContext.enable.collect {
                    _state.value = state.value.copy(
                        enable = it
                    )
                }
            }
            launch {
                platformBluetoothContext.location.collect {
                    _state.value = state.value.copy(
                        location = it
                    )
                }
            }
            launch {
                platformBluetoothContext.permissions.collect {
                    _state.value = state.value.copy(
                        permissions = it.toList()
                    )
                }
            }

            launch {
                slave.slaveState.collect {
                    println("[BLE]vm state = $it")
                    _state.value = state.value.copy(slaveState = it)

                    when (state.value.slaveState){
                        is SlaveState.AdvertiseSuccess -> {}
                        is SlaveState.Connected -> startWriteJob()
                        is SlaveState.Idle -> {}
                        is SlaveState.ServerOpened -> {}
                    }
                }
            }

            launch {
                slave.eventChannel.receiveAsFlow().collect {
                    println("[BLE]vm event = $it")
                    when(it){
                        is PlatformBleSlaveOnCharacteristicReadRequest -> {}
                        is PlatformBleSlaveOnCharacteristicWriteRequest -> {
                            val ch = it.characteristic
                            val v = it.value
                            if (ch?.uuid.toString() == "0000b002-1001-1000-6864-79756e657874"){
                                notifyAllCh()
                            }
                        }
                        is PlatformBleSlaveOnDescriptorReadRequest -> {}
                        is PlatformBleSlaveOnDescriptorWriteRequest -> {}
                        is PlatformBleSlaveOnExecuteWrite -> {}
                        is PlatformBleSlaveOnMtuChanged -> {}
                        is PlatformBleSlaveOnNotificationSent -> {}
                        is PlatformBleSlaveOnPhyRead -> {}
                        is PlatformBleSlaveOnPhyUpdate -> {}
                    }
                }
            }
            launch {
                slave.history.histories.collect{
                    _state.value = state.value.copy(histories = it.asReversed())
                }
            }
        }
    }

    private fun startWriteJob() {
    }

    fun requestPermission(permission: PlatformPermission) {
        platformBluetoothContext.requestPermission(permission)
    }

    fun start() {
        slave.startBroadcast()
    }

    fun stop() {
        slave.stopBroadcast()
    }

    private var notifyAllJob:Job? = null
     fun notifyAllCh() {
        notifyAllJob?.cancel()
        notifyAllJob = viewModelScope.launch {
            val chs = setting.services
                .flatMap {
                    it.characteristics.toList()
                }.filter {
                    it.properties.contains(PlatformBluetoothGattCharacteristic.Property.Notify)
                }
            chs.forEach { ch ->
                delay(1000)
                if (slave.slaveState.value is SlaveState.Connected) {
                    slave.notify(SlaveWriteParam(ch, ch.value ?: byteArrayOf(), false))
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        println("slaveVm onClear")
        slave.close()
    }
}