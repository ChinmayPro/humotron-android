package com.humotron.app.bt.band

import android.R.attr.name
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.humotron.app.bt.band.model.BleData
import com.jstyle.blesdk2208a.callback.OnScanResults
import com.jstyle.blesdk2208a.model.Device
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import lib.linktop.nexring.api.matchFromAdvertisementData
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped BLE API for the J-style band (no bound [android.app.Service]).
 * Inject in ViewModels / Activity; connect after permissions, disconnect when the app session ends
 * (e.g. [com.humotron.app.ui.connect.HomeViewModel.onCleared]).
 */
@Singleton
class BandBleManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        /** Matches J-style SDK / legacy demo action strings for [BleData.action]. */
        const val ACTION_GATT_ON_DESCRIPTOR_WRITE: String =
            "com.jstylelife.ble.service.onDescriptorWrite"
        const val ACTION_GATT_CONNECTED: String =
            "com.jstylelife.ble.service.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED: String =
            "com.jstylelife.ble.service.ACTION_GATT_DISCONNECTED"
        const val ACTION_DATA_AVAILABLE: String =
            "com.jstylelife.ble.service.ACTION_DATA_AVAILABLE"
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _bleEvents = MutableSharedFlow<BleData>(extraBufferCapacity = 64)
    val bleEvents: SharedFlow<BleData> = _bleEvents.asSharedFlow()

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val gattClient = BandBleGattClient(
        context.applicationContext,
        mainHandler,
        emit = { data -> _bleEvents.tryEmit(data) },
        onConnectionFlag = { connected -> _connectionState.value = connected },
    )

    private var devicesNameFilter: Array<String>? = null
    private var onScanResult: OnScanResults? = null
    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name ?: return
            Log.e("BandBleManager", "onScanResult: $deviceName")


            val record = result.scanRecord

            Log.e("BLE", "Device: ${result.device.address}")
            Log.e("BLE", "Name: ${record?.deviceName}")
            Log.e("BLE", "UUIDs: ${record?.serviceUuids}")

            if (
                isScanning &&
                deviceName.isNotEmpty() &&
                matchesDeviceName(deviceName.lowercase().trim())
            ) {
                val device = Device().apply {
                    bluetoothDevice = result.device
                    isIsconted = false
                    isPaired = false
                    isIsdfu = deviceName.lowercase().contains("dfu")
                    setName(result.device.name)
                    setMac(result.device.address)
                    setRiss(result.rssi)
                }
                onScanResult?.Success(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            onScanResult?.Fail(errorCode)
        }
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /**
     * Connect to a paired device by MAC. Safe to call from any thread; GATT work is posted to the main handler.
     */
    fun connectDevice(address: String) {
        if (!isBluetoothEnabled() || address.isBlank() || gattClient.isConnected()) return
        gattClient.initBluetoothDevice(address, context)
    }

    fun enableNotification() {
        gattClient.setCharacteristicNotification(true)
    }

    fun writeValue(value: ByteArray) {
        if (!gattClient.isConnected()) return
        gattClient.writeValue(value)
    }

    fun offerValue(data: ByteArray) {
        gattClient.offerValue(data)
    }

    fun writeNextInQueue() {
        gattClient.nextQueue()
    }

    fun disconnect() {
        gattClient.disconnect()
    }

    fun isConnected(): Boolean = gattClient.isConnected()

    @SuppressLint("MissingPermission")
    fun stopDeviceScan() {
        isScanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    /**
     * Scan for devices whose advertised name contains one of [deviceNameTokens] (case-insensitive).
     */
    @SuppressLint("MissingPermission")
    fun startDeviceScan(deviceNameTokens: Array<String>, onScanResults: OnScanResults) {
        if (isScanning || !isBluetoothEnabled()) return
        isScanning = true
        devicesNameFilter = deviceNameTokens
        onScanResult = onScanResults

        /*val paired = bluetoothAdapter?.bondedDevices.orEmpty()
        if (paired.isNotEmpty()) {
            for (bt in paired) {
                val n = bt.name ?: continue
                if (matchesDeviceName(n.lowercase())) {
                    val device = Device().apply {
                        bluetoothDevice = bt
                        isIsconted = false
                        isPaired = true
                        isIsdfu = n.lowercase().contains("dfu")
                        setName(n)
                        setMac(bt.address)
                    }
                    onScanResults.Success(device)
                }
            }
        }*/

        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        val serviceUuidData = ParcelUuid(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
        val serviceUuidAdvertised =
            ParcelUuid(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))
        //For only band devices
        val filter = ScanFilter.Builder()
            .setServiceUuid(serviceUuidAdvertised)
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(
            listOf(filter),
            settings,
            scanCallback,
        )

        /*scanner.startScan(
            null,
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build(),
            scanCallback,
        )*/
    }

    private fun matchesDeviceName(name: String): Boolean {
        val tokens = devicesNameFilter ?: return false
        if (tokens.isEmpty()) return false
        for (token in tokens) {
            if (name.contains(token.lowercase())) return true
        }
        return false
    }

    /** For advanced use — same as demo [BandBleGattClient.getSupportedGattServices]. */
    fun getSupportedGattServices() = gattClient.getSupportedGattServices()

    fun readRssi(device: BluetoothDevice) = gattClient.readRssi(device)
}
