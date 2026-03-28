package com.humotron.app.util

import android.bluetooth.BluetoothProfile
import androidx.lifecycle.MutableLiveData
import com.humotron.app.bt.BleDevice
import com.humotron.app.bt.OnBleConnectionListener
import com.humotron.app.bt.OnBleScanCallback
import com.humotron.app.core.App
import com.humotron.app.ui.connect.HomeViewModel
import com.pluto.plugins.logger.PlutoLog
import lib.linktop.nexring.api.BATTERY_STATE_CHARGING
import lib.linktop.nexring.api.BatteryInfo
import lib.linktop.nexring.api.OnBatteryInfoChangedListener
import lib.linktop.nexring.api.IActivityHistory
import lib.linktop.nexring.api.LOAD_DATA_EMPTY
import lib.linktop.nexring.api.LOAD_DATA_STATE_COMPLETED
import lib.linktop.nexring.api.LOAD_DATA_STATE_PROCESSING
import lib.linktop.nexring.api.LOAD_DATA_STATE_START
import lib.linktop.nexring.api.NexRingManager
import lib.linktop.nexring.api.OnSleepDataLoadListener
import lib.linktop.nexring.api.SleepData

const val STATE_DEVICE_CHARGING = 1
const val STATE_DEVICE_DISCHARGING = 0
const val STATE_DEVICE_DISCONNECTED = -3
const val STATE_DEVICE_CONNECTING = -2
const val STATE_DEVICE_CONNECTED = -1

class DeviceManager(val app: App) : OnBleConnectionListener, OnSleepDataLoadListener {


    private var isRegisterBattery = false
    val batteryLevel = MutableLiveData(STATE_DEVICE_DISCONNECTED to 0)
    val sycProgress = MutableLiveData(0)
    val connected = MutableLiveData(false)
    val bleAdapterEnabled = MutableLiveData<Boolean>()
    var isSyncingData = MutableLiveData<Boolean?>(null)
    var homeViewModel: HomeViewModel? = null

    var lastConnectedAddress: String? = null

    override fun onBleState(state: Int) {
        when (state) {
            BluetoothProfile.STATE_DISCONNECTED -> {
                /*NexRingManager.get()
                    .deviceApi()
                    .removeBatteryInfoChangedListener(batteryListener)*/ //removeBatteryInfoChangedListener not found
                isRegisterBattery = false
                batteryLevel.postValue(STATE_DEVICE_DISCONNECTED to 0)
                connected.postValue(false)
            }

            BluetoothProfile.STATE_CONNECTED -> {
                lastConnectedAddress = app.bleManager.connectedDevice?.address
                batteryLevel.postValue(STATE_DEVICE_CONNECTED to 0)
                connected.postValue(true)
            }
        }
    }

    override fun onBleReady() {
        PlutoLog.e(TAG_RING_DEBUG, "onBleReady")
        postDelay {
            NexRingManager.get()
                .deviceApi()
                .getBatteryInfo {
                    if (it.state == BATTERY_STATE_CHARGING) {
                        PlutoLog.e(TAG_RING_DEBUG, "BATTERY_STATE_CHARGING")
                        batteryLevel.postValue(STATE_DEVICE_CHARGING to it.level)
                    } else {
                        batteryLevel.postValue(STATE_DEVICE_DISCHARGING to it.level)
                        PlutoLog.e(TAG_RING_DEBUG, "BATTERY_STATE_DISCHARGING")
                    }
                    if (!isRegisterBattery) {
                        isRegisterBattery = true
                        postDelay {
                            PlutoLog.e(TAG_RING_DEBUG, "syncDataFromDevice")
                            NexRingManager.get()
                                .sleepApi()
                                .syncDataFromDev()
                        }
                    }
                }
        }
    }

    private val batteryListener = object : OnBatteryInfoChangedListener {
        override fun onBatteryInfoChanged(batteryInfo: BatteryInfo) {
            if (batteryInfo.state == BATTERY_STATE_CHARGING) {
                batteryLevel.postValue(STATE_DEVICE_CHARGING to batteryInfo.level)
            } else {
                batteryLevel.postValue(STATE_DEVICE_DISCHARGING to batteryInfo.level)
            }

            if (!isRegisterBattery) {
                isRegisterBattery = true
                postDelay {
                    NexRingManager.get()
                        .sleepApi()
                        .syncDataFromDev()
                }
            }
        }
    }

