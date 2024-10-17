package yunext.kotlin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.BottomDrawer
import androidx.compose.material.BottomDrawerValue.Closed
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.yunext.kotlin.kmp.ble.history.BluetoothHistory
import com.yunext.kotlin.kmp.ble.history.type
import com.yunext.kotlin.kmp.ble.slave.SlaveState
import com.yunext.kotlin.kmp.common.util.datetimeFormat
import kotlinx.coroutines.launch
import yunext.kotlin.util.clipBroad
import yunext.kotlin.util.randomBG
import kotlin.uuid.ExperimentalUuidApi

@Deprecated("delete")
@androidx.compose.runtime.Composable
expect fun SlaveScreenPlatform(
    modifier: Modifier,
    @OptIn(ExperimentalUuidApi::class)
    content: @Composable SlaveVM.() -> Unit
)

@OptIn(ExperimentalUuidApi::class)
@androidx.compose.runtime.Composable
fun SlaveScreen(modifier: Modifier = Modifier, controller: NavHostController, onBack: () -> Unit) {
//    @OptIn(ExperimentalUuidApi::class)
//    SlaveScreenPlatform(modifier) {
//        SlaveScreenInternal(modifier, this)
//    }
    val vm: SlaveVM = viewModel()
    SlaveScreenInternal(modifier, vm)
}

val BluetoothHistory.display: String
    get() = "${datetimeFormat { this@display.timestamp.toStr() }}[${this@display.type}] ${this.message}"

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalUuidApi
@androidx.compose.runtime.Composable
private fun SlaveScreenInternal(modifier: Modifier = Modifier, slaveVM: SlaveVM) {
    val state by slaveVM.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberBottomDrawerState(Closed)
    val scaffoldState = rememberScaffoldState()
    val toast by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) {
        Scaffold(scaffoldState = scaffoldState) {
            BottomDrawer(
                drawerContent = {
                    HistoriesInfo(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 320.dp, max = 320.dp), state.histories, onClose = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }, onShare = {
                            coroutineScope.launch {
                                clipBroad("slave", it.joinToString("\n") { h -> h.display })
                                scaffoldState.snackbarHostState.showSnackbar("已复制至剪贴板！")
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxSize(),
                drawerState = drawerState,
                gesturesEnabled = drawerState.isClosed

            ) {
                Column(modifier.fillMaxWidth()) {
                    PlatformBluetoothContextInfo(
                        Modifier.fillMaxWidth(),
                        state.enable,
                        state.location,
                        state.permissions
                    ) {
                        slaveVM.requestPermission(it)
                    }

                    Box(Modifier.weight(1f)) {
                        SlaveStateInfo(
                            state = state.slaveState,
                            onStart = slaveVM::start,
                            onStop = slaveVM::stop
                        )
                    }
                }

            }
        }


    }

}