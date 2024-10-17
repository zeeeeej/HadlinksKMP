package com.yunext.kotlin.kmp.ble.master

import android.bluetooth.BluetoothDevice

class AndroidMasterScanResult(
    override val deviceName: String?,
    override val address: String?,
    override val rssi: Int,
    override val data: ByteArray,
    val device:BluetoothDevice
) :PlatformMasterScanResult {
}