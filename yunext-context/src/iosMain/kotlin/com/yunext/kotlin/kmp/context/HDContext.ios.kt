@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.yunext.kotlin.kmp.context

actual class HDContext actual constructor() {
    actual val context: Any
        get() = DEFAULT

    actual fun init(ctx: Any) {

    }

    companion object {
        private val DEFAULT = Any()
    }
}