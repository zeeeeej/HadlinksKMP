package com.yunext.kotlin.kmp.ble.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
actual fun bluetoothGattService(
    uuid: Uuid,
    serviceType: PlatformBluetoothGattService.ServiceType,
    includeServices: Array<PlatformBluetoothGattService>,
    characteristics: Array<PlatformBluetoothGattCharacteristic>,
): PlatformBluetoothGattService {
    return AndroidBluetoothGattService(
        uuid = uuid,
        serviceType = serviceType,
        includeServices = includeServices,
        characteristics = characteristics
    )
}

private class AndroidBluetoothGattService @OptIn(ExperimentalUuidApi::class) internal constructor(
    override val uuid: Uuid,
    override val serviceType: PlatformBluetoothGattService.ServiceType = PlatformBluetoothGattService.ServiceType.Primary,
    includeServices: Array<PlatformBluetoothGattService> = emptyArray(),
    characteristics: Array<PlatformBluetoothGattCharacteristic> = emptyArray(),
) : PlatformBluetoothGattService {

    private val _includeServices: Array<PlatformBluetoothGattService> = includeServices
    private val _characteristics: Array<PlatformBluetoothGattCharacteristic> = characteristics

    override val includeServices: Array<PlatformBluetoothGattService>
        get() = _includeServices

    override val characteristics: Array<PlatformBluetoothGattCharacteristic>
        get() = _characteristics
}




