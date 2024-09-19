package yunext.kotlin.domain

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val password: String,
    val nickname: String,
    val age: Int,
)