import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import org.jetbrains.compose.ui.tooling.preview.Preview
import yunext.kotlin.ui.TestCase

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            Modifier.fillMaxSize().background(ZhongGuoSe.月白.color).padding(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            //Text(Greeting().greet())
            TestCase()
        }

    }
}

