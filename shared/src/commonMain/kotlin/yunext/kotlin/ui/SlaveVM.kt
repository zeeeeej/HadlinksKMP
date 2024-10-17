package yunext.kotlin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformPermission
import com.yunext.kotlin.kmp.ble.core.PlatformPermissionStatus
import com.yunext.kotlin.kmp.ble.core.platformBluetoothContext
import com.yunext.kotlin.kmp.ble.history.BluetoothHistory
import com.yunext.kotlin.kmp.ble.slave.PlatformSlave
import com.yunext.kotlin.kmp.ble.slave.SlaveState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
    private val setting = SettingDataSource.setting
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


    override fun onCleared() {
        super.onCleared()
        println("slaveVm onClear")
        slave.close()
    }
}