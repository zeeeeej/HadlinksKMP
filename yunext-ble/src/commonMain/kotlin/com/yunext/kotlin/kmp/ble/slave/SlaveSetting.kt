package com.yunext.kotlin.kmp.ble.slave

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService

 interface SlaveSetting {
     val deviceName: String
     val broadcastService: PlatformBluetoothGattService
     val services: Array<PlatformBluetoothGattService>
     val broadcastTimeout: Long
 }