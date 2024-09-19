package yunext.kotlin.domain

import kotlinx.serialization.Serializable

@Serializable
class ApiResponse<out T>(
    val data: T?,
    val code: Int,
    val msg: String?,
) {
    companion object {
        const val SUCCESS = 0
        const val FAIL = 1

    }
}

