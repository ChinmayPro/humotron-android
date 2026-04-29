package com.humotron.app.bt.band

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.humotron.app.bt.band.model.BleData
import com.humotron.app.util.ResolveData
import com.humotron.app.util.SDUtil
import com.humotron.app.util.TAG_BAND_DEBUG
import com.jstyle.blesdk2208a.Util.BleSDK
import com.jstyle.blesdk2208a.model.MyDeviceTime
import com.pluto.plugins.logger.PlutoLog
import java.lang.reflect.Method
import java.util.ArrayDeque
import java.util.Calendar
import java.util.Queue
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * GATT session for the J-style band SDK (logic ported from the old [android.app.Service] demo).
 * Runs in-process with [Application] context — no foreground service required for app-session use.
 */
internal class BandBleGattClient(
    private val appContext: Context,
    private val mainHandler: Handler,
    private val emit: (BleData) -> Unit,
    private val onConnectionFlag: (Boolean) -> Unit,
) {

    companion object {
        private const val TAG = "BandBleGattClient"

        val ACTION_GATT_ON_DESCRIPTOR_WRITE: String =
            "com.jstylelife.ble.service.onDescriptorWrite"
        val ACTION_GATT_CONNECTED: String =
            "com.jstylelife.ble.service.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED: String =
            "com.jstylelife.ble.service.ACTION_GATT_DISCONNECTED"
        val ACTION_DATA_AVAILABLE: String =
            "com.jstylelife.ble.service.ACTION_DATA_AVAILABLE"

        private val NOTIY: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private val SERVICE_DATA: UUID =
            UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
        private val DATA_CHARACTERISTIC: UUID =
            UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb")
        private val NOTIFY_CHARACTERISTIC: UUID =
            UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager: BluetoothManager? =
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val mBluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var mGatt: BluetoothGatt? = null
    private var address: String? = null
    private var needReconnect = false
    var fastConnect = false
    private var isConnected = false

    private var scanToConnect = true
    private var isScanning = false

    private val queues = ConcurrentLinkedQueue<ByteArray>()
    private var isWriting = false

    private val mLeScanCallback =
        BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
            if (device.address == address) {
                val name = ResolveData.decodeDeviceName(device, scanRecord)
                if (!name.isNullOrEmpty() && name == "DfuTarg") return@LeScanCallback
                if (mGatt != null) return@LeScanCallback
                mainHandler.post {
                    startScanDevice(false)
                    try {
                        needReconnect = true
                        mGatt = openGatt(device)
                    } catch (_: Exception) {
                    }
                }
            }
        }

    private val bleGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            // Matches SDK demo: error 133 or disconnected — cleanup and optionally rescan.
            if (status == 133 || newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false
                isWriting = false
                queues.clear()
                onConnectionFlag(false)
                if (mGatt != null) {
                    mGatt?.disconnect()
                    mGatt?.close()
                    refreshDeviceCache(mGatt)
                    mGatt = null
                }
                gatt.disconnect()
                gatt.close()
                refreshDeviceCache(gatt)
                if (needReconnect) startScan(true)
                return
            }
            Log.i(TAG, "onConnectionStateChange:  status$status newstate $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                try {
                    gatt.discoverServices()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setCharacteristicNotification(true)
            } else {
                Log.w("servieDiscovered", "onServicesDiscovered received: $status")
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (BluetoothGatt.GATT_SUCCESS == status) {
                setCharacteristicNotification(true)
            } else {
                gatt.requestMtu(153)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                emit(
                    BleData(
                        action = ACTION_DATA_AVAILABLE,
                        value = characteristic.value,
                    ),
                )
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
        ) {
            Log.i(TAG, "onDescriptorWrite: status=$status")
            isWriting = false
            stopWriteTimeout()
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isConnected = true
                onConnectionFlag(true)
                // These will be queued and processed one by one
                offerValue(buildSetDeviceTimeCommand())
                offerValue(BleSDK.disableAncs())
                emit(BleData(action = ACTION_GATT_ON_DESCRIPTOR_WRITE))
            } else {
                Log.i(TAG, "onDescriptorWrite: failed")
                nextQueue()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            if (mGatt == null) return
            //Log.e(TAG, "onCharacteristicChanged: " + ResolveData.byte2Hex(characteristic.value))
            SDUtil.saveBTLog("log", "Receiving: " + ResolveData.byte2Hex(characteristic.value))
            emit(
                BleData(
                    action = ACTION_DATA_AVAILABLE,
                    value = characteristic.value,
                ),
            )
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            Log.i(TAG, "onCharacteristicWrite: status=$status")
            isWriting = false
            stopWriteTimeout()
            nextQueue()
        }
    }

    @SuppressLint("MissingPermission")
    fun initBluetoothDevice(mac: String, context: Context) {
        fastConnect = false
        address = mac
        if (isConnected()) return
        if (mGatt != null) {
            refreshDeviceCache(mGatt)
            mGatt = null
        }
        mainHandler.post {
            val adapter = mBluetoothAdapter ?: return@post
            val upperMac = mac.uppercase()
            if (!BluetoothAdapter.checkBluetoothAddress(upperMac)) {
                Log.e(TAG, "Invalid Bluetooth address: $upperMac. Connection aborted.")
                return@post
            }
            try {
                val device = adapter.getRemoteDevice(upperMac)
                needReconnect = true
                mGatt = openGatt(device)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get remote device or open GATT: ${e.message}")
            }
        }
    }

    private fun openGatt(device: BluetoothDevice): BluetoothGatt {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            device.connectGatt(
                appContext,
                false,
                bleGattCallback,
                BluetoothDevice.TRANSPORT_LE,
                BluetoothDevice.PHY_LE_1M_MASK,
            )
        } else {
            device.connectGatt(
                appContext,
                false,
                bleGattCallback,
                BluetoothDevice.TRANSPORT_LE,
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan(enable: Boolean) {
        Log.i(TAG, "startScan: $enable")
        if (mBluetoothAdapter?.isEnabled != true) return
        if (scanToConnect) {
            startScanDevice(true)
        } else {
            address?.let { initBluetoothDevice(it, appContext) }
        }
        scanToConnect = !scanToConnect
    }

    @SuppressLint("MissingPermission")
    private fun startScanDevice(enable: Boolean) {
        val adapter = mBluetoothAdapter ?: return
        if (enable) {
            if (isScanning) return
            mainHandler.postDelayed({
                adapter.stopLeScan(mLeScanCallback)
                isScanning = false
                startScan(true)
            }, 20000)
            fastConnect = false
            adapter.startLeScan(mLeScanCallback)
        } else {
            if (isScanning) {
                adapter.stopLeScan(mLeScanCallback)
                mainHandler.removeCallbacksAndMessages(null)
            }
        }
        isScanning = enable
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        needReconnect = false
        queues.clear()
        isWriting = false
        stopWriteTimeout()
        emit(BleData(action = ACTION_GATT_DISCONNECTED))
        val g = mGatt
        if (g != null) {
            if (isConnected) {
                g.disconnect()
                g.close()
                mGatt = null
            } else {
                Log.i(TAG, "close: ")
                g.close()
                mGatt = null
            }
        }
        isConnected = false
        onConnectionFlag(false)
    }

    fun refreshDeviceCache(gatt: BluetoothGatt?): Boolean {
        if (gatt == null) return false
        return try {
            val localMethod: Method? = gatt.javaClass.getMethod("refresh")
            localMethod?.invoke(gatt) as? Boolean ?: false
        } catch (e: Exception) {
            Log.e("s", "An exception occured while refreshing device")
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun readValue(characteristic: BluetoothGattCharacteristic) {
        mGatt?.readCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun writeValue(value: ByteArray?) {
        if (value == null) return
        offerValue(value)
    }

    @SuppressLint("MissingPermission")
    private fun writeCharacteristicInternal(value: ByteArray) {
        val g = mGatt ?: return
        val service = g.getService(SERVICE_DATA) ?: return
        val characteristic = service.getCharacteristic(DATA_CHARACTERISTIC) ?: return

        if (value.isNotEmpty() && value[0] == 0x47.toByte()) {
            needReconnect = false
        }

       // Log.i(TAG, "writeCharacteristicInternal: " + ResolveData.byte2Hex(value))
        SDUtil.saveBTLog("log", "writeValue: " + ResolveData.byte2Hex(value))

        startWriteTimeout()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(
                characteristic,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        } else {
            characteristic.value = value
            g.writeCharacteristic(characteristic)
        }
    }

    private val writeTimeoutRunnable = Runnable {
        Log.w(TAG, "Write timeout reached, forcing nextQueue")
        isWriting = false
        nextQueue()
    }

    private fun startWriteTimeout() {
        mainHandler.removeCallbacks(writeTimeoutRunnable)
        mainHandler.postDelayed(writeTimeoutRunnable, 2000) // 2 seconds timeout
    }

    private fun stopWriteTimeout() {
        mainHandler.removeCallbacks(writeTimeoutRunnable)
    }

    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(enable: Boolean) {
        val g = mGatt ?: return
        val service = g.getService(SERVICE_DATA) ?: return
        val characteristic = service.getCharacteristic(NOTIFY_CHARACTERISTIC) ?: return
        g.setCharacteristicNotification(characteristic, enable)
        try {
            Thread.sleep(20)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val descriptor = characteristic.getDescriptor(NOTIY) ?: return
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        if (mGatt == null) return
        isWriting = true
        startWriteTimeout()
        mGatt?.writeDescriptor(descriptor)
    }

    fun getSupportedGattServices(): List<BluetoothGattService>? =
        mGatt?.services

    @SuppressLint("MissingPermission")
    fun readRssi(device: BluetoothDevice) {
        mGatt?.readRemoteRssi()
    }

    fun offerValue(value: ByteArray) {
        queues.offer(value)
        nextQueue()
    }

    @Synchronized
    fun nextQueue() {
        if (!isConnected || isWriting) return
        val data = queues.poll() ?: return
        isWriting = true
        writeCharacteristicInternal(data)
    }

    private fun buildSetDeviceTimeCommand(): ByteArray {
        val now = Calendar.getInstance()
        val time = MyDeviceTime().apply {
            year = now.get(Calendar.YEAR)
            month = now.get(Calendar.MONTH) + 1
            day = now.get(Calendar.DAY_OF_MONTH)
            hour = now.get(Calendar.HOUR_OF_DAY)
            minute = now.get(Calendar.MINUTE)
            second = now.get(Calendar.SECOND)
        }
        return BleSDK.SetDeviceTime(time)
    }

    fun isConnected(): Boolean = isConnected

    fun isBusy(): Boolean = isWriting || queues.isNotEmpty()
}
