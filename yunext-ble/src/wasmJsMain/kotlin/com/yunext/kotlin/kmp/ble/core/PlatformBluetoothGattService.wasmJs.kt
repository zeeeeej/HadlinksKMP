package com.yunext.kotlin.kmp.ble.core

import com.yunext.kmp.context.UnSupportPlatformException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
actual fun bluetoothGattService(
    uuid: Uuid,
    serviceType: PlatformBluetoothGattService.ServiceType,
    includeServices: Array<PlatformBluetoothGattService>,
    characteristics: Array<PlatformBluetoothGattCharacteristic>,
): PlatformBluetoothGattService {
    throw UnSupportPlatformException("wasmJs暂不支持")
}