package com.yunext.kotlin.kmp.ble.master

import android.bluetooth.BluetoothGatt
import androidx.collection.LruCache

class BleLruCache(max:Int = 4) {
    val map :LruCache<String,BluetoothGatt> = LruCache(max)
}