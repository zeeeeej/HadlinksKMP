package yunext.kotlin.ui

import ZhongGuoSe
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ExitToApp
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.KeyboardArrowDown
import androidx.compose.material.icons.twotone.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import color
import com.yunext.kotlin.kmp.ble.history.BluetoothHistory
import com.yunext.kotlin.kmp.ble.history.IBleIn
import com.yunext.kotlin.kmp.ble.history.IBleOpt
import com.yunext.kotlin.kmp.ble.history.IBleOut
import com.yunext.kotlin.kmp.ble.history.type
import com.yunext.kotlin.kmp.common.util.datetimeFormat
import kotlinx.coroutines.launch
import randomZhongGuoSe

private object HistoryDefaults {
    val Style_1 = TextStyle.Default.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
    val Style_2 = TextStyle.Default.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Monospace
    )
    val Style_3 = TextStyle.Default.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Light,
        fontFamily = FontFamily.Monospace
    )
    val Style_TYPE = TextStyle.Default.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace
    )
}

private enum class ScrollStatus {
    Top, Bottom, Center, NaN;
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HistoriesInfo(
    modifier: Modifier = Modifier,
    list: List<BluetoothHistory>,
    onClose: () -> Unit,
    onShare: (List<BluetoothHistory>) -> Unit
) {
    val state = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
//    var auto by remember { mutableStateOf(true) }
    val auto by remember {
        derivedStateOf {
            state.firstVisibleItemIndex == 0
        }
    }

    val scrollStatus by remember {
        derivedStateOf {
            when {
                state.canScrollBackward && !state.canScrollForward -> ScrollStatus.Bottom
                !state.canScrollBackward && state.canScrollForward -> ScrollStatus.Top
                !state.canScrollBackward && !state.canScrollForward -> ScrollStatus.NaN
                else -> ScrollStatus.Center
            }
        }
    }

    val isBottom by remember {
        derivedStateOf {
            state.canScrollForward
        }
    }
    var suoxie by remember { mutableStateOf(true) }
    var showIn by remember { mutableStateOf(true) }
    var showOut by remember { mutableStateOf(true) }
    var showOpt by remember { mutableStateOf(true) }
    var filterList by remember() { mutableStateOf(list) }
    LaunchedEffect(list, showIn, showOpt, showOut) {
        filterList = list.filter {
            when (it) {
                is IBleIn -> showIn
                is IBleOpt -> showOpt
                is IBleOut -> showOut
            }
        }
        if (auto) {
            state.scrollToItem(0)
        }

    }
    Box(modifier = modifier) {
        Column {


            key("top") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "日志：(${filterList.size}条${
                            if (auto) {
                                "..."
                            } else ""
                        })"
                    )
                    Text(
                        "In",
                        style = HistoryDefaults.Style_TYPE.copy(
                            color = if (showIn) ZhongGuoSe.余烬红.color else ZhongGuoSe.垩灰.color,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.clickable {
                            showIn = !showIn
                        }.widthIn(min = 24.dp)
                    )
                    Text(
                        "Out",
                        style = HistoryDefaults.Style_TYPE.copy(
                            color = if (showOut) ZhongGuoSe.余烬红.color else ZhongGuoSe.垩灰.color,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.clickable {
                            showOut = !showOut
                        }.widthIn(min = 24.dp)
                    )
                    Text(
                        "Opt",
                        style = HistoryDefaults.Style_TYPE.copy(
                            color = if (showOpt) ZhongGuoSe.余烬红.color else ZhongGuoSe.垩灰.color,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.clickable {
                            showOpt = !showOpt
                        }.widthIn(min = 24.dp)
                    )
                }

            }
            LazyColumn(Modifier.weight(1f), state) {

                itemsIndexed(filterList, { _, it -> "${it.timestamp}${it.message}" }) { index, it ->
                    HistoryItem(
                        modifier = Modifier.background(ZhongGuoSe.乳白.color.copy(if (index % 2 == 0) 1f else .5f)),
                        history = it, suoxie = { suoxie }, onSuoXie = {
                            suoxie = !suoxie
                        })
                }
            }
            key("bottom") {
                Spacer(
                    Modifier.fillMaxWidth().height(4.dp).background(randomZhongGuoSe().color)
                )
            }

        }

        FloatingActionButton(
            onClick = {
                onClose()
            }, modifier = Modifier.align(Alignment.TopEnd)
                .padding(16.dp)
                .size(24.dp), backgroundColor = ZhongGuoSe.向日葵黄.color
        ) {
            Image(Icons.TwoTone.Close, null)
        }

        FloatingActionButton(
            onClick = {
                onShare(filterList)
            }, modifier = Modifier.align(Alignment.CenterEnd)
                .padding(16.dp)
                .size(24.dp), backgroundColor = ZhongGuoSe.向日葵黄.color
        ) {
            Image(Icons.Sharp.Share, null)
        }


        Column(
            Modifier.align(Alignment.BottomEnd)
                .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        state.scrollToItem(filterList.size)
                    }
                }, modifier = Modifier.size(24.dp), backgroundColor =
                if (scrollStatus != ScrollStatus.Bottom && scrollStatus != ScrollStatus.NaN) {
                    ZhongGuoSe.向日葵黄.color
                } else ZhongGuoSe.夜灰.color
            ) {
                Image(Icons.TwoTone.KeyboardArrowDown, null, modifier = Modifier)
            }

            Spacer(Modifier.height(16.dp))

            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        state.scrollToItem(0)
                    }
                },
                modifier = Modifier
                    .size(24.dp),
                backgroundColor = if (scrollStatus != ScrollStatus.Top && scrollStatus != ScrollStatus.NaN) {
                    ZhongGuoSe.向日葵黄.color
                } else ZhongGuoSe.夜灰.color
            ) {
                Image(Icons.TwoTone.KeyboardArrowUp, null, modifier = Modifier)
            }
        }

//        Text(
//            "scrollStatus=$scrollStatus", style = HistoryDefaults.Style_TYPE
//        )


    }

}

@Composable
private fun HistoryItem(
    modifier: Modifier = Modifier,
    history: BluetoothHistory,
    suoxie: () -> Boolean,
    onSuoXie: () -> Unit
) {
//    var suoxie by remember { mutableStateOf(true) }

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(datetimeFormat {
            if (suoxie()) {
                history.timestamp.toStr("HH:mm:ss[ SSS]")
            } else
                history.timestamp.toStr()
        }, style = HistoryDefaults.Style_1, modifier = Modifier.clickable {
//            suoxie = !suoxie
            onSuoXie()
        })
        Text(history.type, style = HistoryDefaults.Style_TYPE.copy(color = ZhongGuoSe.大红.color))
        Text(
            "${history.tag ?: ""}${history.message}", style = HistoryDefaults.Style_3.copy(
                color = when (history) {
                    is IBleIn -> ZhongGuoSe.古铜绿.color
                    is IBleOpt -> ZhongGuoSe.天蓝.color
                    is IBleOut -> ZhongGuoSe.月季红.color
                }
            )
        )

    }
}