package com.humotron.app.util

import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humotron.app.bt.ring.OnBleConnectionListener
import com.humotron.app.bt.ring.OnBleScanCallback
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.core.App
import com.humotron.app.data.local.entity.ring.RingActivityIntensityEntity
import com.humotron.app.data.local.entity.ring.RingHistoricalDataEntity
import com.humotron.app.data.local.entity.ring.RingSleepEventEntity
import com.humotron.app.data.local.entity.ring.buildRingSleepSessions
import com.humotron.app.ui.connect.HomeViewModel
import com.pluto.plugins.logger.PlutoLog
import kotlinx.coroutines.launch
import lib.smart.carering.api.BATTERY_STATE_CHARGING
import lib.smart.carering.api.CareRingManager
import lib.smart.carering.api.HistoricalData
import lib.smart.carering.api.HistoricalData2
import lib.smart.carering.api.IActivityHistory
import lib.smart.carering.api.ActivityIntensity
import lib.smart.carering.api.BasicSleepData
import lib.smart.carering.api.OnHistoricalDataListener
import lib.smart.carering.api.OnSyncActivityHistoryListener

const val STATE_DEVICE_CHARGING = 1
const val STATE_DEVICE_DISCHARGING = 0
const val STATE_DEVICE_DISCONNECTED = -3
const val STATE_DEVICE_CONNECTING = -2
const val STATE_DEVICE_CONNECTED = -1

class RingDeviceManager(val app: App) : OnBleConnectionListener {

