package com.yunext.kotlin.kmp.ble.core

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.yunext.kotlin.kmp.ble.core.PlatformPermission.*
import com.yunext.kotlin.kmp.context.application
import com.yunext.kotlin.kmp.context.hdContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalStateException

actual fun platformBluetoothContext(): PlatformBluetoothContext {
    return AndroidBluetoothContext(hdContext.application)
}

internal class AndroidBluetoothContext(context: Context) : PlatformBluetoothContext {
    internal val context: Context = context.applicationContext
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private val _enable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _location: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _permissions: MutableStateFlow<Map<PlatformPermission, PlatformPermissionStatus>> =
        MutableStateFlow(
            emptyMap()
        )
    override val enable: StateFlow<Boolean> = _enable.asStateFlow()
    override val location: StateFlow<Boolean> = _location.asStateFlow()
    override val permissions: StateFlow<Map<PlatformPermission, PlatformPermissionStatus>> =
        _permissions.asStateFlow()

    init {
        _enable.value = checkBleInternal()
        _location.value = checkLocationInternal()
        _permissions.value = checkPermissions(
            hdContext.topActivity ?: throw IllegalStateException("topActivity is null")
        )

    }


    @SuppressLint("MissingPermission")
    override fun enableBle() {
        adapter.enable()
    }

    @SuppressLint("MissingPermission")
    override fun disableBle() {
        adapter.disable()
    }

    override fun enableLocation() {
    }

    override fun disableLocation() {
    }

    companion object {
        private const val KEY = "com.yunext.kotlin.kmp.ble.core.AndroidBluetoothContext"
    }

    override fun requestPermission(permission: PlatformPermission) {
        val activity = hdContext.topActivity ?: return
        val act = activity as? ComponentActivity ?: return

        val todo = when (permission) {
            BluetoothAdvertise -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    Manifest.permission.BLUETOOTH_ADVERTISE
                } else {
                    null
                }

            }

            BluetoothConnect -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                Manifest.permission.BLUETOOTH_CONNECT
            } else {
                null
            }

            Location -> Manifest.permission.ACCESS_FINE_LOCATION
            BluetoothScan -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_SCAN
            } else {
                null
            }
        } ?: return

        val launcher = act.activityResultRegistry.register(
            KEY,
//            act,
            ActivityResultContracts.RequestPermission()
        ) { success ->
            val old = _permissions.value.toMutableMap()
            old[permission] =
                if (success) PlatformPermissionStatus.Granted else PlatformPermissionStatus.Defined
            _permissions.value = old.toMap()
        }
        act.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    launcher.unregister()
                    act.lifecycle.removeObserver(this)
                }
            }
        })
        launcher.launch(todo)
    }

    override fun requestPermissions(vararg permission: PlatformPermission) {
        val activity = hdContext.topActivity ?: return
        val act = activity as? FragmentActivity ?: return
        act.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            act.activityResultRegistry,
        ) { map ->
            val old = _permissions.value.toMutableMap()
            map.forEach { (p, r) ->
                TODO()
                //old[p] = r
            }
            _permissions.value = old.toMap()
        }
    }


    private fun checkBleInternal(): Boolean {
        return adapter.isEnabled
    }

    private fun checkLocationInternal(): Boolean {
        return false
    }

    private fun checkPermissionInternal(
        activity: Activity,
        permission: PlatformPermission
    ): PlatformPermissionStatus {
        return when (permission) {
            BluetoothAdvertise -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val status = ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    )
                    if (status == PackageManager.PERMISSION_GRANTED) {
                        PlatformPermissionStatus.Granted
                    } else PlatformPermissionStatus.Defined
                } else {
                    PlatformPermissionStatus.Granted
                }
            }

            BluetoothConnect -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val status = ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                if (status == PackageManager.PERMISSION_GRANTED) {
                    PlatformPermissionStatus.Granted
                } else PlatformPermissionStatus.Defined
            } else {
                PlatformPermissionStatus.Granted
            }

            Location -> {
                val status = ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (status == PackageManager.PERMISSION_GRANTED) {
                    PlatformPermissionStatus.Granted
                } else PlatformPermissionStatus.Defined
            }

            BluetoothScan -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val status = ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                    if (status == PackageManager.PERMISSION_GRANTED) {
                        PlatformPermissionStatus.Granted
                    } else PlatformPermissionStatus.Defined
                } else {
                    // TODO("VERSION.SDK_INT < S")
                    PlatformPermissionStatus.Granted
                }

            }
        }
    }

    private fun checkPermissions(activity: Activity): Map<PlatformPermission, PlatformPermissionStatus> {
        return entries.associate { permission ->
            val status = checkPermissionInternal(activity, permission)
            (permission to status)
        }
    }

}