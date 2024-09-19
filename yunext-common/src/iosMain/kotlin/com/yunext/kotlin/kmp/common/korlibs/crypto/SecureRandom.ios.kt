package com.yunext.kotlin.kmp.common.korlibs.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
actual fun fillRandomBytes(array: ByteArray) {
    if (array.isEmpty()) return

    array.usePinned { pin ->
        val ptr = pin.addressOf(0)
        val status = SecRandomCopyBytes(kSecRandomDefault, array.size.convert(), ptr)
        if (status != errSecSuccess) {
            error("Error filling random bytes. errorCode=$status")
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun seedExtraRandomBytes(array: ByteArray) {
    if (array.isEmpty()) return

    try {
        array.usePinned { pin ->
            val ptr = pin.addressOf(0)
            val file = fopen("/dev/urandom", "wb")
            if (file != null) {
                fwrite(ptr, 1.convert(), array.size.convert(), file)
                for (n in array.indices) array[n] = ptr[n]
                fclose(file)
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}