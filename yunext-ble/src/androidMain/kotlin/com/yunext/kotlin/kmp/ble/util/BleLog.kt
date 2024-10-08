package com.yunext.kotlin.kmp.ble.util

import android.util.Log

internal const val TAG = "[BLE]"
internal var debug = true

internal fun i(msg: String) {
    if (!debug) return
    Log.i(TAG, msg)
}

internal fun d(msg: String) {
    if (!debug) return
    Log.d(TAG, msg)
}

internal fun w(msg: String) {
    if (!debug) return
    Log.w(TAG, msg)
}

internal fun e(msg: String) {
    if (!debug) return
    Log.e(TAG, msg)
}