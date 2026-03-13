package com.humotron.app.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.humotron.app.R
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.util.handlerRemove
import com.humotron.app.util.loge
import com.humotron.app.util.logi
import com.humotron.app.util.post
import com.humotron.app.util.postDelay
import lib.linktop.nexring.api.NexRingBluetoothGattCallback
import lib.linktop.nexring.api.NexRingManager
import lib.linktop.nexring.api.OEM_AUTHENTICATION_FAILED_FOR_CHECK_R2
import lib.linktop.nexring.api.OEM_AUTHENTICATION_FAILED_FOR_DECRYPT
import lib.linktop.nexring.api.OEM_AUTHENTICATION_FAILED_FOR_SN_NULL
import lib.linktop.nexring.api.OEM_AUTHENTICATION_START
import lib.linktop.nexring.api.OEM_AUTHENTICATION_SUCCESS
import lib.linktop.nexring.api.parseScanRecord

private const val OEM_STEP_CHECK_OEM_AUTHENTICATION_STATUS = 0
private const val OEM_STEP_AUTHENTICATE_OEM = 1
private const val OEM_STEP_TIMESTAMP_SYNC = 2
private const val OEM_STEP_PROCESS_COMPLETED = 3

class BleManager(val app: App) {

    private val tag = "BleManager"
    private val mBluetoothAdapter =
        (app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val mOnBleConnectionListeners: MutableList<OnBleConnectionListener> = ArrayList()

    private var mOnBleScanCallback: OnBleScanCallback? = null
    private var bleGatt: BluetoothGatt? = null
    private val scanDevMacList: MutableList<String> = ArrayList()
    var isScanning = false

    private val mScanCallback = object : ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            synchronized(scanDevMacList) {
                val scanRecord = result.scanRecord
                if (scanRecord != null) {
                    val bytes = scanRecord.bytes
                    val address = result.device.address
                    if (!scanDevMacList.contains(address).apply {
                            loge("scanDevMacList contains address($address) = ${!this}")
                        }) {
                        val bleDevice = bytes.parseScanRecord().run {
                            BleDevice(
                                result.device, cid, color, size,
                                batteryState, batteryLevel,
                                /*chipMode,*/ generation, sn,
                                result.rssi
                            )
                        }
                        scanDevMacList.add(address)
                        mOnBleScanCallback?.apply {
                            post {
                                onScanning(bleDevice)
                            }
                        }
                    }
                }
            }
        }
    }

    private val scanStopRunnable = Runnable {
        cancelScan()
    }

    var bleState = 0
    var connectedDevice: BluetoothDevice? = null

    private val mGattCallback = object : NexRingBluetoothGattCallback(NexRingManager.get()) {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            gatt: BluetoothGatt, status: Int, newState: Int,
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            loge(
                tag,
                "onConnectionStateChange->status:$status, newState:$newState"
            )
            when (newState) {
                BluetoothProfile.STATE_DISCONNECTED -> {
                    NexRingManager.get().apply {
                        setBleGatt(null)
                        unregisterRingService()
                    }
                    connectedDevice = null
                    gatt.close()
                    bleState = BluetoothProfile.STATE_DISCONNECTED
                    postBleState()
                }

                BluetoothProfile.STATE_CONNECTING -> {
                    bleState = BluetoothProfile.STATE_CONNECTING
                    postBleState()
                }

                BluetoothProfile.STATE_CONNECTED -> {
                    bleState = BluetoothProfile.STATE_CONNECTED
                    connectedDevice = gatt.device
                    postBleState()
                    // If you are using the ECG function or the BGEM function, you need to set the MTU to 200 + 3.
                    gatt.requestMtu(200 + 3)
                    // Otherwise, it only needs to be set to 40 + 3.
//                  gatt.requestMtu(40 + 3)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    loge(tag, "onMtuChanged success.")
                    gatt.discoverServices()
                }

                BluetoothGatt.GATT_FAILURE -> {
                    loge(tag, "onMtuChanged failure.")
                }

                else -> loge(tag, "onMtuChanged unknown status $status.")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            loge(tag, "onServicesDiscovered(), status:${status}")
            // Refresh device cache. This is the safest place to initiate the procedure.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                NexRingManager.get().setBleGatt(gatt)
                logi(tag, "onServicesDiscovered(), registerHealthData")
                postDelay {
                    NexRingManager.get().registerRingService()
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int,
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS &&
                NexRingManager.get().isRingServiceRegistered()
            ) {
                post {
                    //you need to synchronize the timestamp with the device first after
                    //the the service registration is successful.
                    with(NexRingManager.get()) {
                        settingsApi()
                            .timestampSync(System.currentTimeMillis()) {
                                synchronized(mOnBleConnectionListeners) {
                                    mOnBleConnectionListeners.forEach {
                                        it.onBleReady()
                                    }
                                }
                            }
                    }

                }
                OemAuthenticationProcess().start()
            }
        }
    }

    @SuppressLint("MissingPermission", "ObsoleteSdkInt")
    private fun connectInterval(device: BluetoothDevice) {
        loge(tag, "connect gatt to ${device.address}")
        bleGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            device.connectGatt(context, false, gattCallback)
            device.connectGatt(app, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(app, false, mGattCallback)
        }.apply { connect() }
    }

    fun isSupportBle(): Boolean =
