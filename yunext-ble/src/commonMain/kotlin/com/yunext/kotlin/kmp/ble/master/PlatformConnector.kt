package com.yunext.kotlin.kmp.ble.master

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

interface PlatformConnector {
    val status: StateFlow<PlatformConnectorStatus>

    fun connect()

    fun disconnect()

    fun enableNotify(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean,
        useCharacteristicDescriptor: Boolean = false
    ): Boolean

    fun enableIndicate(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean,
        useCharacteristicDescriptor: Boolean = false
    ): Boolean

    fun close()

    fun write(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        data: ByteArray
    ) :Boolean

    fun read(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
    ) :Boolean

}

sealed interface PlatformConnectorStatus {
    val device: PlatformBluetoothDevice

    data class Idle(override val device: PlatformBluetoothDevice) : PlatformConnectorStatus
    data class Connected(
        override val device: PlatformBluetoothDevice,
        val time: Long = Random.nextLong()
    ) : PlatformConnectorStatus

    data class ServiceDiscovered(
        override val device: PlatformBluetoothDevice,
        val services: List<PlatformBluetoothGattService>
    ) : PlatformConnectorStatus

    data class Disconnected(override val device: PlatformBluetoothDevice) : PlatformConnectorStatus
    data class Connecting(override val device: PlatformBluetoothDevice) : PlatformConnectorStatus
    data class Disconnecting(override val device: PlatformBluetoothDevice) : PlatformConnectorStatus
}