package com.yunext.kotlin.kmp.ble.slave

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetCallback
import android.content.Context
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.i
import com.yunext.kotlin.kmp.ble.util.w
import java.lang.IllegalStateException

internal sealed interface AdvertiserEvent{
    data object OnSuccess:AdvertiserEvent
    data class OnFail(val error:String):AdvertiserEvent
}

internal class AndroidPlatformAdvertiser(context: Context, private val callback: (AdvertiserEvent) -> Unit) :
    PlatformBroadcaster {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private val bluetoothLeAdvertiser = adapter.bluetoothLeAdvertiser
        ?: throw IllegalStateException("bluetoothLeAdvertiser is null")
    private var advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            d("[AndroidPlatformAdvertiser]AdvertiseCallback onStartSuccess settingsInEffect:$settingsInEffect ")
            callback.invoke(AdvertiserEvent.OnSuccess)
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val msg = errorCode.msg()
            w("[AndroidPlatformAdvertiser]AdvertiseCallback onStartFailure $errorCode:${msg} ")
            callback.invoke(AdvertiserEvent.OnFail(msg))
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

    @SuppressLint("MissingPermission")
    fun start(
        deviceName: String,
        broadcastService: PlatformBluetoothGattService,
        timeout: Long,
    ) {
        d("[AndroidPlatformAdvertiser]start dest:$deviceName,broadcastService:$broadcastService")

        val advertiseSettings =
            AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setTimeout(timeout.toInt())
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
//                .addServiceUuid(ParcelUuid(serviceUUID))
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

}