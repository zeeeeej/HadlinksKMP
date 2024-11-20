@file:OptIn(ExperimentalUuidApi::class)

package com.yunext.kotlin.kmp.ble.util

import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattDescriptor
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import kotlin.uuid.ExperimentalUuidApi

val List<PlatformBluetoothGattService>.display:String
    get() = this.joinToString("") {
        it.display
    }

val PlatformBluetoothGattService.display: String
    get() = """
        |${this.uuid.toString()} (${this.characteristics.size}个) <<${this.serviceType.name}>>
        |${this.characteristics.joinToString("\n") { it.display }}
        |
    """.trimMargin()

@OptIn(ExperimentalStdlibApi::class)
val PlatformBluetoothGattCharacteristic.display: String
    get() = """
        |   ${this.uuid.toString()} ${this.permissions.display}  ${this.properties.display} (${this.descriptors.size}个)
        |   ${this.descriptors.joinToString { it.display }}
        |   ${this.value.toString()} ${this.value?.toHexString()})
    """.trimMargin()

@OptIn(ExperimentalStdlibApi::class)
val PlatformBluetoothGattDescriptor.display: String
    get() = """
        |   ${this.uuid.toString()} ${this.permissions.display})
        |   ${this.value.toString()} ${this.value?.toHexString()})
    """.trimMargin()

val Array<PlatformBluetoothGattCharacteristic.Permission>.display:String
    get() = "[${this.joinToString { it.name }}]"

val Array<PlatformBluetoothGattCharacteristic.Property>.display:String
    get() = "[${this.joinToString { it.name }}]"

val Array<PlatformBluetoothGattDescriptor.Permission>.display:String
    get() = "[${this.joinToString { it.name }}]"
