package yunext.kotlin.ktor

import kotlinx.serialization.json.Json


object Api{
    const val HOST = "127.0.0.1/8080/"
}

val hdJson : Json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys  =true

}



expect class HDHttpClient
//expect interface HDHttpClientConfig<T>




expect fun httpClient(): HDHttpClient
//expect fun httpClient(config: HDHttpClientConfig<*>.() -> Unit): HDHttpClient