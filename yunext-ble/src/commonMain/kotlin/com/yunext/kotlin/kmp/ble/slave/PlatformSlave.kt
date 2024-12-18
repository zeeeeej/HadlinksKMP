package com.yunext.kotlin.kmp.ble.slave

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.history.BluetoothHistory
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

interface PlatformSlave{
    val eventChannel: Channel<PlatformSlaveEvent>
    val slaveState: StateFlow<SlaveState>
    val history: BluetoothHistoryOwner
    fun updateSetting(setting: SlaveSetting)
    fun startBroadcast()
    fun stopBroadcast()
    fun response(response: PlatformResponse, success: Boolean)
    fun notify(param: SlaveWriteParam)
    fun close()
}

expect fun PlatformSlave(
    context: PlatformBluetoothContext,
    setting: SlaveSetting
): PlatformSlave

data class SlaveWriteParam(
    val characteristic: PlatformBluetoothGattCharacteristic,
    val value: ByteArray,
    val confirm: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SlaveWriteParam

        if (characteristic != other.characteristic) return false
        if (!value.contentEquals(other.value)) return false
        if (confirm != other.confirm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = characteristic.hashCode()
        result = 31 * result + value.contentHashCode()
        result = 31 * result + confirm.hashCode()
        return result
    }
}