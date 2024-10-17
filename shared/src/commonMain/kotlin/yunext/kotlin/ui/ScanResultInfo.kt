package yunext.kotlin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import color
import com.yunext.kotlin.kmp.ble.master.PlatformMasterScanResult
import randomZhongGuoSe
import yunext.kotlin.util.randomBG

@Composable
internal fun ScanResultInfo(
    modifier: Modifier = Modifier,
    list: List<PlatformMasterScanResult>,
    onClick: (PlatformMasterScanResult) -> Unit
) {
    Column {
        Text("当前搜索设备：${list.size}个")
        LazyColumn(modifier) {
            items(list, { (it.address ?: "") + (it.deviceName ?: "") }) {
                ScanResultItem(modifier = Modifier.fillMaxWidth().clickable {
                    onClick(it)
                }, result = it)
            }
        }
    }
}

@Composable
private fun ScanResultItem(modifier: Modifier, result: PlatformMasterScanResult) {
    Column(
        modifier.randomBG()
    ) {
        Text("${result.deviceName}")
        Text("${result.address}")
        Text("${result.rssi}")
    }
}