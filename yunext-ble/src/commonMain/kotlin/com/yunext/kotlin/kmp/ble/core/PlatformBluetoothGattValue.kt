package com.yunext.kotlin.kmp.ble.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface PlatformBluetoothGattValue {
    @OptIn(ExperimentalUuidApi::class)
    val uuid: Uuid
}

