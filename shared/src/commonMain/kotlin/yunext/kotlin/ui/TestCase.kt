package yunext.kotlin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.yunext.kotlin.kmp.ble.slave.SlaveState
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@androidx.compose.runtime.Composable
fun TestCase(modifier: Modifier = Modifier) {
    val slaveVM by remember {
        mutableStateOf(SlaveVM())
    }

    val state by slaveVM.state.collectAsState()



    Column(modifier.fillMaxWidth()) {
        PlatformBluetoothContextInfo(
            Modifier.fillMaxWidth(),
            state.enable,
            state.location,
            state.permissions
        ) {
            slaveVM.requestPermission(it)
        }
        Box(Modifier.weight(1f)) { SlaveStateInfo(state = state.slaveState) }


        Column {
            Button(enabled = when (state.slaveState) {
                is SlaveState.AdvertiseSuccess -> true
                is SlaveState.Connected -> true
                is SlaveState.Idle -> true
                is SlaveState.ServerOpened -> true
            }, onClick = {
                when (state.slaveState) {
                    is SlaveState.AdvertiseSuccess -> slaveVM.stop()
                    is SlaveState.Connected -> slaveVM.stop()
                    is SlaveState.Idle -> slaveVM.start()
                    is SlaveState.ServerOpened -> slaveVM.stop()
                }


            }) {
                Text(
                    "[${
                        when (val s = state.slaveState) {
                            is SlaveState.AdvertiseSuccess -> "添加服务中..."
                            is SlaveState.Connected -> "已连接${s.device.address}/${s.device.name}"
                            is SlaveState.Idle -> "初始化"
                            is SlaveState.ServerOpened -> "等待连接中..."
                        }
                    }]"
                )
            }


        }

    }
}