import io.ktor.client.HttpClient

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

fun test(){
    HttpClient()
}