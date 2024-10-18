package com.yunext.kotlin.kmp.ble.master

import kotlinx.coroutines.flow.StateFlow

interface PlatformScanner {
    val status: StateFlow<PlatformMasterScanStatus>
    val scanResults: StateFlow<List<PlatformMasterScanResult>>
}

sealed interface PlatformMasterScanStatus {

    data object ScanStopped : PlatformMasterScanStatus

    data class Scanning(val filter: List<PlatformMasterScanFilter>) : PlatformMasterScanStatus
}

interface PlatformMasterScanResult {
    val deviceName: String?
    val address: String?
    val rssi: Int
    val data: ByteArray
}

data class DefaultPlatformMasterScanResult(
    override val deviceName: String?,
    override val address: String?,
    override val rssi: Int,
    override val data: ByteArray
) : PlatformMasterScanResult {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PlatformMasterScanResult

        if (deviceName != other.deviceName) return false
        if (address != other.address) return false
        if (rssi != other.rssi) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceName?.hashCode() ?: 0
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (rssi?.hashCode() ?: 0)
        result = 31 * result + data.contentHashCode()
        return result
    }
}

interface PlatformMasterScanFilter {
    fun check(result: PlatformMasterScanResult): Boolean
}

class DeviceNamePlatformMasterScanFilter(val deviceName: String) : PlatformMasterScanFilter {
    override fun check(result: PlatformMasterScanResult): Boolean {
        return result.deviceName?.contains(deviceName) ?: false
    }

}