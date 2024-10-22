package com.yunext.kotlin.kmp.ble.master

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.yunext.kotlin.kmp.ble.history.BluetoothHistory
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryOwner
import com.yunext.kotlin.kmp.ble.history.bleIn
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.i
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class AndroidNativeScanner(
    ctx: Context, historyOwner: BluetoothHistoryOwner,
    private val filters: List<PlatformMasterScanFilter>
) :
    PlatformScanner, BluetoothHistoryOwner by historyOwner {
    private val context: Context = ctx.applicationContext
    private val _status: MutableStateFlow<PlatformMasterScanStatus> =
        MutableStateFlow(PlatformMasterScanStatus.ScanStopped)
    override val status: StateFlow<PlatformMasterScanStatus> = _status.asStateFlow()
    private val _scanResults: MutableStateFlow<List<AndroidMasterScanResult>> = MutableStateFlow(
        emptyList()
    )
    override val scanResults: StateFlow<List<AndroidMasterScanResult>> = _scanResults.asStateFlow()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private val scanner = adapter.bluetoothLeScanner
    private val scanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            d("onBatchScanResults results=${results?.size}")
            onScanningChanged(PlatformMasterScanStatus.ScanStopped)
            bleIn("onBatchScanResults:${results?.size}", tag = "scanCallback")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            d("onScanFailed errorCode=$errorCode")
            val s = if (errorCode == 0) {
                PlatformMasterScanStatus.Scanning(filters)
            } else PlatformMasterScanStatus.ScanStopped
            onScanningChanged(s)
            bleIn("onScanFailed:${errorCode}", tag = "scanCallback")
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
            val device = result.device
            val address = device.address
            val deviceName = device.name
            val rssi = result.rssi
            val data = result.scanRecord?.bytes ?: byteArrayOf()
            val scanResult = AndroidMasterScanResult(
                address = address,
                deviceName = deviceName,
                rssi = rssi,
                data = data, device = result.device
            )
            val check = doFilter(scanResult)
            d("onScanResult check=$check callbackType=${callbackType} address=${address} deviceName=${deviceName} rssi=$rssi")
            if (check) {
                bleIn("onScanResult:${deviceName} $address $rssi", tag = "scanCallback")
                onScanResult(scanResult)
            }
        }
    }

    private fun doFilter(result: AndroidMasterScanResult): Boolean {
        filters.forEach {
            val r = it.check(result)
            if (!r) return false
        }
        return true
    }


    @SuppressLint("MissingPermission")
    private val leScanCallback =
        BluetoothAdapter.LeScanCallback { d, rssi, scanRecord ->

            d("onLeScanaddress=${d?.address ?: ""} deviceName=${d?.name ?: ""} rssi=$rssi")
            val device = d ?: return@LeScanCallback
            val address = device.address
            val deviceName = device.name
            val data = scanRecord ?: byteArrayOf()
            val scanResult = AndroidMasterScanResult(
                address = address,
                deviceName = deviceName,
                rssi = rssi,
                data = data, device = device
            )
            val check = doFilter(scanResult)
            d("onLeScan check=$check address=${address} deviceName=${deviceName} rssi=$rssi")
            if (check) {
                onScanResult(scanResult)
            }
        }

    private var startScanJob: Job? = null

    @SuppressLint("MissingPermission")
    fun startScan() {
        startScanJob?.cancel()
        _scanResults.value = emptyList()
        val code = Build.VERSION.SDK_INT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            i("startScan 1 @$code")
            val filters = listOf(
                ScanFilter.Builder()
                    .build()
            )
            val setting = ScanSettings.Builder()
                .setLegacy(true)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                .build()
            scanner.startScan(filters, setting, scanCallback)
        } else {
            i("startScan 2 @$code")
            adapter.startLeScan(leScanCallback)
        }

        onScanningChanged(PlatformMasterScanStatus.Scanning(filters))
        startScanJob = masterScope.launch {
            delay(30000L)
            stopScan()
        }
    }

    private fun onScanningChanged(status: PlatformMasterScanStatus) {
        this._status.value = status
    }

    private fun onScanResult(scanResult: AndroidMasterScanResult) {
        val oldList = this.scanResults.value
        val newList = oldList.filter { it.address != scanResult.address } + scanResult
        this._scanResults.value = newList
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        startScanJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            scanner.stopScan(scanCallback)
        } else {
            adapter.stopLeScan(leScanCallback)
        }
        onScanningChanged(PlatformMasterScanStatus.ScanStopped)
    }

    fun close() {
        stopScan()
        startScanJob?.cancel()
        _scanResults.value = emptyList()
        _status.value = PlatformMasterScanStatus.ScanStopped
    }


}