    override fun onBleAdapterStateChanged(isEnabled: Boolean) {
        PlutoLog.e(TAG_RING_DEBUG, "onBleAdapterStateChanged isEnabled: $isEnabled")
        bleAdapterEnabled.postValue(isEnabled)

        if (isEnabled) {
            lastConnectedAddress?.let { address ->
                postDelay({
                    if (app.bleManager.bleState == BluetoothProfile.STATE_DISCONNECTED) {
                        loge("DeviceManager", "Auto reconnect to $address")
                        connect(address)
                    }
                }, 1000L) //delay required
            }
        } else {
            connected.postValue(false)
        }
    }

    override fun onSyncDataFromDevice(state: Int, progress: Int) {
        PlutoLog.e(TAG_RING_DEBUG, "onSyncDataFromDevice state: $state, progress: $progress")
        when (state) {
            LOAD_DATA_EMPTY -> {
                PlutoLog.e(TAG_RING_DEBUG, "onSyncDataFromDevice LOAD_DATA_EMPTY")
                //TODO Callback when no data is received from the device.
                connected.postValue(true)
                isSyncingData.postValue(false)
                homeViewModel?.loadDateData()
                homeViewModel?.let {
                    PlutoLog.e(TAG_RING_DEBUG, "homeViewModel is not null LOAD_DATA_EMPTY")
                } ?: run {
                    PlutoLog.e(TAG_RING_DEBUG, "homeViewModel is null LOAD_DATA_EMPTY")
                }
            }

            LOAD_DATA_STATE_START -> {
                PlutoLog.e(TAG_RING_DEBUG, "onSyncDataFromDevice LOAD_DATA_STATE_START")
                isSyncingData.postValue(true)
                sycProgress.postValue(progress)
            }

            LOAD_DATA_STATE_PROCESSING -> {
                PlutoLog.e(TAG_RING_DEBUG, "onSyncDataFromDevice LOAD_DATA_STATE_PROCESSING")
                sycProgress.postValue(progress)
            }

            LOAD_DATA_STATE_COMPLETED -> {
                PlutoLog.e(TAG_RING_DEBUG, "onSyncDataFromDevice LOAD_DATA_STATE_COMPLETED")
                sycProgress.postValue(progress)
                isSyncingData.postValue(false)
                connected.postValue(true)
            }
        }
    }

    override fun onSyncDataError(errorCode: Int) {
        app.cmdErrorTip(errorCode)
    }

    override fun onOutputNewSleepData(sleepData: ArrayList<SleepData>?) {
        loge("onOutputNewSleepData")
        homeViewModel?.loadDateData()
        homeViewModel?.let {
            PlutoLog.e(TAG_RING_DEBUG, "homeViewModel is not null onOutputNewSleepData")
        } ?: run {
            PlutoLog.e(TAG_RING_DEBUG, "homeViewModel is null onOutputNewSleepData")
        }
    }

    override fun onOutputActivityHistoryList(list: List<IActivityHistory>) {
        // TODO: Implement this method
    }

    fun registerCb() {
        app.bleManager.addOnBleConnectionListener(this)
        NexRingManager.get().sleepApi().setOnSleepDataLoadListener(this)
    }

    fun unregisterCb() {
        try {
            NexRingManager.get().sleepApi().setOnSleepDataLoadListener(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        app.bleManager.removeOnBleConnectionListener(this)
    }

    fun connect(address: String) {
        PlutoLog.e(TAG_RING_DEBUG, "connect $address")
        with(app.bleManager) {
            when (bleState) {
                BluetoothProfile.STATE_DISCONNECTED -> {
                    batteryLevel.postValue(STATE_DEVICE_CONNECTING to 0)
                    if (!connect(address)) {
                        startScan(
                            50 * 1000L,
                            object : OnBleScanCallback {
                                override fun onScanning(result: BleDevice) {
                                    if (result.device.address == address) {
                                        PlutoLog.e(
                                            TAG_RING_DEBUG,
                                            "address match connect by device"
                                        )
                                        connect(result.device)
                                    }
                                }

                                override fun onScanFinished() {

                                }
                            })
                    }
                }

                BluetoothProfile.STATE_CONNECTED -> {
                    onBleState(bleState)
                    onBleReady()
                }
            }
        }
    }
}
