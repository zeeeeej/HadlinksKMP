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
