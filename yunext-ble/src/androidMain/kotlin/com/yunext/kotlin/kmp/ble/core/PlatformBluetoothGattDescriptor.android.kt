package com.yunext.kotlin.kmp.ble.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
actual fun bluetoothGattDescriptor(
    uuid: Uuid,
    permissions: Array<PlatformBluetoothGattDescriptor.Permission>,
    value: ByteArray?,
): PlatformBluetoothGattDescriptor {
    return AndroidBluetoothGattDescriptor(
        uuid = uuid,
        permissions = permissions,
        value = value
    )
}

private class AndroidBluetoothGattDescriptor @ExperimentalUuidApi  constructor(
    override val uuid: Uuid,
    override val permissions: Array<PlatformBluetoothGattDescriptor.Permission>,
    value:ByteArray?
) : PlatformBluetoothGattDescriptor {
    private var _value: ByteArray? = value
    override val value: ByteArray?
        get() = _value
}