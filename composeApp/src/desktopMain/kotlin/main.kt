import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import hadlinkskmp.composeapp.generated.resources.Res
import hadlinkskmp.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

suspend fun main() = application {
    val state = rememberWindowState(
        size = DpSize(600.dp, 900.dp),
        position = WindowPosition(Alignment.BottomCenter)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource( Res.string.app_name),
        state = state
    ) {
        App()
    }
}