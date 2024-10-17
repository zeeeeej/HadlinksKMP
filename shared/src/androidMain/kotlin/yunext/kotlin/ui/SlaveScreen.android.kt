package yunext.kotlin.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.uuid.ExperimentalUuidApi


@androidx.compose.runtime.Composable
actual fun SlaveScreenPlatform(
    modifier: Modifier,
    @OptIn(ExperimentalUuidApi::class)
    content: @Composable SlaveVM.() -> Unit
) {
    @OptIn(ExperimentalUuidApi::class)
    val vm: SlaveVM = viewModel()
    @OptIn(ExperimentalUuidApi::class)
    content.invoke(vm)
}