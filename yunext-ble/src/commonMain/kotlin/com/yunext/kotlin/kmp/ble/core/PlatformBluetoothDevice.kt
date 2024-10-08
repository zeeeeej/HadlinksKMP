package com.yunext.kotlin.kmp.ble.core

interface PlatformBluetoothDevice {
    val name: String
    val address: String

}

expect fun bluetoothDevice(name:String, address:String):PlatformBluetoothDevice

