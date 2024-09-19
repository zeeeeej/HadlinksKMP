@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.yunext.kotlin.kmp.common.logger

import kotlin.native.concurrent.ThreadLocal

actual interface HDLogger {
    actual var debug: Boolean
    actual fun d(tag: String, msg: String)
    actual fun i(tag: String, msg: String)
    actual fun w(tag: String, msg: String)
    actual fun e(tag: String, msg: String)

    @ThreadLocal
    actual companion object : HDLogger {
        actual override var debug: Boolean = false
        actual override fun d(tag: String, msg: String) {
            println("[$tag][D] $msg")
        }

        actual override fun i(tag: String, msg: String) {
            println("[$tag][I] $msg")
        }

        actual override fun w(tag: String, msg: String) {
            println("[$tag][W] $msg")
        }

        actual override fun e(tag: String, msg: String) {
            println("[$tag][E] $msg")
        }

    }

}