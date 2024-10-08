package com.yunext.kotlin.kmp.ble.core

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
    return AndroidBluetoothGattCharacteristic(
        uuid = uuid,
        permissions  = permissions,
        properties = properties,
        descriptors = descriptors,
        value = value
    )
}

private class AndroidBluetoothGattCharacteristic @ExperimentalUuidApi  constructor(
    override val uuid: Uuid,
    override val permissions: Array<PlatformBluetoothGattCharacteristic.Permission>,
    override val properties: Array<PlatformBluetoothGattCharacteristic.Property>,
    override val descriptors: Array<PlatformBluetoothGattDescriptor>,
    value:ByteArray?
) : PlatformBluetoothGattCharacteristic {
    private var _value: ByteArray? = value
    override val value: ByteArray?
        get() = _value
}