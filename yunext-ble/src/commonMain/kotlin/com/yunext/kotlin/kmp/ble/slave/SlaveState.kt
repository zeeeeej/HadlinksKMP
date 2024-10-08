package com.yunext.kotlin.kmp.ble.slave

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice

sealed interface SlaveState {
    val setting: SlaveSetting

    data class Idle(override val setting: SlaveSetting) : SlaveState
    data class AdvertiseSuccess(override val setting: SlaveSetting) : SlaveState
    data class ServerOpened(override val setting: SlaveSetting) : SlaveState
    data class Connected(override val setting: SlaveSetting, val device: PlatformBluetoothDevice) :
        SlaveState
}

