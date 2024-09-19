import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import hadlinkskmp.composeapp.generated.resources.Res
import hadlinkskmp.composeapp.generated.resources.app_name
import kotlinx.browser.document
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    ComposeViewport(document.body!!.apply {
        val appName = getString(Res.string.app_name) // 怎么获取string资源? (suspend方法)
        document.title = "$appName 科特林多平台例子3"
        this.title = "科特林多平台例子4"
    }) {
        App()
    }
}