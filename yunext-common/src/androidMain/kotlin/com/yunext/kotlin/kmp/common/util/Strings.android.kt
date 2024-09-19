package com.yunext.kotlin.kmp.common.util

import java.security.MessageDigest

private const val MD5 = "MD5"
private val UPPER_HEX_DIGITS = charArrayOf(
    '0', '1', '2', '3', '4',
    '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F'
)

private val LOWER_HEX_DIGITS = charArrayOf(
    '0', '1', '2', '3', '4',
    '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f'
)
fun md5(text: String, upper: Boolean = false): String? {
    val md5 = try {
        MessageDigest.getInstance(MD5)
    } catch (e: Throwable) {
        return null
    }
    val byteArray = text.toByteArray()
    val digest = md5.digest(byteArray)
    val chars = CharArray(digest.size * 2)
    val hexDigits = if (upper) UPPER_HEX_DIGITS else LOWER_HEX_DIGITS
    digest.forEachIndexed { index, byte ->
        chars[index * 2] = hexDigits[byte.toInt() ushr 4 and 0x0f]
        chars[index * 2 + 1] = hexDigits[byte.toInt() and 0x0f]
    }
    return String(chars)
}

actual fun hdMD5(text: String, upperCase: Boolean): String? {
    return md5(text,upperCase)
}


private const val BASE = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

actual fun hdUUID(length: Int): String {
    require(length > 0) {
        "hdRandomString length must > 0"
    }
    return List(length) {
        BASE.random().toString()
    }.joinToString (""){ it }
}