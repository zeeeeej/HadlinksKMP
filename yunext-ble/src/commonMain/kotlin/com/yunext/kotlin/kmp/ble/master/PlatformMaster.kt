package com.yunext.kotlin.kmp.ble.master

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothDevice
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryOwner
import kotlinx.coroutines.flow.StateFlow

interface PlatformMaster:PlatformScanner {

    val connectStatusMap: StateFlow<Map<String, PlatformConnectorStatus>>
    val connectServicesMap: StateFlow<Map<String, List<PlatformBluetoothGattService>>>
    val historyOwner: BluetoothHistoryOwner


    fun startScan()

    fun stopScan()

    fun connect(device: PlatformBluetoothDevice): Boolean

    fun disconnect(device: PlatformBluetoothDevice): Boolean

    fun enableNotify(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable:Boolean
    ): Boolean

    fun enableIndicate(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable:Boolean
    ): Boolean

    fun read(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic
    ): Boolean

    fun write(
        device: PlatformBluetoothDevice,
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        data: ByteArray,
    ): Boolean

    fun close()

}


expect fun PlatformMaster(
    context: PlatformBluetoothContext,
): PlatformMaster