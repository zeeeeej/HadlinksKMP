import com.yunext.kotlin.kmp.common.util.hdMD5

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}! ".run {
            this + hdMD5(this)
        }
    }
}