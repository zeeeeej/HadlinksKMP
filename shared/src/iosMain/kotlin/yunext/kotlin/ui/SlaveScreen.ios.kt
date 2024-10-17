package yunext.kotlin.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.uuid.ExperimentalUuidApi


@androidx.compose.runtime.Composable
actual fun SlaveScreenPlatform(modifier: Modifier,
                               @OptIn(ExperimentalUuidApi::class)
                               content:@Composable SlaveVM.()->Unit){

}