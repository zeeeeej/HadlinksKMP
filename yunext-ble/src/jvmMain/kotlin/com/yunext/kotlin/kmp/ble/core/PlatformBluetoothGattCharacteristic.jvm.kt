package com.yunext.kotlin.kmp.ble.core

import com.yunext.kmp.context.UnSupportPlatformException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
actual fun bluetoothGattCharacteristic(
    uuid: Uuid,
    permissions: Array<PlatformBluetoothGattCharacteristic.Permission>,
    properties: Array<PlatformBluetoothGattCharacteristic.Property>,
    descriptors: Array<PlatformBluetoothGattDescriptor>,
    value: ByteArray?,
): PlatformBluetoothGattCharacteristic {
    throw UnSupportPlatformException("不支持jvm")
}