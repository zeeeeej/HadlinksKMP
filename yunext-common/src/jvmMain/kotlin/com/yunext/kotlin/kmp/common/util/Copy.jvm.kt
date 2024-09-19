package com.yunext.kotlin.kmp.common.util

import com.yunext.kmp.context.UnSupportPlatformException

actual suspend fun copy(label: String, text: String) {
    throw UnSupportPlatformException("jvm")
}