package com.yunext.kotlin.kmp.common.korlibs.crypto

import com.yunext.kmp.context.UnSupportPlatformException

actual fun fillRandomBytes(array: ByteArray) {
    throw UnSupportPlatformException("wasmJs")
}

actual fun seedExtraRandomBytes(array: ByteArray) {
   throw UnSupportPlatformException("wasmJs")
}