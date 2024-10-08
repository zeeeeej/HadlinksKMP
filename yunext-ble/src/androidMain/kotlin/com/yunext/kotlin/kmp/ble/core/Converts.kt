package com.yunext.kotlin.kmp.ble.core

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Build
import com.yunext.kotlin.kmp.ble.util.toUUID
import com.yunext.kotlin.kmp.ble.util.toUuid
import kotlin.uuid.ExperimentalUuidApi

//<editor-fold desc="/* native to platform */">


@SuppressLint("MissingPermission")
internal fun BluetoothDevice.asPlatformBase(): PlatformBluetoothDevice {
    @OptIn(ExperimentalStdlibApi::class)
    val name: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.name ?: this.alias ?: this.bluetoothClass?.deviceClass?.toHexString()
        ?: this.uuids?.joinToString { it.toString() }
    } else {
        this.name ?: this.bluetoothClass?.deviceClass?.toHexString()
        ?: this.uuids?.joinToString { it.toString() }
    }
    val address = this.address
    return bluetoothDevice(name ?: "", address)
}

@OptIn(ExperimentalUuidApi::class)
internal fun BluetoothGattService.asPlatformBase(): PlatformBluetoothGattService {
    val uuid = this.uuid.toUuid()
    val serviceType = when (val type = this.type.toUInt()) {
        PlatformBluetoothGattService.ServiceType.Primary.value -> PlatformBluetoothGattService.ServiceType.Primary
        PlatformBluetoothGattService.ServiceType.Secondary.value -> PlatformBluetoothGattService.ServiceType.Secondary
        else -> throw IllegalArgumentException("错误的type $type")
    }
    val includedServices = this.includedServices.map {
        it.asPlatformBase()
    }
    val characteristics = this.characteristics.map {
        it.asPlatformBase()
    }

    return bluetoothGattService(
        uuid = uuid,
        serviceType = serviceType,
        includeServices = includedServices.toTypedArray(),
        characteristics = characteristics.toTypedArray()
    )
}

@OptIn(ExperimentalUuidApi::class)
internal fun BluetoothGattCharacteristic.asPlatformBase(): PlatformBluetoothGattCharacteristic {
    val uuid = this.uuid.toUuid()
    val permissions = characteristicPermissionOf(this.permissions)
    val properties = characteristicPropertyOf(this.permissions)
    val descriptors = this.descriptors.map {
        it.asPlatformBase()
    }
    val value = this.value
    return bluetoothGattCharacteristic(
        uuid = uuid,
        permissions = permissions,
        properties = properties,
        descriptors = descriptors.toTypedArray(),
        value = value
    )
}

private fun characteristicPermissionOf(value: Int): Array<PlatformBluetoothGattCharacteristic.Permission> {
    val list = PlatformBluetoothGattCharacteristic.Permission.entries.filter {
        (value and it.value.toInt()) != 0
    }
    return list.toTypedArray()
}

private fun characteristicPropertyOf(value: Int): Array<PlatformBluetoothGattCharacteristic.Property> {
    //
    // Read ：0x02
    // WriteWithoutResponse ：0x04
    // Write ：0x08
    // Notify ：0x10
    // Indicate ：0x20
    // 14  N W  WN  R
    // 000 0 1  1   1 0
    // 18
    // 000 1 0  0   1 0
    // 30
    // 000 1 1  1   1 0
//    val list = mutableListOf<PlatformBluetoothGattCharacteristic.Property>()
//    if ((value and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
//        list.add(PlatformBluetoothGattCharacteristic.Property.Notify)
//    }
//    if ((value and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
//        list.add(PlatformBluetoothGattCharacteristic.Property.Read)
//    }
//    if ((value and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
//        list.add(PlatformBluetoothGattCharacteristic.Property.Write)
//    }
//    if ((value and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
//        list.add(PlatformBluetoothGattCharacteristic.Property.WriteNoResponse)
//    }
//    if ((value and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
//        list.add(PlatformBluetoothGattCharacteristic.Property.Indicate)
//    }
    val list = PlatformBluetoothGattCharacteristic.Property.entries.filter {
        (value and it.value.toInt()) != 0
    }
    return list.toTypedArray()
}


@OptIn(ExperimentalUuidApi::class)
internal fun BluetoothGattDescriptor.asPlatformBase(): PlatformBluetoothGattDescriptor {
    val uuid = this.uuid.toUuid()
    val permissions = descriptorPermissionOf(this.permissions)
    val value = this.value
    return bluetoothGattDescriptor(
        uuid = uuid,
        permissions = permissions,
        value = value
    )
}

private fun descriptorPermissionOf(value: Int): Array<PlatformBluetoothGattDescriptor.Permission> {
    val list = PlatformBluetoothGattDescriptor.Permission.entries.filter {
        (value and it.value.toInt()) != 0
    }
    return list.toTypedArray()
}
//</editor-fold>

//<editor-fold desc="/* platform to native */">
@OptIn(ExperimentalUuidApi::class)
internal fun PlatformBluetoothGattService.asNativeBase(): BluetoothGattService {
    val uuid = this.uuid.toUUID()
    val serviceType = this.serviceType.value.toInt()
    val characteristics = this.characteristics.map {
        it.asNativeBase()
    }
    val service = BluetoothGattService(uuid, serviceType).apply {
        characteristics.forEach {
            this.addCharacteristic(it)
        }
    }
    return service
}

@OptIn(ExperimentalUuidApi::class)
internal fun PlatformBluetoothGattCharacteristic.asNativeBase(): BluetoothGattCharacteristic {
    val uuid = this.uuid.toUUID()
    val properties = characteristicPropertyNativeOf(this.properties)
    val permissions = characteristicPermissionNativeOf(this.permissions)
    val descriptors = descriptors.map {
        it.asNativeBase()
    }
    return BluetoothGattCharacteristic(uuid, properties, permissions).apply {
        descriptors.forEach {
            this.addDescriptor(it)
        }
    }
}

private fun characteristicPermissionNativeOf(permissions: Array<PlatformBluetoothGattCharacteristic.Permission>): Int {
    return permissions.fold(0x00u) { acc, permission ->
        acc or permission.value
    }.toInt()
}

private fun characteristicPropertyNativeOf(properties: Array<PlatformBluetoothGattCharacteristic.Property>): Int {
    return properties.fold(0x00u) { acc, property ->
        acc or property.value
    }.toInt()
}

@OptIn(ExperimentalUuidApi::class)
internal fun PlatformBluetoothGattDescriptor.asNativeBase(): BluetoothGattDescriptor {
    val uuid = this.uuid.toUUID()
    val permissions = descriptorPermissionNativeOf(this.permissions)
    return BluetoothGattDescriptor(uuid, permissions)
}

private fun descriptorPermissionNativeOf(permissions: Array<PlatformBluetoothGattDescriptor.Permission>): Int {
    return permissions.fold(0x00u) { acc, permission ->
        acc or permission.value
    }.toInt()
}
//</editor-fold>





