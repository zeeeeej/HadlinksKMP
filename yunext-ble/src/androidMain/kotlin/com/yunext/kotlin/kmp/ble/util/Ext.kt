package com.yunext.kotlin.kmp.ble.util

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

val List<BluetoothGattService>.display: String
    get() = this.joinToString("") {
        it.display
    }

val BluetoothGattService.display: String
    get() = """
        |${this.uuid.toString()} (${this.characteristics.size}个) <<${this.type}>>
        |   ${this.characteristics.joinToString("\n") { it.display }}
    """.trimMargin()

val BluetoothGattCharacteristic.display: String
    get() = """
        |${this.uuid.toString()} ${this.permissions}  ${this.properties} (${this.descriptors.size}个)
        |   ${this.descriptors.joinToString { it.display }}
    """.trimMargin()

val BluetoothGattDescriptor.display: String
    get() = """
        |${this.uuid.toString()} ${this.permissions})
    """.trimMargin()