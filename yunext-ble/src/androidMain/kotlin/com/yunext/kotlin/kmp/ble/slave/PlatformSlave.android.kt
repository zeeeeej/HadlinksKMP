package com.yunext.kotlin.kmp.ble.slave

import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import com.yunext.kotlin.kmp.ble.core.AndroidBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothContext
import com.yunext.kotlin.kmp.ble.core.PlatformBluetoothGattCharacteristic
import com.yunext.kotlin.kmp.ble.core.asNativeBase
import com.yunext.kotlin.kmp.ble.core.asPlatformBase
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryImpl
import com.yunext.kotlin.kmp.ble.history.BluetoothHistoryOwner
import com.yunext.kotlin.kmp.ble.history.bleIn
import com.yunext.kotlin.kmp.ble.history.bleOpt
import com.yunext.kotlin.kmp.ble.history.bleOut
import com.yunext.kotlin.kmp.ble.master.masterScope
import com.yunext.kotlin.kmp.ble.util.d
import com.yunext.kotlin.kmp.ble.util.display
import com.yunext.kotlin.kmp.ble.util.i
import com.yunext.kotlin.kmp.ble.util.w
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import kotlin.uuid.ExperimentalUuidApi

actual fun PlatformSlave(
    context: PlatformBluetoothContext,
    setting: SlaveSetting,
): PlatformSlave {
    return AndroidSlave(context as AndroidBluetoothContext, setting)
}

private val slaveScope by lazy {
    CoroutineScope(Dispatchers.Main.immediate + SupervisorJob() + CoroutineName("slaveScope"))
}

