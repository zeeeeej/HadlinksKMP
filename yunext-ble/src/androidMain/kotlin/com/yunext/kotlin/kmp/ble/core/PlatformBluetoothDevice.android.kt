package com.yunext.kotlin.kmp.ble.core

actual fun bluetoothDevice(
    name: String,
    address: String,
): PlatformBluetoothDevice {
    return AndroidBluetoothDevice(name =name,address = address)
}

private class AndroidBluetoothDevice(override val name: String, override val address: String) :
    PlatformBluetoothDevice {

}