package yunext.kotlin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.slave.SlaveSetting
import com.yunext.kotlin.kmp.ble.slave.SlaveState
import com.yunext.kotlin.kmp.ble.util.display
import kotlin.uuid.ExperimentalUuidApi

private object Defaults {
    val Style_1 = TextStyle.Default.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
    val Style_2 = TextStyle.Default.copy(fontSize = 11.sp, fontWeight = FontWeight.Normal)
    val Style_3 = TextStyle.Default.copy(fontSize = 11.sp, fontWeight = FontWeight.Light)
    const val Show_List = true
}

@Composable
internal fun SlaveStateInfo(modifier: Modifier = Modifier.fillMaxWidth(), state: SlaveState) {
    Column(modifier) {
        Text("【Slave状态】", style = TextStyle.Default.copy(fontWeight = FontWeight.Bold))
        SlaveSettingInfo(setting = state.setting)
        Text(
            text = "[${
                when (state) {
                    is SlaveState.AdvertiseSuccess -> "广播成功"
                    is SlaveState.Connected -> "已连接${state.device.address}/${state.device.name}"
                    is SlaveState.Idle -> "初始化"
                    is SlaveState.ServerOpened -> "服务已开启"
                }
            }]"
        )
    }

}


@Composable
internal fun SlaveSettingInfo(modifier: Modifier = Modifier.fillMaxWidth(), setting: SlaveSetting) {
    var expend by remember { mutableStateOf(false) }
    PlatformBluetoothGattServicesInfo(
        services = setting.services, expend =
        expend, header = {
            Column(modifier = modifier) {
                Row {
                    Text("设备名称")
                    Text(setting.deviceName)
                }

                Column {
                    Text("广播Service")
                    PlatformBluetoothGattServiceInfo(
                        service = setting.broadcastService,
                        expend = false
                    )
                }

                Row {
                    Text("超时时间")
                    Text("${setting.broadcastTimeout}ms")
                }
                Row {
                    Text("设备Service")
                    Text(
                        text = if (expend) "收起" else "展开",
                        modifier = Modifier.clickable {
                            expend = !expend
                        })
                }
            }
        }
    )
}


@Composable
fun PlatformBluetoothGattServicesInfo(
    modifier: Modifier = Modifier, services: Array<PlatformBluetoothGattService>, expend: Boolean,
    header: @Composable () -> Unit
) {
    LazyColumn(modifier = modifier) {
        item {
            header()
        }
        @OptIn(ExperimentalUuidApi::class)
        items(services, { it.uuid.toString() }) {
            PlatformBluetoothGattServiceInfo(service = it, expend = expend)
        }
    }
}

@Composable
internal fun PlatformBluetoothGattServiceInfo(
    modifier: Modifier = Modifier.fillMaxWidth(),
    service: PlatformBluetoothGattService,
    expend: Boolean
) {
    var showList by remember(expend) { mutableStateOf(expend) }
    Column(modifier = modifier) {
        Row {
            @OptIn(ExperimentalUuidApi::class)
            Text(service.uuid.toString(), style = Defaults.Style_1.copy(Color.Red))
            Text(
                "(${service.characteristics.size}个)",
                style = Defaults.Style_1,
                modifier = Modifier.clickable {
                    showList = !showList
                })
            Text("<<${service.serviceType.name.take(1).uppercase()}>>", style = Defaults.Style_1)
        }
        if (service.characteristics.isNotEmpty()) {
            AnimatedVisibility(showList) {
//            if (showList) {
                Column {
                    service.characteristics.forEach {
                        PlatformBluetoothGattCharacteristicInfo(
                            characteristic = it,
                            expend = expend
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlatformBluetoothGattCharacteristicInfo(
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    characteristic: PlatformBluetoothGattCharacteristic,
    expend: Boolean
) {
    var showList by remember(expend) { mutableStateOf(expend) }
    var showDetail by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row {
            @OptIn(ExperimentalUuidApi::class)
            Text(
                characteristic.uuid.toString(),
                style = Defaults.Style_2.copy(Color.Green),
                modifier = Modifier.clickable {
                    showDetail = !showDetail
                })
            Text(
                "${characteristic.descriptors.size}个",
                style = Defaults.Style_2,
                modifier = Modifier.clickable {
                    showList = !showList
                })
//            Text("Characteristic", style = Defaults.Style_1)

        }
        AnimatedVisibility(showDetail) {
//        if (showDetail) {
            Column {
                Text(characteristic.properties.display, style = Defaults.Style_1)
                Text(characteristic.permissions.display, style = Defaults.Style_1)
            }
        }

        if (characteristic.descriptors.isNotEmpty()) {
            AnimatedVisibility(showList) {
//            if (showList) {
                Column {
                    characteristic.descriptors.forEach {
                        PlatformBluetoothGattDescriptorInfo(descriptor = it)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlatformBluetoothGattDescriptorInfo(
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    descriptor: PlatformBluetoothGattDescriptor
) {
    var showDetail by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row {
            @OptIn(ExperimentalUuidApi::class)
            Text(
                descriptor.uuid.toString(),
                style = Defaults.Style_3.copy(color = Color.Blue),
                modifier = Modifier.clickable {
                    showDetail = !showDetail
                })
//            Text("Descriptor", style = Defaults.Style_1)
        }

        AnimatedVisibility(showDetail) {
//        if (showDetail) {
            Text(descriptor.permissions.display, style = Defaults.Style_1)

        }
    }
}


// by flow

@Composable
fun PlatformBluetoothGattServicesInfoByFlow(
    modifier: Modifier = Modifier, services: Array<PlatformBluetoothGattService>, expend: Boolean
) {
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = modifier
            //FlowRow一定是使用verticalScroll横向滚动才有item填充满的效果
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(15.dp)
    ) {
        services.forEach {
            PlatformBluetoothGattServiceInfo(service = it, expend = expend)
        }

    }
}

@Composable
internal fun PlatformBluetoothGattServiceInfoByFlow(
    modifier: Modifier = Modifier.fillMaxWidth(),
    service: PlatformBluetoothGattService,
    expend: Boolean
) {

    var showList by remember(expend) { mutableStateOf(expend) }
    Column(modifier = modifier) {
        Row {
            @OptIn(ExperimentalUuidApi::class)
            Text(service.uuid.toString(), style = Defaults.Style_1.copy(Color.Red))
            Text(
                "(${service.characteristics.size}个)",
                style = Defaults.Style_1,
                modifier = Modifier.clickable {
                    showList = !showList
                })
            Text("<<${service.serviceType.name.take(1).uppercase()}>>", style = Defaults.Style_1)
        }
        if (service.characteristics.isNotEmpty()) {
            AnimatedVisibility(showList) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = modifier
                        //FlowRow一定是使用verticalScroll横向滚动才有item填充满的效果
//                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                ) {
                    service.characteristics.forEach {
                        PlatformBluetoothGattCharacteristicInfo(
                            characteristic = it,
                            expend = expend
                        )
                    }
                }
            }
        }
    }
}
