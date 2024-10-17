package com.yunext.kotlin.kmp.ble.slave

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService

sealed interface SlaveState {
    val setting: SlaveSetting

    data class Idle(override val setting: SlaveSetting) : SlaveState
    data class AdvertiseSuccess(override val setting: SlaveSetting) : SlaveState
    data class ServerOpened(override val setting: SlaveSetting,   val services: List<PlatformBluetoothGattService> = emptyList()) : SlaveState
    data class Connected(
        override val setting: SlaveSetting, val device: PlatformBluetoothDevice,
        val services: List<PlatformBluetoothGattService> = emptyList()
    ) :
        SlaveState
}

