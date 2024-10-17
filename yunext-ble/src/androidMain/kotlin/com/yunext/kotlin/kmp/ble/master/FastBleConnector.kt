package com.yunext.kotlin.kmp.ble.master

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattService
import com.yunext.kotlin.kmp.ble.core.asPlatformBase
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.context.application
import com.yunext.kotlin.kmp.context.hdContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FastBleConnector(ctx: Context, private val device: BluetoothDevice) :
    PlatformConnector {
    private val fastBle: BleManager = BleManager.getInstance()

    private val _status: MutableStateFlow<PlatformConnectorStatus> = MutableStateFlow(
        PlatformConnectorStatus.Disconnected(device.asPlatformBase())
    )
    override val status: StateFlow<PlatformConnectorStatus> = _status.asStateFlow()

    private fun init(application: Application) {
        fastBle.splitWriteNum = 240
        fastBle.init(application)
        fastBle.setReConnectCount(0)
        fastBle.setOperateTimeout(30000)
    }

    init {
        init(hdContext.application)
    }

    @SuppressLint("MissingPermission")
    override fun connect() {
        d("connect")

        fastBle.connect(device.address, object : BleGattCallback() {
            val tag = "[BleGattCallback]"
            override fun onStartConnect() {
                d("${tag}onStartConnect")
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                d("${tag}onConnectFail bleDevice=${bleDevice?.name} ${bleDevice?.mac} exception=$exception")
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                d("${tag}onConnectSuccess bleDevice=${bleDevice?.name} ${bleDevice?.mac} gatt=$gatt status=$status")
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                d("${tag}onDisConnected bleDevice=${device?.name} ${device?.mac} gatt=$gatt status=$status isActiveDisConnected=$isActiveDisConnected")
            }

        })
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        d("disconnect")
        onStatusChanged(PlatformConnectorStatus.Disconnecting(device = device.asPlatformBase()))
        fastBle.disconnectAllDevice()
        onStatusChanged(PlatformConnectorStatus.Disconnected(device = device.asPlatformBase()))
    }

    override fun enableNotify(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean,
        useCharacteristicDescriptor: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun enableIndicate(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        enable: Boolean,
        useCharacteristicDescriptor: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }


    override fun close() {
        d("close")
        disconnect()
    }

    override fun write(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun read(
        service: PlatformBluetoothGattService,
        characteristic: PlatformBluetoothGattCharacteristic
    ): Boolean {
        TODO("Not yet implemented")
    }

    private fun onStatusChanged(status: PlatformConnectorStatus) {
        _status.value = status
    }


}