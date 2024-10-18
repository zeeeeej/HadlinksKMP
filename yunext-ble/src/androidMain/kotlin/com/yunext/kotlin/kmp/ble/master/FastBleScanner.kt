package com.yunext.kotlin.kmp.ble.master

import android.app.Application
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.scan.BleScanRuleConfig
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.context.application
import com.yunext.kotlin.kmp.context.hdContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FastBleScanner(  private val filters: List<PlatformMasterScanFilter> = listOf(
    DeviceNamePlatformMasterScanFilter(
        "angel_"
    )
)) :
    PlatformScanner {
    private val fastBle: BleManager = BleManager.getInstance()

    private val _status: MutableStateFlow<PlatformMasterScanStatus> =
        MutableStateFlow(PlatformMasterScanStatus.ScanStopped)
    override val status: StateFlow<PlatformMasterScanStatus> = _status.asStateFlow()
    private val _scanResults: MutableStateFlow<List<AndroidMasterScanResult>> = MutableStateFlow(
        emptyList()
    )
    override val scanResults: StateFlow<List<AndroidMasterScanResult>> = _scanResults.asStateFlow()

    fun init(application: Application) {
        fastBle.splitWriteNum = 240
        fastBle.init(application)
        fastBle.setReConnectCount(0)
        fastBle.setOperateTimeout(30000)
    }

    init {
        init(hdContext.application)
    }

    fun startScan(filter: PlatformMasterScanFilter = DeviceNamePlatformMasterScanFilter("angel_")) {
        _scanResults.value = emptyList()
        fastBle.initScanRule(
            BleScanRuleConfig.Builder()
                .setScanTimeOut(15000)
                //.setServiceUuids(arrayOf(HBle.adUUID()))
                .also {
                    if (filter is DeviceNamePlatformMasterScanFilter) {
                        it.setDeviceName(true, filter.deviceName)
                    }
                }
                .build()
        )
        val bleScanCallback = object : BleScanCallback() {

            override fun onScanStarted(success: Boolean) {
                d("[startScan]onScanStarted success:$success")
                onScanningChanged(
                    if (success) {
                        PlatformMasterScanStatus.Scanning(filters)
                    } else {
                        PlatformMasterScanStatus.ScanStopped
                    }
                )
            }

            override fun onScanning(bleDevice: com.clj.fastble.data.BleDevice?) {
                val d = bleDevice ?: return
                val deviceName = d.name ?: ""
                d("[startScan]onScanning bleDevice:${d.mac}($deviceName) *${d.rssi}")
                if (deviceName.isEmpty()) return
                val scanResult = AndroidMasterScanResult(
                    deviceName = deviceName,
                    address = bleDevice.mac,
                    rssi = bleDevice.rssi,
                    data = bleDevice.scanRecord,
                    device = d.device

                )
                val check = filter.check(scanResult)
                if (check) {
                    onScanResult(scanResult)
                }

            }


            override fun onScanFinished(scanResultList: MutableList<com.clj.fastble.data.BleDevice>?) {
                d("[startScan]onScanFinished scanResultList:$scanResultList")
                onScanningChanged(PlatformMasterScanStatus.ScanStopped)
            }

        }
        fastBle.scan(bleScanCallback)
    }

    private fun onScanningChanged(status: PlatformMasterScanStatus) {
        this._status.value = status
    }

    private fun onScanResult(scanResult: AndroidMasterScanResult) {
        val oldList = this.scanResults.value
        val newList = oldList.filter { it.address != scanResult.address } + scanResult
        this._scanResults.value = newList
    }

    fun stopScan(){
        fastBle.cancelScan()
    }

    fun close(){
        _scanResults.value = emptyList()
        _status.value = PlatformMasterScanStatus.ScanStopped
    }


}