    private var isRegisterBattery = false
    private var isInitialRingSyncStarted = false
    private var isInitialRingSyncFinished = false
    private var isHealthHistoryHandoffStarted = false
    private val ringHistoricalDataBuffer = mutableListOf<RingHistoricalDataEntity>()
    private val ringSleepEventBuffer = mutableListOf<RingSleepEventEntity>()
    private val ringActivityIntensityBuffer = mutableListOf<RingActivityIntensityEntity>()
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
                PlutoLog.e(TAG_RING_DEBUG, "STATE_DISCONNECTED Ring Device Manager")
                isRegisterBattery = false
                isInitialRingSyncStarted = false
                isInitialRingSyncFinished = false
                isHealthHistoryHandoffStarted = false
                ringHistoricalDataBuffer.clear()
                ringSleepEventBuffer.clear()
                ringActivityIntensityBuffer.clear()
                batteryLevel.postValue(STATE_DEVICE_DISCONNECTED to 0)
                connected.postValue(false)
            }

            BluetoothProfile.STATE_CONNECTED -> {
                PlutoLog.e(TAG_RING_DEBUG, "STATE_CONNECTED Ring Device Manager")
                lastConnectedAddress = app.ringBleManager.connectedDevice?.address
                batteryLevel.postValue(STATE_DEVICE_CONNECTED to 0)
                connected.postValue(true)
            }
        }
    }

    override fun onBleReady() {
        PlutoLog.e(TAG_RING_DEBUG, "onBleReady")
        postDelay {
            CareRingManager.get()
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
                            syncDataFromDev()
                        }
                    }
                }
        }
    }

    private fun syncDataFromDev() {
        Log.e(TAG_RING_DEBUG, "syncDataFromDev: ")
        if (isInitialRingSyncStarted) return
        isInitialRingSyncStarted = true
        isInitialRingSyncFinished = false
        isHealthHistoryHandoffStarted = false
        ringHistoricalDataBuffer.clear()
        ringSleepEventBuffer.clear()
        ringActivityIntensityBuffer.clear()
        PlutoLog.e(TAG_RING_DEBUG, "syncDataFromDev -> start ring sdk sync")
        syncHealthHistory()
    }

    private fun syncHealthHistory() {
        Log.e(TAG_RING_DEBUG, "syncHealthHistory: ")
        val historicalDataList = mutableListOf<RingHistoricalDataEntity>()
        CareRingManager.get().healthApi().apply {
            setOnHistoricalDataListener(object : OnHistoricalDataListener {
                override fun onHistoricalDataCount(count: Int, uuidMax: Int, uuidMin: Int) {
                    PlutoLog.e(
                        TAG_RING_DEBUG,
                        "health history count=$count uuidMax=$uuidMax uuidMin=$uuidMin"
                    )
                    sycProgress.postValue(0)
                    isSyncingData.postValue(true)
                }

                override fun onHistoricalData(index: Int, historicalData: HistoricalData) {
                    Log.e(TAG_RING_DEBUG, "onHistoricalData: $index")
                    if (historicalData.batteryChargingStatus != BATTERY_STATE_CHARGING
                        && historicalData.worn == 1
                        && historicalData.workout != 1
                    ) {
                        historicalData.heartRate.run {
                            if (this != null && this > 0) {
                                RingHistoricalDataEntity(
                                    hardwareId = homeViewModel?.prefUtils?.getRingHardwareId()
                                        .orEmpty(),
                                    ts = historicalData.ts / 1000,
                                    sourceType = 0,
                                    uuid = historicalData.uuid,
                                    heartRate = historicalData.heartRate,
                                    hrv = historicalData.hrv,
                                    spo2 = historicalData.spo2,
                                    rr = historicalData.rr,
                                    skinTemperature = historicalData.skinTemperature,
                                    totalSteps = historicalData.totalSteps,
                                    motion = historicalData.motion,
                                    batteryLevel = historicalData.batteryLevel,
                                    workout = historicalData.workout,
                                    sync = false,
                                )
                            } else null
                        }?.apply {
                            historicalDataList.add(this)
                        }
                    }
                }

                override fun onHistoricalDataCompleted() {
                    PlutoLog.e(
                        TAG_RING_DEBUG,
                        "health history completed size=${historicalDataList.size}"
                    )
                    saveHealthHistoryAndContinue(historicalDataList)
                }

                override fun onHistoricalDataTimeout() {
                    PlutoLog.e(TAG_RING_DEBUG, "health history timeout")
                    saveHealthHistoryAndContinue(historicalDataList)
                }

                override fun onSyncHistoricalDataError(code: Int) {
                    Log.e(TAG_RING_DEBUG, "onSyncHistoricalDataError: ")
                    app.cmdErrorTip(code)
                    saveHealthHistoryAndContinue(historicalDataList)
                }
            })
            syncHistoricalData(null)
        }
    }

    private fun saveHealthHistoryAndContinue(items: List<RingHistoricalDataEntity>) {
        if (isInitialRingSyncFinished || isHealthHistoryHandoffStarted) return
        isHealthHistoryHandoffStarted = true
        homeViewModel?.viewModelScope?.launch {
            if (items.isNotEmpty()) {
                ringHistoricalDataBuffer.addAll(items)
                homeViewModel?.repository?.insertRingHistoricalData(items)
            }
            syncActivityHistory()
        } ?: run {
            syncActivityHistory()
        }
    }

    private fun syncActivityHistory() {
        val sleepEvents = mutableListOf<RingSleepEventEntity>()
        val activityIntensityList = mutableListOf<RingActivityIntensityEntity>()
        val historicalDataList = mutableListOf<RingHistoricalDataEntity>()
        val hardwareId = homeViewModel?.prefUtils?.getRingHardwareId().orEmpty()

        CareRingManager.get().activityApi().apply {
            setOnActivityHistoryCallback(object : OnSyncActivityHistoryListener {
                override fun onSyncActivityHistoryCount(count: Int, uuidMax: Int, uuidMin: Int) {
                    PlutoLog.e(
                        TAG_RING_DEBUG,
                        "activity history count=$count uuidMax=$uuidMax uuidMin=$uuidMin"
                    )
                    sycProgress.postValue(50)
                    isSyncingData.postValue(true)
                }

                override fun onSyncActivityHistory(index: Int, history: IActivityHistory) {
                    when (history) {
                        is BasicSleepData -> {
                            sleepEvents.add(history.toRingSleepEventEntity(hardwareId))
                        }

                        is HistoricalData2 -> {
                            historicalDataList.add(history.toRingHistoricalDataEntity(hardwareId))
                        }

                        is ActivityIntensity -> {
                            activityIntensityList.add(
                                history.toRingActivityIntensityEntity(
                                    hardwareId
                                )
                            )
                        }
                    }
                }

                override fun onSyncActivityHistoryCompleted() {
                    PlutoLog.e(
                        TAG_RING_DEBUG,
                        "activity history completed sleep=${sleepEvents.size} intensity=${activityIntensityList.size} detailed=${historicalDataList.size}"
                    )
                    saveActivityHistory(
                        sleepEvents = sleepEvents,
                        activityIntensityList = activityIntensityList,
                        historicalDataList = historicalDataList,
                    )
                }

                override fun onSyncActivityHistoryTimeout() {
                    PlutoLog.e(TAG_RING_DEBUG, "activity history timeout")
                    saveActivityHistory(
                        sleepEvents = sleepEvents,
                        activityIntensityList = activityIntensityList,
                        historicalDataList = historicalDataList,
                    )
                }

                override fun onSyncActivityHistoryError(code: Int) {
                    app.cmdErrorTip(code)
                    saveActivityHistory(
                        sleepEvents = sleepEvents,
                        activityIntensityList = activityIntensityList,
                        historicalDataList = historicalDataList,
                    )
                }
            })
            syncHistory(null)
        }
    }

    private fun saveActivityHistory(
        sleepEvents: List<RingSleepEventEntity>,
        activityIntensityList: List<RingActivityIntensityEntity>,
        historicalDataList: List<RingHistoricalDataEntity>,
    ) {
        if (isInitialRingSyncFinished) return
        isInitialRingSyncFinished = true
        homeViewModel?.viewModelScope?.launch {
            if (sleepEvents.isNotEmpty()) {
                ringSleepEventBuffer.addAll(sleepEvents)
                homeViewModel?.repository?.insertRingSleepEventData(sleepEvents)
            }
            if (activityIntensityList.isNotEmpty()) {
                ringActivityIntensityBuffer.addAll(activityIntensityList)
                homeViewModel?.repository?.insertRingActivityIntensityData(activityIntensityList)
            }
            if (historicalDataList.isNotEmpty()) {
                ringHistoricalDataBuffer.addAll(historicalDataList)
                homeViewModel?.repository?.insertRingHistoricalData(historicalDataList)
            }
            val sleepSessions = buildRingSleepSessions(
                hardwareId = app.ringDeviceManager.lastConnectedAddress.orEmpty(),
                ringHistoricalData = ringHistoricalDataBuffer.toList(),
                sleepEvents = ringSleepEventBuffer.toList(),
                activityIntensity = ringActivityIntensityBuffer.toList(),
            )
            if (sleepSessions.isNotEmpty()) {
                homeViewModel?.repository?.insertRingSleepSessionData(sleepSessions)
            }
            homeViewModel?.uploadRingData()
            isSyncingData.postValue(false)
            sycProgress.postValue(100)
            connected.postValue(true)
            CareRingManager.get().healthApi().setOnHistoricalDataListener(null)
            CareRingManager.get().activityApi().setOnActivityHistoryCallback(null)
        } ?: run {
            isSyncingData.postValue(false)
            sycProgress.postValue(100)
            connected.postValue(true)
            CareRingManager.get().healthApi().setOnHistoricalDataListener(null)
            CareRingManager.get().activityApi().setOnActivityHistoryCallback(null)
        }
    }

    private fun HistoricalData.toRingHistoricalDataEntity(sourceType: Int): RingHistoricalDataEntity {
        return RingHistoricalDataEntity(
            hardwareId = app.ringDeviceManager.lastConnectedAddress.orEmpty(),
            ts = ts / 1000,
            sourceType = sourceType,
            uuid = uuid,
            heartRate = heartRate,
            hrv = hrv,
            spo2 = spo2,
            rr = rr,
            skinTemperature = skinTemperature,
            totalSteps = totalSteps,
            motion = motion,
            batteryLevel = batteryLevel,
            workout = workout,
            sync = false,
        )
    }

    private fun HistoricalData2.toRingHistoricalDataEntity(hardwareId: String): RingHistoricalDataEntity {
        return RingHistoricalDataEntity(
            hardwareId = hardwareId,
            ts = ts / 1000,
            sourceType = type,
            uuid = uuid,
            heartRate = hr,
            hrv = hrv,
            spo2 = spo2,
            rr = rr,
            skinTemperature = temperature,
            totalSteps = null,
            motion = null,
            batteryLevel = batteryLevel,
            workout = null,
            ibi = ibi,
            cc = cc,
            t90 = t90,
            sync = false,
        )
    }

    private fun BasicSleepData.toRingSleepEventEntity(hardwareId: String): RingSleepEventEntity {
        return RingSleepEventEntity(
            hardwareId = hardwareId,
            sleepTs = sleepTs / 1000,
            type = type,
            bedRestDuration = bedRestDuration,
            awakeningOrder = awakeningOrder,
            sync = false,
        )
    }

    private fun ActivityIntensity.toRingActivityIntensityEntity(hardwareId: String): RingActivityIntensityEntity {
        return RingActivityIntensityEntity(
            hardwareId = hardwareId,
            ts = ts / 1000,
            intensity = intensity,
            steps = steps,
            sync = false,
        )
    }

    override fun onBleAdapterStateChanged(isEnabled: Boolean) {
        PlutoLog.e(TAG_RING_DEBUG, "onBleAdapterStateChanged isEnabled: $isEnabled")
        bleAdapterEnabled.postValue(isEnabled)

        if (isEnabled) {
            lastConnectedAddress?.let { address ->
                postDelay({
                    if (app.ringBleManager.bleState == BluetoothProfile.STATE_DISCONNECTED) {
                        loge("RingDeviceManager", "Auto reconnect to $address")
                        connect(address)
                    }
                }, 1000L) //delay required
            }
        } else {
            connected.postValue(false)
        }
    }

    fun registerCb() {
        app.ringBleManager.addOnBleConnectionListener(this)
        // Re-emit current manager state for newly attached UI observers (e.g. reopening DeviceDataFragment).
        /*val currentBleState = app.ringBleManager.bleState
        onBleState(currentBleState)
        if (currentBleState == BluetoothProfile.STATE_CONNECTED) {
            postDelay {
                NexRingManager.get()
                    .deviceApi()
                    .getBatteryInfo {
                        if (it.state == BATTERY_STATE_CHARGING) {
                            batteryLevel.postValue(STATE_DEVICE_CHARGING to it.level)
                        } else {
                            batteryLevel.postValue(STATE_DEVICE_DISCHARGING to it.level)
                        }
                    }
            }
        }*/
    }

    fun unregisterCb() {
        app.ringBleManager.removeOnBleConnectionListener(this)
    }

    fun connect(address: String) {
        PlutoLog.e(TAG_RING_DEBUG, "connect $address")
        with(app.ringBleManager) {
            when (bleState) {
                BluetoothProfile.STATE_DISCONNECTED -> {
                    batteryLevel.postValue(STATE_DEVICE_CONNECTING to 0)
                    if (!connect(address)) {
                        startScan(
                            50 * 1000L,
                            object : OnBleScanCallback {
                                override fun onScanning(result: RingBleDevice) {
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
