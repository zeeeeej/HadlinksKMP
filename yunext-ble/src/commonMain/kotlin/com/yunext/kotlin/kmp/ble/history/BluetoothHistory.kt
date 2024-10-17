package com.yunext.kotlin.kmp.ble.history

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

// opt [service] [ch] [value] [success]

sealed interface BluetoothHistory {
    val message: String
    val timestamp: Long
    val tag: String?


}

val BluetoothHistory.type: String
    get() = when (this) {
        is IBleIn -> "I"
        is IBleOut -> "O"
        is IBleOpt -> "P"
    }

interface IBleIn : BluetoothHistory
interface IBleOut : BluetoothHistory
interface IBleOpt : BluetoothHistory

internal data class BleIn(
    override val message: String, override val timestamp: Long,
    override val tag: String? = null,
) : IBleIn

internal data class BleOut(
    override val message: String, override val timestamp: Long,
    override val tag: String? = null
) : IBleOut

internal data class BleOpt(
    override val message: String, override val timestamp: Long,
    override val tag: String? = null
) : IBleOpt

interface BluetoothHistoryOwner {
    val histories: StateFlow<List<BluetoothHistory>>
    fun add(history: BluetoothHistory)
    fun clear()
}

internal class BluetoothHistoryImpl : BluetoothHistoryOwner {
    private val _histories: MutableStateFlow<List<BluetoothHistory>> = MutableStateFlow(emptyList())
    override val histories: StateFlow<List<BluetoothHistory>>
        get() = _histories.asStateFlow()

    override fun add(history: BluetoothHistory) {
        val old = _histories.value
        _histories.value = old + history
    }

    override fun clear() {
        _histories.value = emptyList()
    }

}

internal fun BluetoothHistoryOwner.bleOpt(msg: String, tag: String? = null) {
    this.add(BleOpt(msg, Clock.System.now().toEpochMilliseconds(), tag))
}

internal fun BluetoothHistoryOwner.bleIn(msg: String, tag: String? = null) {
    this.add(BleIn(msg, Clock.System.now().toEpochMilliseconds(), tag))
}

internal fun BluetoothHistoryOwner.bleOut(msg: String, tag: String? = null) {
    this.add(BleOut(msg, Clock.System.now().toEpochMilliseconds(), tag))
}