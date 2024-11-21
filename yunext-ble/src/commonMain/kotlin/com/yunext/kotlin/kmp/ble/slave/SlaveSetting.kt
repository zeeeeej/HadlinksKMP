package com.yunext.kotlin.kmp.ble.slave

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface SlaveSetting {
    val deviceName: String
    val broadcastService: PlatformBluetoothGattService
    val services: Array<PlatformBluetoothGattService>
    val broadcastTimeout: Long
}

@OptIn(ExperimentalUuidApi::class)
fun SlaveSetting.searchDescriptor(
    descUUID: String,
    chUUID: String,
    serviceUUID: String
): PlatformBluetoothGattDescriptor? {
    services.forEach { s ->
        s.characteristics.forEach { c ->
            c.descriptors.forEach { d ->
                if (s.uuid.toString() == serviceUUID && c.uuid.toString() == chUUID && d.uuid.toString() == descUUID) {
                    return d
                }
            }
        }
    }
    return null
}