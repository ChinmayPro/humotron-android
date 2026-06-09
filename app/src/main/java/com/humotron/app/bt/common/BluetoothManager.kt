package com.humotron.app.bt.common

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager as PlatformBluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val platformBluetoothManager: PlatformBluetoothManager? by lazy(LazyThreadSafetyMode.NONE) {
        appContext.getSystemService(PlatformBluetoothManager::class.java)
    }

    private val _bluetoothState = MutableStateFlow(readBluetoothState())
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(_bluetoothState.value.isEnabled)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    val runtimePermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            emptyArray()
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR,
            )
            updateState(BluetoothState.fromAdapterState(state))
        }
    }

    init {
        registerReceiver()
        refreshBluetoothState()
    }

    fun hasRequiredRuntimePermissions(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        return runtimePermissions.all { permission ->
            ContextCompat.checkSelfPermission(appContext, permission) == PERMISSION_GRANTED
        }
    }

    fun refreshBluetoothState() {
        updateState(readBluetoothState())
    }

    fun currentState(): BluetoothState = _bluetoothState.value

    private fun registerReceiver() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun updateState(newState: BluetoothState) {
        if (_bluetoothState.value == newState) return
        _bluetoothState.value = newState
        _isBluetoothEnabled.value = newState.isEnabled
    }

    private fun readBluetoothState(): BluetoothState {
        if (!hasRequiredRuntimePermissions()) {
            return BluetoothState.OFF
        }

        val adapter = platformBluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

        return runCatching {
            BluetoothState.fromAdapterState(adapter?.state ?: BluetoothAdapter.STATE_OFF)
        }.getOrElse {
            BluetoothState.OFF
        }
    }
}
