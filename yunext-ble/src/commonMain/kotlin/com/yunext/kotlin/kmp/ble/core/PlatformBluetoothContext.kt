package com.yunext.kotlin.kmp.ble.core

import kotlinx.coroutines.flow.StateFlow

interface PlatformBluetoothContext {

    val enable: StateFlow<Boolean>
    val location: StateFlow<Boolean>
    val permissions: StateFlow<Map<PlatformPermission,PlatformPermissionStatus>>

    fun enableBle()
    fun disableBle()
    fun enableLocation()
    fun disableLocation()

    fun requestPermission(permission: PlatformPermission)
    fun requestPermissions(vararg permission: PlatformPermission)
}

expect fun platformBluetoothContext(): PlatformBluetoothContext

enum class PlatformPermission {
    BluetoothAdvertise,
    BluetoothConnect,
    Location,
    BluetoothScan
    ;
}

enum class PlatformPermissionStatus {
    Granted,
    Defined
    ;
}