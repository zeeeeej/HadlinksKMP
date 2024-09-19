package yunext.kotlin.domain

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val deviceId: String,
    val deviceName: String,
    val productKey: String,
    val owner: String?,
)