package com.yunext.kotlin.kmp.ble.core

class BluetoothException(
    message: String, cause: Throwable? = null,
) : Throwable(
    message = message, cause = cause
)