package com.yunext.kotlin.kmp.ble.slave

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothWork

actual class PlatformResponse(
    val requestId: Int,
    val offset: Int,
    val value: ByteArray?
) : PlatformBluetoothWork