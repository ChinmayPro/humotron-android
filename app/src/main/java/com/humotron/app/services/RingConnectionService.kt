package com.humotron.app.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.bt.ring.OnBleScanCallback
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.util.NotificationManagerService
import com.humotron.app.util.PrefUtils
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RingConnectionService : Service() {

    @Inject
    lateinit var prefUtils: PrefUtils

    private val app by lazy { application as App }
    private var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        PlutoLog.e("RingConnectionService", "Service starting background connection attempt.")

        // promote service to foreground service
        ServiceCompat.startForeground(
            this,
            1,
            NotificationManagerService.createNotification(this),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            } else {
                0
            }
        )

        // Do nothing if already connected
        if (app.ringDeviceManager.connected.value == true) {
            PlutoLog.e("RingConnectionService", "Device already connected. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        if (isBlueToothSupported() && hasPermissions() && isBluetoothEnabled()) {
            connectToRing()
        } else {
            PlutoLog.e(
                "RingConnectionService",
                "Pre-conditions not met for connection (BT support, permissions, or BT enabled). Stopping service."
            )
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun connectToRing() {
        app.ringDeviceManager.registerCb()
        val address = prefUtils.getString(Preference.WEARABLE_RING) ?: ""
        if (address.isNotEmpty()) {
            PlutoLog.e("RingConnectionService", "Starting scan for ring: $address")
            // Scan for a limited time to avoid battery drain
            app.ringBleManager.startScan(60000, object : OnBleScanCallback {
                @SuppressLint("MissingPermission")
                override fun onScanning(result: RingBleDevice) {
                    PlutoLog.e(
                        "RingConnectionService",
                        "Scanning for ring: ${result.device.address}"
                    )
                    if (result.device.address == address) {
                        PlutoLog.e("RingConnectionService", "Ring found, attempting to connect.")
                        app.ringDeviceManager.connect(result.device.address)
                        app.ringBleManager.cancelScan() // Stop scanning once found
                    }
                }

                override fun onScanFinished() {
                    PlutoLog.e("RingConnectionService", "Scan finished.")
                    if (app.ringDeviceManager.connected.value != true) {
                        PlutoLog.e("RingConnectionService", "Ring not found during scan.")
                    }
                    stopSelf() // Stop the service
                }

            })
        } else {
            PlutoLog.e("RingConnectionService", "Ring address is not saved. Cannot connect.")
            stopSelf()
        }
    }

    private fun isBlueToothSupported(): Boolean {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            PlutoLog.e("RingConnectionService", "Bluetooth LE is not supported on this device.")
            return false
        }
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null) {
            PlutoLog.e("RingConnectionService", "Bluetooth adapter is null.")
            return false
        }
        return true
    }

    private fun hasPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val hasAllPermissions = permissions.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        if (!hasAllPermissions) {
            PlutoLog.w(
                "RingConnectionService",
                "Required Bluetooth permissions are not granted. Scan will likely fail."
            )
        }
        return hasAllPermissions
    }

    private fun isBluetoothEnabled(): Boolean {
        val isEnabled = mBluetoothAdapter?.isEnabled == true
        if (!isEnabled) {
            PlutoLog.e("RingConnectionService", "Bluetooth is not enabled.")
        }
        return isEnabled
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        // Stop scanning if the service is destroyed for any reason
        app.ringBleManager.cancelScan()
        PlutoLog.e("RingConnectionService", "Service destroyed.")
        super.onDestroy()
    }
}
