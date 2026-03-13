package com.humotron.app.util

import android.bluetooth.BluetoothProfile
import androidx.lifecycle.MutableLiveData
import com.humotron.app.bt.BleDevice
import com.humotron.app.bt.OnBleConnectionListener
import com.humotron.app.bt.OnBleScanCallback
import com.humotron.app.core.App
import com.humotron.app.ui.connect.HomeViewModel
import lib.linktop.nexring.api.BATTERY_STATE_CHARGING
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
    var isSyncingData = MutableLiveData<Boolean?>(null)
    var homeViewModel: HomeViewModel? = null

    override fun onBleState(state: Int) {
        when (state) {
            BluetoothProfile.STATE_DISCONNECTED -> {
                isRegisterBattery = false
                batteryLevel.postValue(STATE_DEVICE_DISCONNECTED to 0)
                connected.postValue(false)
            }

            BluetoothProfile.STATE_CONNECTED -> {
                batteryLevel.postValue(STATE_DEVICE_CONNECTED to 0)
                connected.postValue(true)
            }
        }
    }

    override fun onBleReady() {
        postDelay {
            NexRingManager.get()
                .deviceApi()
                .getBatteryInfo {
                    if (it.state == BATTERY_STATE_CHARGING) {
                        batteryLevel.postValue(STATE_DEVICE_CHARGING to it.level)
                    } else {
                        batteryLevel.postValue(STATE_DEVICE_DISCHARGING to it.level)
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
    }

    override fun onSyncDataFromDevice(state: Int, progress: Int) {
        logi(TAG, "onSyncDataFromDevice state: $state, progress: $progress")
        when (state) {
            LOAD_DATA_EMPTY -> {
                logi(TAG, "onSyncDataFromDevice LOAD_DATA_EMPTY")
                //TODO Callback when no data is received from the device.
                connected.postValue(true)
                isSyncingData.postValue(false)
                homeViewModel?.loadDateData()
            }

            LOAD_DATA_STATE_START -> {
                logi(TAG, "onSyncDataFromDevice LOAD_DATA_STATE_START")
                isSyncingData.postValue(true)
                sycProgress.postValue(progress)
            }

            LOAD_DATA_STATE_PROCESSING -> {
                logi(TAG, "onSyncDataFromDevice LOAD_DATA_STATE_PROCESSING")
                sycProgress.postValue(progress)
            }

            LOAD_DATA_STATE_COMPLETED -> {
                logi(TAG, "onSyncDataFromDevice LOAD_DATA_STATE_COMPLETED")
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
