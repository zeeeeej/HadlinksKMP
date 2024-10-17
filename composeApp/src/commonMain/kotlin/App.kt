import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import org.jetbrains.compose.ui.tooling.preview.Preview
import yunext.kotlin.ui.BluetoothScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val remainingInsets = remember { MutableWindowInsets() }
        val safeContent = WindowInsets.safeContent
        Box(Modifier.navigationBarsPadding()
            .onConsumedWindowInsetsChanged { consumedWindowInsets ->
                remainingInsets.insets = safeContent.exclude(consumedWindowInsets)
            }) {
            // padding can be used without recomposition when insets change.
            Box(Modifier.padding(remainingInsets.asPaddingValues())){
                BluetoothScreen(Modifier.fillMaxSize())
            }
        }
    }

//    Box(
//        Modifier
//            .fillMaxSize()
//            .background(ZhongGuoSe.月白.color)
//            .padding(WindowInsets.safeContent.asPaddingValues()),
//        contentAlignment = Alignment.TopCenter
//    ) {
//        BluetoothScreen(Modifier.fillMaxSize())
//    }
}

