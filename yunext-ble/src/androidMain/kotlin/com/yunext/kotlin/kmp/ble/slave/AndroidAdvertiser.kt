package com.yunext.kotlin.kmp.ble.slave

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetCallback
import android.content.Context
import android.os.ParcelUuid
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.master.masterScope
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.w
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import kotlin.uuid.ExperimentalUuidApi

internal sealed interface AdvertiserEvent {
    data object OnSuccess : AdvertiserEvent
    data class OnFail(val error: String) : AdvertiserEvent
}

internal class AndroidPlatformAdvertiser(
    context: Context,
    private val callback: (AdvertiserEvent) -> Unit
) :
    PlatformAdvertiser {

    private val hasRetry: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var hasRetryJob: Job? = null
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private val bluetoothLeAdvertiser = adapter.bluetoothLeAdvertiser
        ?: throw IllegalStateException("bluetoothLeAdvertiser is null")


    private fun doTask(task: () -> Unit) {
        masterScope.launch() {
            task()
        }
    }

    private var advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            doTask{
                d("[AndroidPlatformAdvertiser]AdvertiseCallback onStartSuccess settingsInEffect:$settingsInEffect ")
                hasRetryJob?.cancel()
                hasRetryJob = null
                hasRetryJob= masterScope.launch {
                    if (hasRetry.value){
                        w("直接广播成功了")
                        callback.invoke(AdvertiserEvent.OnSuccess)
                    }else{
                        w("重新广播")
                        stop()
                        val (deviceName,broadcastService,broadcastTimeout) = tmp?:return@launch
                        hasRetry.value = true
                        delay(500)
                        startInternal( deviceName, broadcastService, broadcastTimeout *2L )
                    }
                }
            }


        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            doTask {
                val msg = errorCode.msg()
                w("[AndroidPlatformAdvertiser]AdvertiseCallback onStartFailure $errorCode:${msg} ")
                callback.invoke(AdvertiserEvent.OnFail(msg))
            }
        }

        private fun Int.msg(): String {
            return when (this) {
                AdvertisingSetCallback.ADVERTISE_SUCCESS -> "ADVERTISE_SUCCESS"
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                else -> "未知"
            }
        }


    }

    private var tmp:Triple<String,PlatformBluetoothGattService,Long> ? = null

    @SuppressLint("MissingPermission")
    fun start(
        deviceName: String,
        broadcastService: PlatformBluetoothGattService,
        timeout: Long,
    ) {
        d("[AndroidPlatformAdvertiser]start dest:$deviceName,broadcastService:$broadcastService")
        hasRetry.value = false
        hasRetryJob?.cancel()
        hasRetryJob = null
        tmp = Triple(deviceName,broadcastService,timeout)
        startInternal(deviceName, broadcastService, timeout)
    }

    @OptIn(ExperimentalUuidApi::class)
    @SuppressLint("MissingPermission")
    private fun startInternal(
        deviceName: String,
        broadcastService: PlatformBluetoothGattService,
        timeout: Long,
    ) {
        adapter.setName(deviceName)
        val advertiseSettings =
            AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setTimeout(Math.min(180000,timeout.toInt()))
                .setConnectable(true)
                .build()
        val advertiseData =
            AdvertiseData.Builder()
                .setIncludeDeviceName(true)
//                .setIncludeTxPowerLevel(true)
//                .addManufacturerData(
//                    0x09,
//                    payload
//                )
//                .addManufacturerData(
//                    0x08,
//                    "xpl".toByteArray()
//                )
//                .addManufacturerData(
//                    0x0A,
//                    byteArrayOf(0x01)
//                )
                .addServiceUuid(ParcelUuid.fromString(broadcastService.uuid.toString()))
                .build()
        val scanResponseData = null
        bluetoothLeAdvertiser.startAdvertising(
            advertiseSettings,
            advertiseData,
            scanResponseData,
            advertiseCallback
        )
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        d("[AndroidPlatformAdvertiser]stop")
        try {
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        } catch (e: Exception) {
            w("[AndroidPlatformAdvertiser]stopBroadcast $e")
        } finally {
        }
    }

    fun close(){
        stop()
        hasRetryJob?.cancel()
        hasRetryJob = null
        hasRetry.value = false
    }

}