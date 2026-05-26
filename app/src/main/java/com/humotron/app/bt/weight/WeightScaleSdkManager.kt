package com.humotron.app.bt.weight

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.humotron.app.BuildConfig
import com.humotron.app.util.SingleLiveEvent
import com.qn.device.constant.QNBLEState
import com.qn.device.listener.QNBleConnectionChangeListener
import com.qn.device.listener.QNBleDeviceDiscoveryListener
import com.qn.device.listener.QNBleStateListener
import com.qn.device.listener.QNLogListener
import com.qn.device.listener.QNResultCallback
import com.qn.device.listener.QNScaleDataListener
import com.qn.device.out.QNBleApi
import com.qn.device.out.QNBleDevice
import com.qn.device.out.QNScaleData
import com.qn.device.out.QNScaleStoreData
import com.qn.device.out.QNUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightScaleSdkManager @Inject constructor(
    @ApplicationContext context: Context,
    private val applicationScope: CoroutineScope,
) {

    private val appContext = context.applicationContext
    private val bleApi: QNBleApi = QNBleApi.getInstance(appContext)
    private val deviceCache = ConcurrentHashMap<String, QNBleDevice>()
    private val stateLock = Any()

    private val _sdkState = MutableLiveData<WeightScaleSdkState>(WeightScaleSdkState.Uninitialized)
    val sdkState: LiveData<WeightScaleSdkState> = _sdkState

    private val _bluetoothState = MutableLiveData(QNBLEState.Unknown.name)
    val bluetoothState: LiveData<String> = _bluetoothState

    private val _scanState = MutableLiveData<WeightScaleScanState>(WeightScaleScanState.Idle)
    val scanState: LiveData<WeightScaleScanState> = _scanState

    private val _devices = MutableLiveData<List<WeightScaleDeviceSummary>>(emptyList())
    val devices: LiveData<List<WeightScaleDeviceSummary>> = _devices

    private val _connectionState =
        MutableLiveData<WeightScaleConnectionState>(WeightScaleConnectionState.Idle)
    val connectionState: LiveData<WeightScaleConnectionState> = _connectionState

    private val _measurementState =
        MutableLiveData<WeightScaleMeasurementState>(WeightScaleMeasurementState.Idle)
    val measurementState: LiveData<WeightScaleMeasurementState> = _measurementState

    private val _latestMeasurement = MutableLiveData<WeightScaleMeasurement?>()
    val latestMeasurement: LiveData<WeightScaleMeasurement?> = _latestMeasurement

    private val _events = SingleLiveEvent<WeightScaleEvent>()
    val events: LiveData<WeightScaleEvent> = _events

    private val _errors = SingleLiveEvent<WeightScaleError>()
    val errors: LiveData<WeightScaleError> = _errors

    @Volatile
    private var sdkInitialized = false

    @Volatile
    private var listenersRegistered = false

    @Volatile
    private var manualDisconnect = false

    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private var lastConnectedDevice: QNBleDevice? = null
    private var lastUserProfile: WeightScaleUserProfile? = null
    private var currentUser: QNUser? = null

    private val logListener = QNLogListener { log ->
        if (BuildConfig.DEBUG) {
            _events.postValue(WeightScaleEvent.ResultMessage(0, log))
        }
    }

    private val sdkResultCallback = object : QNResultCallback {
        override fun onResult(code: Int, msg: String) {
            _events.postValue(WeightScaleEvent.ResultMessage(code, msg))
        }
    }

    private val bleStateListener = QNBleStateListener { state ->
        _bluetoothState.postValue(state.name)
        _events.postValue(WeightScaleEvent.BluetoothStateChanged(state.name))
    }

    private val discoveryListener = object : QNBleDeviceDiscoveryListener {
        override fun onStartScan() {
            _scanState.postValue(WeightScaleScanState.Scanning)
        }

        override fun onStopScan() {
            _scanState.postValue(WeightScaleScanState.Idle)
        }

        override fun onScanFail(code: Int) {
            val message = "Bluetooth scan failed"
            _scanState.postValue(WeightScaleScanState.Failed(code, message))
            _errors.postValue(WeightScaleError.Scan(code, message))
        }

        override fun onDeviceDiscover(device: QNBleDevice) {
            val mac = device.getMac().orEmpty()
            if (mac.isBlank()) return
            deviceCache[mac] = device
            _devices.postValue(deviceCache.values.map { it.toSummary() }.sortedBy { it.name })
        }
    }

    private val connectionListener = object : QNBleConnectionChangeListener {
        override fun onConnecting(device: QNBleDevice) {
            _connectionState.postValue(WeightScaleConnectionState.Connecting(device.toSummary()))
        }

        override fun onConnected(device: QNBleDevice) {
            synchronized(stateLock) {
                lastConnectedDevice = device
                reconnectAttempts = 0
            }
            _connectionState.postValue(WeightScaleConnectionState.Connected(device.toSummary()))
        }

        override fun onServiceSearchComplete(device: QNBleDevice) {
            _connectionState.postValue(WeightScaleConnectionState.Connected(device.toSummary()))
        }

        override fun onStartInteracting(device: QNBleDevice) {
            _connectionState.postValue(WeightScaleConnectionState.Ready(device.toSummary()))
            _measurementState.postValue(WeightScaleMeasurementState.Measuring(device.toSummary()))
        }

        override fun onDisconnecting(device: QNBleDevice) {
            _connectionState.postValue(WeightScaleConnectionState.Disconnecting(device.toSummary()))
        }

        override fun onDisconnected(device: QNBleDevice) {
            _connectionState.postValue(WeightScaleConnectionState.Disconnected(device.toSummary()))
            _measurementState.postValue(WeightScaleMeasurementState.Idle)
            if (!manualDisconnect) {
                scheduleReconnect(device)
            }
        }

        override fun onConnectError(device: QNBleDevice, errorCode: Int) {
            val summary = device.toSummary()
            val message = "Weight scale connection error"
            _connectionState.postValue(
                WeightScaleConnectionState.Failed(
                    device = summary,
                    code = errorCode,
                    message = message,
                )
            )
            _errors.postValue(WeightScaleError.Connection(summary, errorCode, message))
            if (!manualDisconnect) {
                scheduleReconnect(device)
            }
        }
    }

    private val scaleDataListener = object : QNScaleDataListener {
        override fun onGetUnsteadyWeight(device: QNBleDevice, weight: Double) {
            _measurementState.postValue(
                WeightScaleMeasurementState.UnsteadyWeight(device.toSummary(), weight)
            )
        }

        override fun onGetScaleData(device: QNBleDevice, scaleData: QNScaleData) {
            val measurement = scaleData.toMeasurement(device)
            _latestMeasurement.postValue(measurement)
            _measurementState.postValue(
                WeightScaleMeasurementState.Completed(device.toSummary(), measurement)
            )
        }

        override fun onGetStoredScale(device: QNBleDevice, data: MutableList<QNScaleStoreData>) {
            _measurementState.postValue(
                WeightScaleMeasurementState.StoredDataReceived(device.toSummary(), data.size)
            )
        }

        override fun onGetElectric(device: QNBleDevice, electric: Int) {
            _events.postValue(
                WeightScaleEvent.BatteryUpdated(
                    device = device.toSummary(),
                    level = electric,
                    isCharging = false,
                )
            )
        }

        override fun onScaleStateChange(device: QNBleDevice, status: Int) {
            _events.postValue(WeightScaleEvent.ScaleStatusChanged(device.toSummary(), status))
        }

        override fun onScaleEventChange(device: QNBleDevice, event: Int) {
            _events.postValue(WeightScaleEvent.ScaleEventChanged(device.toSummary(), event))
        }

        override fun readSnComplete(device: QNBleDevice, sn: String) {
            _events.postValue(WeightScaleEvent.SerialNumberReceived(device.toSummary(), sn))
        }

        override fun onGetBleVer(device: QNBleDevice, version: Int) {
            _events.postValue(WeightScaleEvent.FirmwareVersionReceived(device.toSummary(), version))
        }

        override fun onGetBatteryLevel(
            device: QNBleDevice,
            batteryLevel: Int,
            isCharging: Boolean,
        ) {
            _events.postValue(
                WeightScaleEvent.BatteryUpdated(
                    device = device.toSummary(),
                    level = batteryLevel,
                    isCharging = isCharging,
                )
            )
        }

        override fun onGetBarCode(codeType: String, code: String) = Unit

        override fun onGetBarCodeFail(codeType: String) = Unit

        override fun onGetBarCodeGunState(codeType: String, isConnected: Boolean) = Unit

        override fun onSetHeightScaleConfigState(
            device: QNBleDevice,
            supportMeasure: Boolean,
            supportSpeaker: Boolean,
            supportWifi: Boolean,
            supportDisconnectAutoOff: Boolean,
        ) = Unit

        override fun onGetHeightScaleConfig(
            device: QNBleDevice,
            deviceFunction: com.qn.device.out.QNHeightDeviceFunction,
        ) = Unit

        override fun onResetHeightScaleState(device: QNBleDevice, success: Boolean) = Unit

        override fun onClearHeightScaleWifiConfigState(device: QNBleDevice, success: Boolean) = Unit

        override fun onGetHeightScaleWifiConfig(
            device: QNBleDevice,
            hasConfig: Boolean,
            ssid: String,
        ) = Unit

        override fun onScanHeightScaleWifiSsidResult(
            device: QNBleDevice,
            ssid: String,
            rssi: Int,
        ) = Unit

        override fun onScanHeightScaleWifiSsidFinish(device: QNBleDevice, count: Int) = Unit

        override fun updateSlimDeviceConfigResult(device: QNBleDevice, resultCode: Int) = Unit

        override fun updateUserCurveDataResult(device: QNBleDevice, resultCode: Int) = Unit

        override fun updateUserSlimConfigResult(device: QNBleDevice, resultCode: Int) = Unit

        override fun deviceRestoreFactorySettings(device: QNBleDevice, resultCode: Int) = Unit
    }

    init {
        bleApi.setLogListener(logListener)
    }

    fun initializeSdk(
        appId: String = WeightScaleSdkDefaults.APP_ID,
        encryptFilePath: String = WeightScaleSdkDefaults.ENCRYPT_FILE_PATH,
    ) {
        if (sdkInitialized) return
        _sdkState.postValue(WeightScaleSdkState.Initializing)
        bleApi.initSdk(appId, encryptFilePath, object : QNResultCallback {
            override fun onResult(code: Int, msg: String) {
                if (code == 0) {
                    sdkInitialized = true
                    registerSdkListenersIfNeeded()
                    configureSdkTimeouts()
                    _sdkState.postValue(WeightScaleSdkState.Ready)
                } else {
                    val message = msg.ifBlank { "Weight scale SDK initialization failed" }
                    _sdkState.postValue(WeightScaleSdkState.Failed(code, message))
                    _errors.postValue(WeightScaleError.Initialization(code, message))
                }
                _events.postValue(WeightScaleEvent.ResultMessage(code, msg))
            }
        })
    }

    private fun registerSdkListenersIfNeeded() {
        if (listenersRegistered) return
        bleApi.setBleStateListener(bleStateListener)
        bleApi.setBleDeviceDiscoveryListener(discoveryListener)
        bleApi.setBleConnectionChangeListener(connectionListener)
        bleApi.setDataListener(scaleDataListener)
        listenersRegistered = true
    }

    fun startScan() {
        ensureSdkReady()
        reconnectJob?.cancel()
        deviceCache.clear()
        _devices.postValue(emptyList())
        _scanState.postValue(WeightScaleScanState.Starting)
        bleApi.startBleDeviceDiscovery(object : QNResultCallback {
            override fun onResult(code: Int, msg: String) {
                if (code != 0) {
                    val message = msg.ifBlank { "Unable to start weight scale scan" }
                    _scanState.postValue(WeightScaleScanState.Failed(code, message))
                    _errors.postValue(WeightScaleError.Scan(code, message))
                }
                _events.postValue(WeightScaleEvent.ResultMessage(code, msg))
            }
        })
    }

    fun stopScan() {
        ensureSdkReady()
        _scanState.postValue(WeightScaleScanState.Stopping)
        bleApi.stopBleDeviceDiscovery(sdkResultCallback)
    }

    fun connect(deviceMac: String, userProfile: WeightScaleUserProfile) {
        val device = deviceCache[deviceMac]
        requireNotNull(device) { "Unknown scale device for mac=$deviceMac" }
        connect(device, userProfile)
    }

    fun connect(device: QNBleDevice, userProfile: WeightScaleUserProfile) {
        ensureSdkReady()
        reconnectJob?.cancel()
        manualDisconnect = false
        val qnUser = buildUser(userProfile) ?: return
        synchronized(stateLock) {
            currentUser = qnUser
            lastUserProfile = userProfile
            lastConnectedDevice = device
        }
        _connectionState.postValue(WeightScaleConnectionState.Connecting(device.toSummary()))
        bleApi.connectDevice(device, qnUser, sdkResultCallback)
    }

    fun disconnect() {
        val device = synchronized(stateLock) { lastConnectedDevice } ?: return
        disconnect(device)
    }

    fun disconnect(device: QNBleDevice) {
        manualDisconnect = true
        reconnectJob?.cancel()
        _connectionState.postValue(WeightScaleConnectionState.Disconnecting(device.toSummary()))
        bleApi.disconnectDevice(device, sdkResultCallback)
    }

    fun reconnect() {
        val device = synchronized(stateLock) { lastConnectedDevice }
        if (device != null) {
            scheduleReconnect(device, force = true)
        }
    }

    fun clearMeasurements() {
        _latestMeasurement.postValue(null)
        _measurementState.postValue(WeightScaleMeasurementState.Idle)
    }

    fun release() {
        reconnectJob?.cancel()
        if (listenersRegistered) {
            bleApi.setBleDeviceDiscoveryListener(null)
            bleApi.setBleConnectionChangeListener(null)
            bleApi.setDataListener(null)
            bleApi.setBleStateListener(null)
            listenersRegistered = false
        }
        bleApi.setLogListener(null)
    }

    private fun configureSdkTimeouts() {
        bleApi.config.apply {
            scanOutTime = WeightScaleSdkDefaults.DEFAULT_SCAN_TIMEOUT_MILLIS
            connectOutTime = WeightScaleSdkDefaults.DEFAULT_CONNECT_TIMEOUT_MILLIS
            isAllowDuplicates = false
            save(sdkResultCallback)
        }
    }

    private fun buildUser(profile: WeightScaleUserProfile): QNUser? {
        var validationFailed = false
        val user = bleApi.buildUser(
            profile.userId,
            profile.heightCm,
            profile.normalizedGender,
            profile.birthday,
            profile.athleteType,
            profile.userShape,
            profile.userGoal,
            profile.clothesWeightKg,
            profile.userIndex,
            profile.secret
        ) { code, msg ->
            if (code != 0) {
                validationFailed = true
                val message = msg.ifBlank { "Invalid weight scale user profile" }
                _errors.postValue(WeightScaleError.UserValidation(code, message))
            }
            _events.postValue(WeightScaleEvent.ResultMessage(code, msg))
        }
        if (validationFailed) {
            return null
        }
        user.isMeasureFat = profile.measureFat
        user.isAdjustBodyAge = profile.adjustBodyAge
        user.areaType = profile.areaType
        return user
    }

    private fun scheduleReconnect(device: QNBleDevice, force: Boolean = false) {
        val profile = synchronized(stateLock) { lastUserProfile } ?: return
        if (!force && reconnectAttempts >= WeightScaleSdkDefaults.MAX_RECONNECT_ATTEMPTS) {
            return
        }
        reconnectJob?.cancel()
        reconnectJob = applicationScope.launch {
            synchronized(stateLock) {
                reconnectAttempts += 1
            }
            val attempt = reconnectAttempts
            _connectionState.postValue(
                WeightScaleConnectionState.Reconnecting(
                    device.toSummary(),
                    attempt
                )
            )
            delay(attempt * WeightScaleSdkDefaults.BASE_RECONNECT_DELAY_MILLIS)
            connect(device, profile)
        }
    }

    private fun ensureSdkReady() {
        if (!sdkInitialized) {
            initializeSdk()
        }
    }
}
