package com.yunext.kotlin.kmp.ble.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.clj.fastble.utils.BleLog
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.asPlatformBase
import com.yunext.kotlin.kmp.ble.core.status
import kotlin.uuid.ExperimentalUuidApi

val BluetoothDevice.display: String
    @SuppressLint("MissingPermission")
    get() = "${this.address}[${this.name?:""}]"

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


@OptIn(ExperimentalStdlibApi::class)
val BluetoothGattDescriptor.display: String
    get() = """
        |${this.uuid.toString()} p:${this.permissions} v:${this.value?.toHexString()} s:${this.asPlatformBase().status})
    """.trimMargin()


@Synchronized
internal fun BluetoothGatt?.refreshDeviceCache() {
    try {

        val refresh = BluetoothGatt::class.java.getMethod("refresh")
        if (refresh != null && this != null) {
            val success = refresh.invoke(this) as Boolean
            d("refreshDeviceCache, is success:  $success")
        }
    } catch (e: Exception) {
        e("exception occur while refreshing device: " + e.message)
        e.printStackTrace()
    }
}

fun BluetoothGatt.findService(uuid: String): BluetoothGattService? {
    val services = this.services
    if (services.isEmpty()) return null
    return services.singleOrNull() {
        it.uuid.toString() == uuid
    }
}

fun BluetoothGattService.findCharacteristic(uuid: String): BluetoothGattCharacteristic? {
    val chs = this.characteristics
    if (chs.isEmpty()) return null
    return chs.singleOrNull {
        it.uuid.toString() == uuid
    }
}

fun BluetoothGattCharacteristic.findDescriptor(uuid: String): BluetoothGattDescriptor? {
    val descriptors = this.descriptors
    if (descriptors.isEmpty()) return null
    return descriptors.singleOrNull {
        it.uuid.toString() == uuid
    }
}