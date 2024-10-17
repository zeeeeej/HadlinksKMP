package yunext.kotlin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import color
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.master.PlatformConnectorStatus
import com.yunext.kotlin.kmp.ble.master.PlatformMasterScanResult
import randomZhongGuoSe
import yunext.kotlin.util.randomBG

@Composable
internal fun ConnectInfo(
    modifier: Modifier = Modifier,
    list: List<PlatformConnectorStatus>,
    onClick: (PlatformConnectorStatus) -> Unit,
    onNotify: (
        PlatformBluetoothDevice,
        PlatformBluetoothGattService,
        PlatformBluetoothGattCharacteristic
    ) -> Unit
) {
    Column {
        Text("当前连接设备：${list.size}个")
        LazyColumn(modifier) {
            items(list, { (it.device.address) + (it.device.name) }) {
                ConnectInfoItem(modifier = Modifier.fillMaxWidth(), status = it, onClick = {
                    onClick(it)
                }, onNotify = onNotify)
            }
        }
    }

}

@Composable
private fun ConnectInfoItem(
    modifier: Modifier, status: PlatformConnectorStatus,
    onClick: () -> Unit,
    onNotify: (
        PlatformBluetoothDevice,
        PlatformBluetoothGattService,
        PlatformBluetoothGattCharacteristic
    ) -> Unit
) {
    Column(
        modifier.randomBG()
    ) {
        Text(status.device.name)
        Text(status.device.address)
        when (status) {
            is PlatformConnectorStatus.Connected -> {
                Text(
                    "已连接"
                )
                Text(
                    "断开连接", modifier = Modifier.clickable {
                        onClick()
                    }
                )
            }

            is PlatformConnectorStatus.Connecting -> Text("连接中...")
            is PlatformConnectorStatus.Disconnected -> Text("已断开")
            is PlatformConnectorStatus.Disconnecting -> Text("断开中")
            is PlatformConnectorStatus.Idle -> Text("初始化")
            is PlatformConnectorStatus.ServiceDiscovered -> {

                Column(Modifier.fillMaxWidth().heightIn(max = 256.dp)) {
                    PlatformBluetoothGattServicesInfo(
                        services = status.services.toTypedArray(),
                        expend = true,
                        onNotify = { service, ch ->
                            onNotify.invoke(status.device, service, ch)
                        }
                    ) {
                        Text("发现服务")
                    }
                }
                Text(
                    "断开连接", modifier = Modifier.clickable {
                        onClick()
                    }
                )
            }
        }


    }
}