//         Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
        app.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)


    @SuppressLint("MissingPermission")
    fun startScan(timeoutMillis: Long, callback: OnBleScanCallback) {
        isScanning = true
        mOnBleScanCallback = callback
        scanDevMacList.clear()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        mBluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, mScanCallback)
        postDelay(scanStopRunnable, timeoutMillis)
    }

    @SuppressLint("MissingPermission")
    fun cancelScan() {
        if (isScanning) {
            isScanning = false
            mBluetoothAdapter.bluetoothLeScanner.stopScan(mScanCallback)
            post {
                mOnBleScanCallback?.onScanFinished()
                mOnBleScanCallback = null
                scanStopRunnable.handlerRemove()
            }
        }
        scanDevMacList.clear()
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String): Boolean {
        val remoteDevice = mBluetoothAdapter.getRemoteDevice(address)
        loge("JKL", "connect to remoteDevice by address, ${remoteDevice.name}")
        return if (!remoteDevice.name.isNullOrEmpty()) {
            connect(remoteDevice)
            true
        } else {
            loge("JKL", "reject, because it cannot connect success.")
            false
        }
    }

    fun connect(device: BluetoothDevice) {
        val delayConnect = isScanning
        cancelScan()
        if (delayConnect) {
            loge("JKL", "connect to ${device.address}, delay 200L")
            postDelay({
                loge("JKL", "delay finish, connect to ${device.address}")
                connectInterval(device)
            }, 200L)
        } else {
            loge("JKL", "connect to ${device.address} right now.")
            connectInterval(device)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bleGatt?.disconnect()
        bleGatt = null
    }


    fun addOnBleConnectionListener(listener: OnBleConnectionListener) {
        synchronized(mOnBleConnectionListeners) {
            mOnBleConnectionListeners.add(listener)
        }
    }

    fun removeOnBleConnectionListener(listener: OnBleConnectionListener) {
        synchronized(mOnBleConnectionListeners) {
            mOnBleConnectionListeners.remove(listener)
        }
    }

    fun postBleState() {
        post {
            synchronized(mOnBleConnectionListeners) {
                mOnBleConnectionListeners.forEach {
                    it.onBleState(bleState)
                }
            }
        }
    }

    inner class OemAuthenticationProcess : Thread() {

        private val innerTag = "OemAuthenticationProcess"
        private val locked = Object()
        private var step = OEM_STEP_CHECK_OEM_AUTHENTICATION_STATUS

        override fun run() {
            while (step < OEM_STEP_PROCESS_COMPLETED) {
                sleep(200L)
                synchronized(locked) {
                    when (step) {
                        OEM_STEP_CHECK_OEM_AUTHENTICATION_STATUS -> {
                            loge(innerTag, "OEM_STEP_CHECK_OEM_AUTHENTICATION_STATUS")
                            NexRingManager.get().securityApi().checkOemAuthenticationStatus {
                                step =
                                    if (it) OEM_STEP_AUTHENTICATE_OEM else OEM_STEP_TIMESTAMP_SYNC
                                synchronized(locked) {
                                    locked.notify()
                                }
                            }
                        }

                        OEM_STEP_AUTHENTICATE_OEM -> {
                            loge(innerTag, "OEM_STEP_AUTHENTICATE_OEM")
                            NexRingManager.get().securityApi().authenticateOem { result ->
                                when (result) {
                                    OEM_AUTHENTICATION_FAILED_FOR_CHECK_R2 -> {
                                        logi(innerTag, "OEM_AUTHENTICATION_FAILED_FOR_CHECK_R2")
                                        step = OEM_STEP_PROCESS_COMPLETED
                                        result.showOemAuthFailDialog()
                                        synchronized(locked) {
                                            locked.notify()
                                        }
                                    }

                                    OEM_AUTHENTICATION_FAILED_FOR_DECRYPT -> {
                                        logi(innerTag, "OEM_AUTHENTICATION_FAILED_FOR_DECRYPT")
                                        step = OEM_STEP_PROCESS_COMPLETED
                                        result.showOemAuthFailDialog()
                                        synchronized(locked) {
                                            locked.notify()
                                        }
                                    }

                                    OEM_AUTHENTICATION_FAILED_FOR_SN_NULL -> {
                                        logi(innerTag, "OEM_AUTHENTICATION_FAILED_FOR_SN_NULL")
                                        step = OEM_STEP_PROCESS_COMPLETED
                                        result.showOemAuthFailDialog()
                                        synchronized(locked) {
                                            locked.notify()
                                        }
                                    }

                                    OEM_AUTHENTICATION_START -> {
                                        logi(innerTag, "OEM_AUTHENTICATION_START")
                                    }

                                    OEM_AUTHENTICATION_SUCCESS -> {
                                        logi(innerTag, "OEM_AUTHENTICATION_SUCCESS")
                                        step = OEM_STEP_TIMESTAMP_SYNC
                                        synchronized(locked) {
                                            locked.notify()
                                        }
                                    }
                                }
                            }
                        }

                        OEM_STEP_TIMESTAMP_SYNC -> {
                            loge(innerTag, "OEM_STEP_TIMESTAMP_SYNC")
                            NexRingManager.get()
                                .settingsApi()
                                .timestampSync(System.currentTimeMillis()) {
                                    loge(innerTag, "OEM_STEP_TIMESTAMP_SYNC result $it")
                                    synchronized(mOnBleConnectionListeners) {
                                        post {
                                            mOnBleConnectionListeners.forEach { listener ->
                                                listener.onBleReady()
                                            }
                                        }
                                    }
                                    step = OEM_STEP_PROCESS_COMPLETED
                                    synchronized(locked) {
                                        locked.notify()
                                    }
                                }
                        }
                    }
                    locked.wait()
                }
            }
            loge(innerTag, "OEM_STEP_PROCESS_COMPLETED")
        }
    }

    private fun Int.showOemAuthFailDialog() {
        app.mActivityLifecycleCb.currAct.apply {
            if (this != null) {
                val message = when (this@showOemAuthFailDialog) {
                    OEM_AUTHENTICATION_FAILED_FOR_SN_NULL -> {
                        getString(R.string.dialog_msg_oem_auth_failed_cause_by_sn_null)
                    }

                    OEM_AUTHENTICATION_FAILED_FOR_DECRYPT -> {
                        getString(R.string.dialog_msg_oem_auth_failed_cause_by_r1_to_r2)
                    }

                    OEM_AUTHENTICATION_FAILED_FOR_CHECK_R2 -> {
                        getString(R.string.dialog_msg_oem_auth_failed_cause_by_check_r2)
                    }

                    else -> "Unknown error."
                }
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(R.string.dialog_title_oem_auth_failed)
                        .setMessage(message)
                        .setPositiveButton(R.string.btn_label_disconnected) { _, _ ->
                            disconnect()
                        }.create().show()
                }
            } else disconnect()
        }
    }
}