package com.yunext.kotlin.kmp.ble.slave

import android.bluetooth.BluetoothManager
import android.content.Context
import com.yunext.kotlin.kmp.ble.core.AndroidBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothContext
import com.yunext.kotlin.kmp.ble.core.asNativeBase
import com.yunext.kotlin.kmp.ble.core.asPlatformBase
import com.yunext.kotlin.kmp.ble.core.platformBluetoothContext
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.i
import com.yunext.kotlin.kmp.ble.util.w
import com.yunext.kotlin.kmp.context.application
import com.yunext.kotlin.kmp.context.hdContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

actual fun PlatformSlave(
    context: PlatformBluetoothContext,
    setting: SlaveSetting,
): PlatformSlave {
    return AndroidPlatformSlave(context as AndroidBluetoothContext, setting)
}

private val slaveScope by lazy {
    CoroutineScope(Dispatchers.Main.immediate + SupervisorJob() + CoroutineName("slaveScope"))
}

internal class AndroidPlatformSlave(
    private val platformContext: AndroidBluetoothContext,
    setting: SlaveSetting,
) : PlatformSlave {
    private val context: Context =
        (platformContext).context.applicationContext
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private var setting: SlaveSetting

    init {
        this.setting = setting
    }

    private var advertiser: AndroidPlatformAdvertiser? = null
    private var server: AndroidPlatformServer? = null
    private var delayReBroadcast: Job? = null

    private val _eventChannel: Channel<PlatformSlaveEvent> = Channel()
    override val eventChannel: Channel<PlatformSlaveEvent>
        get() = _eventChannel
    private val _slaveState: MutableStateFlow<SlaveState> =
        MutableStateFlow(SlaveState.Idle(setting))
    override val slaveState: StateFlow<SlaveState> = _slaveState.asStateFlow()

    init {
        onStateChanged(SlaveState.Idle(setting), "init")
        autoBroadcastDelay("init")
    }

    override fun updateSetting(setting: SlaveSetting) {
        i("[PlatformSlave]updateSetting")
        stopBroadcast()
        this.setting = setting
        onStateChanged(SlaveState.Idle(setting), "updateSetting")
        autoBroadcastDelay("updateSetting")
    }

    private var startBroadcastTimeoutJob: Job? = null
    override fun startBroadcast() {
        i("[PlatformSlave]startBroadcast")
        startBroadcastTimeoutJob?.cancel()
        startBroadcastTimeoutJob = null
        server?.stop()
        advertiser?.stop()
        delayReBroadcast?.cancel()
        if (!platformContext.enable.value) {
            w("[PlatformSlave]startBroadcast bluetooth is disable")
            return
        }
        advertiser = AndroidPlatformAdvertiser(context) { advertiserEvent ->
            when (advertiserEvent) {
                is AdvertiserEvent.OnFail -> {
                    onStateChanged(SlaveState.Idle(setting), "onFail")
                }

                AdvertiserEvent.OnSuccess -> {
                    onStateChanged(SlaveState.AdvertiseSuccess(setting), "onSuccess")
                    // 添加服务，开启server。
                    server = AndroidPlatformServer(context, slaveScope, callback = { serverEvent ->
                        when (serverEvent) {
                            ServerEvent.AddServiceFail -> {

                            }

                            ServerEvent.AddServiceSuccess -> {
                                onStateChanged(SlaveState.ServerOpened(setting),"AddServiceSuccess")
                            }

                            is ServerEvent.OnConnected -> {
                                onStateChanged(
                                    SlaveState.Connected(
                                        setting,
                                        device = serverEvent.device?.asPlatformBase()
                                            ?: throw IllegalStateException("device is null"),
                                    ), "OnConnectionStateChange"
                                )
                                startBroadcastTimeoutJob?.cancel()
                                startBroadcastTimeoutJob = null
                                debugForWrite()
                            }

                            is ServerEvent.OnServiceAdded -> {

                            }

                            ServerEvent.OnDisconnected -> {
                                onStateChanged(
                                    SlaveState.Idle(
                                        setting,
                                    ), "OnDisconnected"
                                )
                                autoBroadcastDelay(tag = "OnDisconnected")

                            }
                        }
                    }, eventCallback = {
                        val resp = when (it) {
                            is BleSlaveOnCharacteristicReadRequest -> {
                                PlatformResponse(
                                    requestId = it.requestId,
                                    offset = it.offset,
                                    value = null
                                )
                            }

                            is BleSlaveOnCharacteristicWriteRequest -> PlatformResponse(
                                requestId = it.requestId,
                                offset = it.offset,
                                value = null
                            )

                            is BleSlaveOnDescriptorReadRequest -> PlatformResponse(
                                requestId = it.requestId,
                                offset = it.offset,
                                value = null
                            )

                            is BleSlaveOnDescriptorWriteRequest -> PlatformResponse(
                                requestId = it.requestId,
                                offset = it.offset,
                                value = null
                            )

                            is BleSlaveOnExecuteWrite -> PlatformResponse(
                                requestId = it.requestId,
                                offset = 0,
                                value = null
                            )

                            is BleSlaveOnMtuChanged -> null
                            is BleSlaveOnNotificationSent -> null
                            is BleSlaveOnPhyRead -> null
                            is BleSlaveOnPhyUpdate -> null
                        }
                        _eventChannel.trySend(it.asPlatform())
                        if (resp != null) {
                            response(resp, true)
                        }
                    }).also {
                        // 开始启动Server
                        it.start(services = setting.services)
                    }
                }
            }
        }.also {
            // 开始advertise
            it.start(
                setting.deviceName, setting.broadcastService, setting.broadcastTimeout * 2L
            )
        }
        startBroadcastTimeoutJob = slaveScope.launch {
            launch {
                var time = 0L
//                delay(setting.broadcastTimeout)
                while (time < setting.broadcastTimeout) {
                    ensureActive()
                    delay(5000)
                    time += 5000
                    ensureActive()
                    i("正在广播中 ...已用时:${time}ms")
                }
                stopBroadcast()
                onStateChanged(SlaveState.Idle(setting), "onTimeout")
                autoBroadcastDelay(tag = "onTimeout")
            }

//            launch {
//                var time = 0L
//                while (isActive) {
//                    ensureActive()
//                    delay(5000)
//                    time += 5000
//                    ensureActive()
//                    if (slaveState.value !is SlaveState.Idle) {
//                        i("正在广播中 ...已用时:${time}ms")
//                    }
//                }
//            }

        }.also {
            it.invokeOnCompletion {
                i("超时任务关闭")
            }
        }
    }

    private var debugForWriteJob :Job? = null
    private fun debugForWrite() {
        debugForWriteJob?.cancel()
        debugForWriteJob = slaveScope.launch {
            while (true){
                delay(3000)
                i("debugForWrite-->")
                setting.services.random().characteristics.random().let {
                    write(SlaveWriteParam(it, byteArrayOf(0x01,0x02),false))
                }
            }
        }

    }

    private fun autoBroadcastDelay(tag: String, delay: Long = 1000L) {
        d("[PlatformSlave]autoBroadcastDelay $delay ms from $tag")
        delayReBroadcast = slaveScope.launch {
            delay(delay)
            startBroadcast()
        }
    }

    override fun stopBroadcast() {
        i("[PlatformSlave]stopBroadcast")
        debugForWriteJob?.cancel()
        advertiser?.stop()
        delayReBroadcast?.cancel()
        startBroadcastTimeoutJob?.cancel()
        server?.stop()
        onStateChanged(SlaveState.Idle(setting), "stopBroadcast")
    }

    override fun response(response: PlatformResponse, success: Boolean) {
        if (!platformContext.enable.value) {
            w("[PlatformSlave]startBroadcast bluetooth is disable")
            return
        }
        if (slaveState.value is SlaveState.Connected) {
            val status = if (success) 0 else 1
            server?.response(response.requestId, status, response.offset, response.value)
        }
    }

    override fun write(param: SlaveWriteParam) {
        if (!platformContext.enable.value) {
            w("[PlatformSlave]startBroadcast bluetooth is disable")
            return
        }
        i("[PlatformSlave]write $param")
        if (slaveState.value is SlaveState.Connected) {
            server?.write(param.characteristic.asNativeBase(), param.value, param.confirm)
        }
    }

    override fun close() {
        i("[PlatformSlave]close")
        stopBroadcast()
    }

    private fun onStateChanged(state: SlaveState, tag: String) {
        d("[]onStateChanged $state from <$tag>")
        this._slaveState.value = state
    }

}