package yunext.kotlin.ui

import kotlinx.coroutines.CoroutineScope

actual open class PlatformViewModel {
    protected actual val scope: CoroutineScope
        get() = TODO("Not yet implemented")

    protected actual open fun onCleared() {
    }

}