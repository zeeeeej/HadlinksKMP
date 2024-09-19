package com.yunext.kotlin.kmp.common.logger

actual interface HDLogger {
    actual var debug: Boolean
    actual fun d(tag: String, msg: String)
    actual fun i(tag: String, msg: String)
    actual fun w(tag: String, msg: String)
    actual fun e(tag: String, msg: String)

    actual companion object : HDLogger {

        actual override var debug: Boolean = true

        actual override fun d(tag: String, msg: String) {
        }

        actual override fun i(tag: String, msg: String) {
        }

        actual override fun w(tag: String, msg: String) {
        }

        actual override fun e(tag: String, msg: String) {
        }

    }

}