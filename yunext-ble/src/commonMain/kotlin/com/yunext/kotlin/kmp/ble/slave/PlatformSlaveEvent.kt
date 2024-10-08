package com.yunext.kotlin.kmp.ble.slave

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattDescriptor
import kotlin.uuid.ExperimentalUuidApi

sealed interface PlatformSlaveEvent{
    val device:PlatformBluetoothDevice
}

//@Deprecated("见 BleSlaveConfigurationConnectedDevice")
//class PlatformBleSlaveOnConnectionStateChange(
//    override val device: BluetoothDevice,
//    val status: Int,
//    val connected: Boolean,
//) : PlatformSlaveEvent

class PlatformBleSlaveOnCharacteristicReadRequest(
    override val device: PlatformBluetoothDevice,
    val requestId: Int,
    val offset: Int,
    val characteristic: PlatformBluetoothGattCharacteristic?,
) : PlatformSlaveEvent{
    override fun toString(): String {
        @OptIn(ExperimentalUuidApi::class)
        return "[OnCharacteristicReadRequest]characteristic=${characteristic?.uuid?.toString()}"
    }
}

class PlatformBleSlaveOnExecuteWrite(
    override val device: PlatformBluetoothDevice,
    val requestId: Int,
    val execute: Boolean,
) : PlatformSlaveEvent{
    override fun toString(): String {
        return "[OnExecuteWrite]execute=${execute}"
    }
}

class PlatformBleSlaveOnDescriptorReadRequest(
    override val device: PlatformBluetoothDevice,
    val requestId: Int,
    val offset: Int,
    val descriptor: PlatformBluetoothGattDescriptor?,
) : PlatformSlaveEvent{
    override fun toString(): String {
        @OptIn(ExperimentalUuidApi::class)
        return "[OnDescriptorReadRequest]descriptor=${descriptor?.uuid?.toString()}"
    }
}

class PlatformBleSlaveOnDescriptorWriteRequest(
    override val device: PlatformBluetoothDevice,
    val requestId: Int,
    val descriptor: PlatformBluetoothGattDescriptor?,
    val preparedWrite: Boolean,
    val responseNeeded: Boolean,
    val offset: Int,
    val value: ByteArray?,
) : PlatformSlaveEvent{

    override fun toString(): String {
        @OptIn(ExperimentalStdlibApi::class)
        val v = value?.toHexString()
        @OptIn(ExperimentalUuidApi::class)
        return "[OnDescriptorWriteRequest]descriptor=${descriptor?.uuid?.toString()} preparedWrite=${preparedWrite} responseNeeded=${responseNeeded} value=[${v}]"
    }
}

class PlatformBleSlaveOnCharacteristicWriteRequest(
    override val device: PlatformBluetoothDevice,
    val requestId: Int,
    val characteristic: PlatformBluetoothGattCharacteristic?,
    val preparedWrite: Boolean,
    val responseNeeded: Boolean,
    val offset: Int,
    val value: ByteArray?,
) : PlatformSlaveEvent{
    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        @OptIn(ExperimentalStdlibApi::class)
        val v = value?.toHexString()
        @OptIn(ExperimentalUuidApi::class)
        return "[OnCharacteristicWriteRequest]descriptor=${characteristic?.uuid?.toString()} preparedWrite=${preparedWrite} responseNeeded=${responseNeeded} value=[$v]"
    }
}

class PlatformBleSlaveOnNotificationSent(
    override val device: PlatformBluetoothDevice,
    val status: Int,
) : PlatformSlaveEvent{
    override fun toString(): String {
        return "[OnNotificationSent]status=${status}"
    }
}

class PlatformBleSlaveOnMtuChanged(
    override val device: PlatformBluetoothDevice,
    val mtu: Int,
) : PlatformSlaveEvent{
    override fun toString(): String {
        return "[OnMtuChanged]mtu=${mtu}"
    }
}

class PlatformBleSlaveOnPhyRead(
    override val device: PlatformBluetoothDevice,
   val  txPhy: Int,
   val  rxPhy: Int,
   val  status: Int,
) : PlatformSlaveEvent{
    override fun toString(): String {
        return "[OnPhyRead]txPhy=${txPhy} rxPhy=${rxPhy} status=${status}"
    }
}

class PlatformBleSlaveOnPhyUpdate(
    override val device: PlatformBluetoothDevice,
    val txPhy: Int,
    val rxPhy: Int,
    val status: Int,
) : PlatformSlaveEvent{
    override fun toString(): String {
        return "[OnPhyUpdate]txPhy=${txPhy} rxPhy=${rxPhy} status=${status}"
    }
}

//class PlatformBleSlaveOnServiceAdded(
//    override val device: BluetoothDevice,
//    service: BluetoothGattService,
//) : PlatformSlaveEvent