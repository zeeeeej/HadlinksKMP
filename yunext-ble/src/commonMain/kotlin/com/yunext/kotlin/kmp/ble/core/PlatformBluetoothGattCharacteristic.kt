package com.yunext.kotlin.kmp.ble.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface PlatformBluetoothGattCharacteristic : PlatformBluetoothGattValue {
    val permissions: Array<Permission>
    val properties: Array<Property>
    val descriptors: Array<PlatformBluetoothGattDescriptor>
    val value:ByteArray?

    enum class WriteType {
        Default,
        WriteNoResponse,
        Signed
        ;
    }

    enum class Property {
        Broadcast,
        Read,
        WriteNoResponse,
        Write,
        Notify,
        Indicate,
        SignedWrite,
        ExtendedProps,
        ;
    }

    enum class Permission {
        Read,
        ReadEncrypted,
        ReadEncryptedMitm,
        Write,
        WriteEncrypted,
        WriteEncryptedMitm,
        WriteSigned,
        WriteSignedMitm,
        ;
    }
}

val PlatformBluetoothGattCharacteristic.WriteType.value: UInt
    get() = when (this) {
        PlatformBluetoothGattCharacteristic.WriteType.Default -> 0x02u
        PlatformBluetoothGattCharacteristic.WriteType.WriteNoResponse -> 0x01u
        PlatformBluetoothGattCharacteristic.WriteType.Signed -> 0x04u
    }

val PlatformBluetoothGattCharacteristic.Property.value: UInt
    get() = when (this) {
        PlatformBluetoothGattCharacteristic.Property.Broadcast -> 0x01u
        PlatformBluetoothGattCharacteristic.Property.Read -> 0x02u
        PlatformBluetoothGattCharacteristic.Property.WriteNoResponse -> 0x04u
        PlatformBluetoothGattCharacteristic.Property.Write -> 0x08u
        PlatformBluetoothGattCharacteristic.Property.Notify -> 0x10u
        PlatformBluetoothGattCharacteristic.Property.Indicate -> 0x20u
        PlatformBluetoothGattCharacteristic.Property.SignedWrite -> 0x40u
        PlatformBluetoothGattCharacteristic.Property.ExtendedProps -> 0x80u
    }

val PlatformBluetoothGattCharacteristic.Permission.value: UInt
    get() = when (this) {
        PlatformBluetoothGattCharacteristic.Permission.Read -> 0x01u
        PlatformBluetoothGattCharacteristic.Permission.ReadEncrypted -> 0x02u
        PlatformBluetoothGattCharacteristic.Permission.ReadEncryptedMitm -> 0x04u
        PlatformBluetoothGattCharacteristic.Permission.Write -> 0x10u
        PlatformBluetoothGattCharacteristic.Permission.WriteEncrypted -> 0x20u
        PlatformBluetoothGattCharacteristic.Permission.WriteEncryptedMitm -> 0x40u
        PlatformBluetoothGattCharacteristic.Permission.WriteSigned -> 0x80u
        PlatformBluetoothGattCharacteristic.Permission.WriteSignedMitm -> 0x100u
    }


@OptIn(ExperimentalUuidApi::class)
expect fun bluetoothGattCharacteristic(
    uuid: Uuid,
    permissions: Array<PlatformBluetoothGattCharacteristic.Permission>,
    properties: Array<PlatformBluetoothGattCharacteristic.Property>,
    descriptors: Array<PlatformBluetoothGattDescriptor>,
    value:ByteArray?
): PlatformBluetoothGattCharacteristic
