package yunext.kotlin.ktor

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import java.util.concurrent.TimeUnit

actual class HDHttpClient() {
    private val config: HttpClientConfig<*>.() -> Unit = {
        install(DefaultRequest) {
            url {
                protocol = URLProtocol.HTTPS
                host = Api.HOST
                contentType(ContentType.Application.Json)
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 10_000 // 10s
            connectTimeoutMillis = 10_000 // 10s
        }
    }

    val httpClient: HttpClient = HttpClient(OkHttp) {

        config(this)

        engine {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(0, TimeUnit.SECONDS)
            }
        }


    }
}

actual fun httpClient(): HDHttpClient {
    return HDHttpClient()
}