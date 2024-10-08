package com.yunext.kotlin.kmp.ble.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface PlatformBluetoothGattDescriptor : PlatformBluetoothGattValue {

    val permissions: Array<Permission>
    val value: ByteArray?

    enum class Permission {
        PermissionRead,
        PermissionReadEncrypted,
        PermissionReadEncryptedMitm,
        PermissionWrite,
        PermissionWriteEncrypted,
        PermissionWriteEncryptedMitm,
        PermissionWriteSigned,
        PermissionWriteSignedMitm
        ;
    }

    companion object {
        /* Value used to enable notification for a client configuration descriptor */
        val EnableNotificationValue = byteArrayOf(0x01, 0x00)

        /* Value used to enable indication for a client configuration descriptor */
        val EnableIndicationValue = byteArrayOf(0x02, 0x00)

        /* Value used to disable notifications or indications */
        val DisableNotificationValue = byteArrayOf(0x00, 0x00)
    }
}

val PlatformBluetoothGattDescriptor.Permission.value: UInt
    get() = when (this) {
        PlatformBluetoothGattDescriptor.Permission.PermissionRead -> 0x01u
        PlatformBluetoothGattDescriptor.Permission.PermissionReadEncrypted -> 0x02u
        PlatformBluetoothGattDescriptor.Permission.PermissionReadEncryptedMitm -> 0x04u
        PlatformBluetoothGattDescriptor.Permission.PermissionWrite -> 0x10u
        PlatformBluetoothGattDescriptor.Permission.PermissionWriteEncrypted -> 0x20u
        PlatformBluetoothGattDescriptor.Permission.PermissionWriteEncryptedMitm -> 0x40u
        PlatformBluetoothGattDescriptor.Permission.PermissionWriteSigned -> 0x80u
        PlatformBluetoothGattDescriptor.Permission.PermissionWriteSignedMitm -> 0x100u
    }

@OptIn(ExperimentalUuidApi::class)
expect fun bluetoothGattDescriptor(
    uuid: Uuid,
    permissions: Array<PlatformBluetoothGattDescriptor.Permission>,
    value:ByteArray?
): PlatformBluetoothGattDescriptor