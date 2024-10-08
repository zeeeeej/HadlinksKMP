package com.yunext.kotlin.kmp.ble.core

import com.yunext.kmp.context.UnSupportPlatformException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
actual fun bluetoothGattDescriptor(
    uuid: Uuid,
    permissions: Array<PlatformBluetoothGattDescriptor.Permission>,
    value: ByteArray?,
): PlatformBluetoothGattDescriptor {
    throw UnSupportPlatformException("暂不支持jvm")
}