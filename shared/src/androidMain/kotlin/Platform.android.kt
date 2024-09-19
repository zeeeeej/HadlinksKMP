import android.os.Build
import yunext.kotlin.ktor.httpClient

class AndroidPlatform : Platform {

    override val name: String = "Android ${Build.VERSION.SDK_INT} ${httpClient()}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

