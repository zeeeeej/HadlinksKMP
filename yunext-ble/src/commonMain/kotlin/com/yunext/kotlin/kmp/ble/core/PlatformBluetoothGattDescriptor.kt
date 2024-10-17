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

    sealed class Value(val value: ByteArray?) {
        data object EnableNotificationValue :
            Value(PlatformBluetoothGattDescriptor.EnableNotificationValue) {
            @OptIn(ExperimentalStdlibApi::class)
            override fun toString(): String {
                return "EnableNotification(${value?.toHexString()})"
            }
        }

        data object EnableIndicationValue :
            Value(PlatformBluetoothGattDescriptor.EnableIndicationValue) {
            @OptIn(ExperimentalStdlibApi::class)
            override fun toString(): String {
                return "EnableIndication(${value?.toHexString()})"
            }
        }

        data object DisableNotificationValue :
            Value(PlatformBluetoothGattDescriptor.DisableNotificationValue) {
            @OptIn(ExperimentalStdlibApi::class)
            override fun toString(): String {
                return "DisableNotification(${value?.toHexString()})"
            }
        }

        class NaN(value: ByteArray?) : Value(value) {

            @OptIn(ExperimentalStdlibApi::class)
            override fun toString(): String {
                return "NaN(${value?.toHexString()})"
            }
        }

    }

    companion object {
        /* Value used to enable notification for a client configuration descriptor */
        private val EnableNotificationValue = byteArrayOf(0x01, 0x00)

        /* Value used to enable indication for a client configuration descriptor */
        private val EnableIndicationValue = byteArrayOf(0x02, 0x00)

        /* Value used to disable notifications or indications */
        private val DisableNotificationValue = byteArrayOf(0x00, 0x00)
    }
}

val PlatformBluetoothGattDescriptor.status: PlatformBluetoothGattDescriptor.Value
    get() = when {
        this.value.contentEquals(PlatformBluetoothGattDescriptor.Value.DisableNotificationValue.value) -> PlatformBluetoothGattDescriptor.Value.DisableNotificationValue
        this.value.contentEquals(PlatformBluetoothGattDescriptor.Value.EnableNotificationValue.value) -> PlatformBluetoothGattDescriptor.Value.EnableNotificationValue
        this.value.contentEquals(PlatformBluetoothGattDescriptor.Value.EnableIndicationValue.value) -> PlatformBluetoothGattDescriptor.Value.EnableIndicationValue
        else -> PlatformBluetoothGattDescriptor.Value.NaN(this.value)
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
    value: ByteArray?
): PlatformBluetoothGattDescriptor

const val NotifyDescriptorUUID = "00002902-0000-1000-8000-00805f9b34fb"