internal class AndroidSlave(
    private val platformContext: AndroidBluetoothContext,
    setting: SlaveSetting,
    override val history: BluetoothHistoryOwner = BluetoothHistoryImpl()
) : PlatformSlave, BluetoothHistoryOwner by history {

    companion object {
        private const val TAG = "AndroidSlave"
    }

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


    }

    init {
        onStateChanged(SlaveState.Idle(setting), "init")
        autoBroadcastDelay("init")
    }

    override fun updateSetting(setting: SlaveSetting) {
        i("[${TAG}]updateSetting")
        stopBroadcast()
        this.setting = setting
        onStateChanged(SlaveState.Idle(setting), "updateSetting")
        autoBroadcastDelay("updateSetting")
    }

    private var startBroadcastTimeoutJob: Job? = null
    private var startJob: Job? = null
    private var startBroadcastPrepareJob: Job? = null

    override fun startBroadcast() {
        i("[${TAG}]startBroadcast")
        bleOpt("startBroadcast")
        startBroadcastPrepareJob?.cancel()
        startBroadcastPrepareJob = masterScope.launch {
            startBroadcastTimeoutJob?.cancel()
            startBroadcastTimeoutJob = null
            startJob?.cancel()
            startJob = null
            server?.stop()
            advertiser?.stop()
            delayReBroadcast?.cancel()
            delay(500)
            if (!platformContext.enable.value) {
                w("[${TAG}]startBroadcast bluetooth is disable")
                return@launch
            }
            advertiser = AndroidPlatformAdvertiser(context) { advertiserEvent ->
                when (advertiserEvent) {
                    is AdvertiserEvent.OnFail -> {
                        onStateChanged(SlaveState.Idle(setting), "onFail")
                    }

                    AdvertiserEvent.OnSuccess -> {
                        onStateChanged(SlaveState.AdvertiseSuccess(setting), "onSuccess")
                        // 添加服务，开启server。
                        server =
                            AndroidPlatformServer(context, slaveScope, callback = { serverEvent ->
                                when (serverEvent) {
                                    ServerEvent.AddServiceFail -> {
                                        bleIn("AddServiceFail")
                                    }

                                    is ServerEvent.AddServiceSuccess -> {
                                        bleIn("AddServiceSuccess")
                                        onStateChanged(
                                            SlaveState.ServerOpened(
                                                setting,
                                                serverEvent.services.map(BluetoothGattService::asPlatformBase)
                                            ),
                                            "AddServiceSuccess"
                                        )
                                    }

                                    is ServerEvent.OnConnected -> {
                                        advertiser?.stop()
                                        bleIn("OnConnected ${serverEvent.device?.display}")
                                        onStateChanged(
                                            SlaveState.Connected(
                                                setting,
                                                device = serverEvent.device?.asPlatformBase()
                                                    ?: throw IllegalStateException("device is null"),
                                            ), "OnConnectionStateChange"
                                        )
                                        startBroadcastTimeoutJob?.cancel()
                                        startBroadcastTimeoutJob = null
                                        debugForNotify()
                                    }

                                    is ServerEvent.OnServiceAdded -> {
                                        bleIn("OnServiceAdded ")
                                    }

                                    ServerEvent.OnDisconnected -> {
                                        bleIn("OnDisconnected ")
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
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "<${it.characteristic?.value?.toHexString()}> by ${it.characteristic?.uuid.toString()}/${it.characteristic?.service?.uuid.toString()}",
                                            tag = "OnCharacteristicReadRequest"
                                        )
                                        PlatformResponse(
                                            requestId = it.requestId,
                                            offset = it.offset,
                                            value = null
                                        )
                                    }

                                    is BleSlaveOnCharacteristicWriteRequest -> {
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "<${it.value?.toHexString()}> by ${it.characteristic?.uuid.toString()}/${it.characteristic?.service?.uuid.toString()}",
                                            tag = "OnCharacteristicWriteRequest"
                                        )
                                        PlatformResponse(
                                            requestId = it.requestId,
                                            offset = it.offset,
                                            value = null
                                        )
                                    }

                                    is BleSlaveOnDescriptorReadRequest -> {
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "<${it.descriptor?.value?.toHexString()}> by ${it.descriptor?.uuid.toString()}/${it.descriptor?.characteristic?.uuid.toString()}",
                                            tag = "OnDescriptorReadRequest"
                                        )
                                        PlatformResponse(
                                            requestId = it.requestId,
                                            offset = it.offset,
                                            value = null
                                        )
                                    }

                                    is BleSlaveOnDescriptorWriteRequest -> {
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "<${it.value?.toHexString()}> by ${it.descriptor?.uuid.toString()}/${it.descriptor?.characteristic?.uuid.toString()}",
                                            tag = "OnDescriptorWriteRequest"
                                        )
                                        PlatformResponse(
                                            requestId = it.requestId,
                                            offset = it.offset,
                                            value = null
                                        )
                                    }

                                    is BleSlaveOnExecuteWrite -> {
                                        bleIn(
                                            "BleSlaveOnExecuteWrite",
                                            tag = "BleSlaveOnExecuteWrite"
                                        )
                                        PlatformResponse(
                                            requestId = it.requestId,
                                            offset = 0,
                                            value = null
                                        )
                                    }

                                    is BleSlaveOnMtuChanged -> {
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "${it.mtu}",
                                            tag = "BleSlaveOnMtuChanged"
                                        )
                                        null
                                    }

                                    is BleSlaveOnNotificationSent -> {
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "BleSlaveOnNotificationSent",
                                            tag = "BleSlaveOnNotificationSent"
                                        )
                                        null
                                    }

                                    is BleSlaveOnPhyRead -> {
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "${it.rxPhy} ${it.txPhy}",
                                            tag = "BleSlaveOnPhyRead"
                                        )
                                        null
                                    }

                                    is BleSlaveOnPhyUpdate -> {
                                        @OptIn(ExperimentalStdlibApi::class)
                                        bleIn(
                                            "${it.rxPhy} ${it.txPhy}",
                                            tag = "BleSlaveOnPhyUpdate"
                                        )
                                        null
                                    }
                                }
                                _eventChannel.trySend(it.asPlatform())
                                if (resp != null) {
                                    response(resp, true)
                                }
                            }).also {
                                startJob?.cancel()
                                startJob = it.connectState.onEach { state ->
                                    @OptIn(ExperimentalStdlibApi::class)
                                    bleIn(
                                        "连接状态：$state",
                                        tag = "BleSlaveOnPhyUpdate"
                                    )
                                    when (state) {
                                        ConnectState.NaN -> {
                                            onStateChanged(
                                                SlaveState.Idle(
                                                    setting,
                                                ), "OnDisconnected"
                                            )
                                            //autoBroadcastDelay(tag = "OnDisconnected")
                                        }

                                        is ConnectState.Connected -> {
                                            onStateChanged(
                                                SlaveState.Connected(
                                                    setting,
                                                    device = state.device.asPlatformBase()
                                                        ?: throw IllegalStateException("device is null"),
                                                    services = state.services.map(
                                                        BluetoothGattService::asPlatformBase
                                                    )
                                                ), "OnConnectionStateChange"
                                            )
                                            startBroadcastTimeoutJob?.cancel()
                                            startBroadcastTimeoutJob = null
                                            debugForNotify()
                                        }

                                        is ConnectState.Disconnected -> {
                                            onStateChanged(
                                                SlaveState.Idle(
                                                    setting,
                                                ), "OnDisconnected"
                                            )
                                            autoBroadcastDelay(tag = "OnDisconnected")
                                        }
                                    }
                                }.launchIn(slaveScope)
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
                        bleOpt("正在广播中 ...已用时:${time}ms")
                        i("正在广播中 ...已用时:${time}ms")
                    }
                    stopBroadcast()
                    bleOpt("广播超时")
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


    }

    private var debugForWriteJob: Job? = null
    private fun debugForNotify() {
        if (false) return
        debugForWriteJob?.cancel()
        debugForWriteJob = slaveScope.launch {
            val ch = setting.services
                .flatMap {
                    it.characteristics.toList()
                }.filter {
                    it.properties.contains(PlatformBluetoothGattCharacteristic.Property.Notify)
                }.random()
            while (slaveState.value is SlaveState.Connected) {
                delay(3000)
                i("debugForNotify $ch -->")
                notify(SlaveWriteParam(ch, byteArrayOf(0x01, 0x02), false))
            }
        }
    }

    private fun autoBroadcastDelay(tag: String, delay: Long = 1000L) {
        d("[${TAG}]autoBroadcastDelay $delay ms from $tag")
        delayReBroadcast = slaveScope.launch {
            delay(delay)
            startBroadcast()
        }
    }

    override fun stopBroadcast() {
        i("[${TAG}]stopBroadcast")
        bleOpt("stopBroadcast")
        startBroadcastPrepareJob?.cancel()
        startBroadcastPrepareJob = null
        debugForWriteJob?.cancel()
        advertiser?.stop()
        delayReBroadcast?.cancel()
        startBroadcastTimeoutJob?.cancel()
        startJob?.cancel()
        startJob = null
        server?.stop()
        onStateChanged(SlaveState.Idle(setting), "stopBroadcast")
        // 不能断开连接
//        slaveScope.launch {
//            platformContext.disableBle()
//            delay(1000)
//            platformContext.enableBle()
//        }

    }


    override fun response(response: PlatformResponse, success: Boolean) {
        bleOpt("response")
        if (!platformContext.enable.value) {
            w("[${TAG}]startBroadcast bluetooth is disable")
            return
        }
        if (slaveState.value is SlaveState.Connected) {
            val status = if (success) 0 else 1
            @OptIn(ExperimentalStdlibApi::class)
            bleOut("<${response.value?.toHexString()}>$success", tag = "response")
            server?.response(response.requestId, status, response.offset, response.value)
        }
    }


    override fun notify(param: SlaveWriteParam) {
        bleOpt("notify")
        if (!platformContext.enable.value) {
            w("[${TAG}]startBroadcast bluetooth is disable")
            return
        }
        i("[${TAG}]notify $param")
        if (slaveState.value is SlaveState.Connected) {
            // check
            val properties = param.characteristic.properties
            if (properties.contains(PlatformBluetoothGattCharacteristic.Property.Notify)) {
                @OptIn(ExperimentalUuidApi::class)
                val chUUID = param.characteristic.uuid.toString()
                @OptIn(ExperimentalStdlibApi::class)
                bleOut("<${param.value.toHexString()}> by $chUUID", tag = "notify")
                server?.write(param.characteristic.asNativeBase(), param.value, param.confirm)
            } else {
                @OptIn(ExperimentalUuidApi::class)
                w("notify ${param.characteristic.uuid.toString()} 不支持notify")
                @OptIn(ExperimentalUuidApi::class)
                bleOpt(
                    "notify ${param.characteristic.uuid.toString()} 不支持notify",
                    tag = "notify"
                )
            }
        }
    }

    override fun close() {
        i("[${TAG}]close")
        bleOpt("close")
        stopBroadcast()
        advertiser?.close()
        startBroadcastPrepareJob?.cancel()
        startBroadcastPrepareJob = null
    }

    private fun onStateChanged(state: SlaveState, tag: String) {
        d("[${TAG}]onStateChanged $state from <$tag>")
        this._slaveState.value = state
    }

}