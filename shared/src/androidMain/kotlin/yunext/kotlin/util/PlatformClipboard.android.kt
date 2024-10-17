package yunext.kotlin.util

import android.content.ClipData
import android.content.ClipboardManager
import com.yunext.kotlin.kmp.context.application
import com.yunext.kotlin.kmp.context.hdContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun clipBroad(label: String, text: String) {
    return suspendCoroutine {
        val ctx = hdContext.application
        val clipboard: ClipboardManager =
            ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        it.resume(Unit)
    }
}