package com.yunext.kotlin.kmp.ble.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface PlatformBluetoothGattService : PlatformBluetoothGattValue {
    val serviceType: ServiceType
    val includeServices: Array<PlatformBluetoothGattService>
    val characteristics: Array<PlatformBluetoothGattCharacteristic>

    enum class ServiceType {
        Primary, Secondary;
    }
}

val PlatformBluetoothGattService.ServiceType.value: UInt
    get() = when (this) {
        PlatformBluetoothGattService.ServiceType.Primary -> 0u
        PlatformBluetoothGattService.ServiceType.Secondary -> 1u
    }


@OptIn(ExperimentalUuidApi::class)
expect fun bluetoothGattService(
    uuid: Uuid,
    serviceType: PlatformBluetoothGattService.ServiceType,
    includeServices: Array<PlatformBluetoothGattService>,
    characteristics: Array<PlatformBluetoothGattCharacteristic>,
): PlatformBluetoothGattService


fun List<PlatformBluetoothGattService>.findService(uuid: String): PlatformBluetoothGattService? {
    if (this.isEmpty()) return null
    return this.singleOrNull() {
        @OptIn(ExperimentalUuidApi::class)
        it.uuid.toString() == uuid
    }
}

fun PlatformBluetoothGattService.findCharacteristic(uuid: String): PlatformBluetoothGattCharacteristic? {
    val chs = this.characteristics
    if (chs.isEmpty()) return null
    return chs.singleOrNull {
        @OptIn(ExperimentalUuidApi::class)
        it.uuid.toString() == uuid
    }
}

fun PlatformBluetoothGattCharacteristic.findDescriptor(uuid: String): PlatformBluetoothGattDescriptor? {
    val descriptors = this.descriptors
    if (descriptors.isEmpty()) return null
    return descriptors.singleOrNull {
        @OptIn(ExperimentalUuidApi::class)
        it.uuid.toString() == uuid
    }
}
