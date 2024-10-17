package com.yunext.kotlin.kmp.ble.core

interface PlatformBluetoothDevice {
    val name: String
    val address: String

}

val PlatformBluetoothDevice.display:String
    get() = "$name($address)"

expect fun bluetoothDevice(name:String, address:String):PlatformBluetoothDevice

