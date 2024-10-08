package com.yunext.kotlin.kmp.ble.slave

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.asPlatformBase

sealed interface AndroidSlaveEvent {
    val device: BluetoothDevice
}

fun AndroidSlaveEvent.asPlatform(): PlatformSlaveEvent {
    val device = this.device.asPlatformBase()
    return when (this) {
        is BleSlaveOnCharacteristicReadRequest -> PlatformBleSlaveOnCharacteristicReadRequest(
            device = device,
            requestId = this.requestId,
            offset = this.offset,
            characteristic = this.characteristic?.asPlatformBase()
        )

        is BleSlaveOnCharacteristicWriteRequest -> PlatformBleSlaveOnCharacteristicWriteRequest(
            device = device,
            requestId = this.requestId,
            characteristic = this.characteristic?.asPlatformBase(),
            preparedWrite = this.preparedWrite,
            responseNeeded = this.responseNeeded,
            offset = this.offset,
            value = this.value
        )

        is BleSlaveOnDescriptorReadRequest -> PlatformBleSlaveOnDescriptorReadRequest(
            device = device,
            requestId = this.requestId,
            offset = this.offset,
            descriptor = this.descriptor?.asPlatformBase()
        )

        is BleSlaveOnDescriptorWriteRequest -> PlatformBleSlaveOnDescriptorWriteRequest(
            device = device,
            requestId = this.requestId,
            descriptor = this.descriptor?.asPlatformBase(),
            preparedWrite = this.preparedWrite,
            responseNeeded = this.responseNeeded,
            offset = this.offset,
            value = this.value
        )

        is BleSlaveOnExecuteWrite -> PlatformBleSlaveOnExecuteWrite(
            device = device,
            requestId = this.requestId,
            execute = this.execute
        )

        is BleSlaveOnMtuChanged -> PlatformBleSlaveOnMtuChanged(
            device = device,
            mtu = this.mtu
        )

        is BleSlaveOnNotificationSent -> PlatformBleSlaveOnNotificationSent(
            device = device,
            status = this.status
        )

        is BleSlaveOnPhyRead -> PlatformBleSlaveOnPhyRead(
            device = device,
            txPhy = this.txPhy,
            rxPhy = this.rxPhy,
            status = this.status
        )

        is BleSlaveOnPhyUpdate -> PlatformBleSlaveOnPhyUpdate(
            device = device, txPhy = this.txPhy, rxPhy = this.rxPhy, status = this.status
        )
    }
}

//@Deprecated("见 BleSlaveConfigurationConnectedDevice")
//class BleSlaveOnConnectionStateChange(
//    override val device: BluetoothDevice,
//    val status: Int,
//    val connected: Boolean,
//) : AndroidSlaveEvent

class BleSlaveOnCharacteristicReadRequest(
    override val device: BluetoothDevice,
    val requestId: Int,
    val offset: Int,
    val characteristic: BluetoothGattCharacteristic?,
) : AndroidSlaveEvent

class BleSlaveOnExecuteWrite(
    override val device: BluetoothDevice,
    val requestId: Int,
    val execute: Boolean,
) : AndroidSlaveEvent

class BleSlaveOnDescriptorReadRequest(
    override val device: BluetoothDevice,
    val requestId: Int,
    val offset: Int,
    val descriptor: BluetoothGattDescriptor?,
) : AndroidSlaveEvent

class BleSlaveOnDescriptorWriteRequest(
    override val device: BluetoothDevice,
    val requestId: Int,
    val descriptor: BluetoothGattDescriptor?,
    val preparedWrite: Boolean,
    val responseNeeded: Boolean,
    val offset: Int,
    val value: ByteArray?,
) : AndroidSlaveEvent

class BleSlaveOnCharacteristicWriteRequest(
    override val device: BluetoothDevice,
    val requestId: Int,
    val characteristic: BluetoothGattCharacteristic?,
    val preparedWrite: Boolean,
    val responseNeeded: Boolean,
    val offset: Int,
    val value: ByteArray?,
) : AndroidSlaveEvent

class BleSlaveOnNotificationSent(
    override val device: BluetoothDevice,
    val status: Int,
) : AndroidSlaveEvent

class BleSlaveOnMtuChanged(
    override val device: BluetoothDevice,
    val mtu: Int,
) : AndroidSlaveEvent

class BleSlaveOnPhyRead(
    override val device: BluetoothDevice,
    val txPhy: Int,
    val rxPhy: Int,
    val status: Int,
) : AndroidSlaveEvent

class BleSlaveOnPhyUpdate(
    override val device: BluetoothDevice,
    val txPhy: Int,
    val rxPhy: Int,
    val status: Int,
) : AndroidSlaveEvent

//class BleSlaveOnServiceAdded(
//    override val device: BluetoothDevice,
//    service: BluetoothGattService,
//) : AndroidSlaveEvent