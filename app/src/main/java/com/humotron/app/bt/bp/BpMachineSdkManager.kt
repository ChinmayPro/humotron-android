package com.humotron.app.bt.bp

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.constants.Constant
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ext.airbp.Battery
import com.lepu.blepro.ext.bp2.Bp2Config
import com.lepu.blepro.ext.bp2.Bp2File
import com.lepu.blepro.ext.bp2w.Bp2wConfig
import com.lepu.blepro.ext.bp2w.Bp2wFile
import com.lepu.blepro.ext.bp3.BleFile
import com.lepu.blepro.ext.bp3.Bp3Config
import com.lepu.blepro.ext.bp3.ListCrc
import com.lepu.blepro.ext.bp3.UserList
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BleChangeObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class BpMachineSdkManager @Inject constructor(
    @ApplicationContext context: Context,
    private val applicationScope: CoroutineScope,
) : BleChangeObserver {

    private val appContext = context.applicationContext
    private val application = appContext as Application
    private val bleService = BleServiceHelper.BleServiceHelper
    private val deviceCache = ConcurrentHashMap<String, BpDiscoveredDevice>()
    private val stateLock = Any()

    private val _sdkState = MutableLiveData<BpSdkState>(BpSdkState.Uninitialized)
    val sdkState: LiveData<BpSdkState> = _sdkState

    private val _scanState = MutableLiveData<BpScanState>(BpScanState.Idle)
    val scanState: LiveData<BpScanState> = _scanState

    private val _connectionState = MutableLiveData<BpConnectionState>(BpConnectionState.Idle)
    val connectionState: LiveData<BpConnectionState> = _connectionState

    private val _events = MutableLiveData<BpMachineEvent>()
    val events: LiveData<BpMachineEvent> = _events

    private val _errors = MutableLiveData<BpError>()
    val errors: LiveData<BpError> = _errors

    private val _devices = MutableLiveData<List<BpDiscoveredDevice>>(emptyList())
    val devices: LiveData<List<BpDiscoveredDevice>> = _devices

    @Volatile
    private var sdkInitialized = false

    @Volatile
    private var observersRegistered = false

    @Volatile
    private var manualDisconnect = false

    @Volatile
    private var autoReconnectEnabled = true

    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private var currentSession: BpDiscoveredDevice? = null

    @Volatile
    private var currentFileName: String? = null

    private val serviceInitObserver = Observer<Boolean> { success ->
        if (success) {
            sdkInitialized = true
            _sdkState.postValue(BpSdkState.Ready)
            _events.postValue(BpMachineEvent.ServiceInitialized(true))
        } else {
            val message = "BP SDK service initialization failed"
            _sdkState.postValue(BpSdkState.Failed(-1, message))
            _errors.postValue(BpError.Initialization(-1, message))
            _events.postValue(BpMachineEvent.ServiceInitialized(false))
        }
    }

    private val deviceFoundObserver = Observer<Bluetooth> { bluetooth ->
        val device = bluetooth?.toBpDevice() ?: return@Observer
        deviceCache[device.deviceKey] = device
        postDeviceSnapshot()
        _events.postValue(BpMachineEvent.DeviceDiscovered(device))
    }

    private val deviceReadyObserver = Observer<Int> { model ->
        markConnectionReady(model)
    }

    private val disconnectReasonObserver = Observer<Int> { reason ->
        val session = currentSession
        _connectionState.postValue(BpConnectionState.Disconnected(session, reason))
        _events.postValue(BpMachineEvent.DisconnectReason(session, reason))

        if (manualDisconnect || !autoReconnectEnabled) {
            reconnectJob?.cancel()
            reconnectAttempts = 0
            return@Observer
        }

        session?.let { scheduleReconnect(it) }
    }

    private val encryptionObserver = Observer<InterfaceEvent> { event ->
        val verified = event?.data as? Boolean == true
        _events.postValue(BpMachineEvent.EncryptionVerified(event?.model ?: -1, verified))
    }

    init {
        registerObserversIfNeeded()
        initializeSdk()
    }

    fun initializeSdk() {
        if (sdkInitialized) return
        _sdkState.postValue(BpSdkState.Initializing)
        try {
            if (!bleService.checkService()) {
                bleService.initService(application)
            } else {
                sdkInitialized = true
                _sdkState.postValue(BpSdkState.Ready)
                _events.postValue(BpMachineEvent.ServiceInitialized(true))
            }
        } catch (throwable: Throwable) {
            val message = throwable.message ?: "BP SDK initialization error"
            _sdkState.postValue(BpSdkState.Failed(-1, message))
            _errors.postValue(BpError.Initialization(-1, message))
            _events.postValue(BpMachineEvent.ServiceInitialized(false))
        }
    }

    fun startScan(models: IntArray = BpMachineDefaults.DEFAULT_SCAN_MODELS) {
        ensureReady()
        reconnectJob?.cancel()
        reconnectAttempts = 0
        deviceCache.clear()
        _devices.postValue(emptyList())
        _scanState.postValue(BpScanState.Starting)
        _events.postValue(BpMachineEvent.ScanStarted(models))
        try {
            bleService.startScan(models)
            _scanState.postValue(BpScanState.Scanning)
        } catch (throwable: Throwable) {
            val message = throwable.message ?: "Unable to start BP scan"
            _scanState.postValue(BpScanState.Failed(-1, message))
            _errors.postValue(BpError.Scan(-1, message))
        }
    }

    fun stopScan() {
        if (!sdkInitialized) return
        _scanState.postValue(BpScanState.Stopping)
        try {
            bleService.stopScan()
            _scanState.postValue(BpScanState.Idle)
            _events.postValue(BpMachineEvent.ScanStopped)
        } catch (throwable: Throwable) {
            val message = throwable.message ?: "Unable to stop BP scan"
            _scanState.postValue(BpScanState.Failed(-1, message))
            _errors.postValue(BpError.Scan(-1, message))
        }
    }

    fun connect(device: BpDiscoveredDevice, autoReconnect: Boolean = true) {
        ensureReady()
        stopScan()
        autoReconnectEnabled = autoReconnect
        manualDisconnect = false
        reconnectJob?.cancel()
        synchronized(stateLock) {
            currentSession = device
        }
        _connectionState.postValue(BpConnectionState.Connecting(device))
        try {
            val androidDevice = device.bluetoothDevice
                ?: throw IllegalStateException("Missing Android BluetoothDevice for ${device.macAddress}")
            bleService.setInterfaces(device.model)
            bleService.connect(appContext, device.model, androidDevice)
        } catch (throwable: Throwable) {
            val message = throwable.message ?: "Unable to connect to BP device"
            _connectionState.postValue(BpConnectionState.Failed(device, -1, message))
            _errors.postValue(BpError.Connection(device, -1, message))
        }
    }

    fun connect(model: Int, macAddress: String, autoReconnect: Boolean = true) {
        val device = deviceCache.values.firstOrNull {
            it.model == model && it.macAddress.equals(macAddress, ignoreCase = true)
        } ?: run {
            val message = "Unknown BP device model=$model mac=$macAddress"
            _errors.postValue(BpError.Connection(null, -1, message))
            return
        }
        connect(device, autoReconnect)
    }

    fun disconnect(autoReconnect: Boolean = false) {
        val device = synchronized(stateLock) { currentSession }
        if (device == null) {
            _connectionState.postValue(BpConnectionState.Disconnected(null))
            return
        }
        manualDisconnect = !autoReconnect
        autoReconnectEnabled = autoReconnect
        reconnectJob?.cancel()
        currentFileName = null
        _connectionState.postValue(BpConnectionState.Disconnecting(device, autoReconnect))
        try {
            bleService.disconnect(autoReconnect)
        } catch (throwable: Throwable) {
            val message = throwable.message ?: "Unable to disconnect BP device"
            _errors.postValue(BpError.Connection(device, -1, message))
        }
    }

    fun disconnect(model: Int, autoReconnect: Boolean = false) {
        val device = synchronized(stateLock) { currentSession }
            ?: deviceCache.values.firstOrNull { it.model == model }
            ?: return

        manualDisconnect = !autoReconnect
        autoReconnectEnabled = autoReconnect
        reconnectJob?.cancel()
        currentFileName = null
        _connectionState.postValue(BpConnectionState.Disconnecting(device, autoReconnect))
        try {
            bleService.disconnect(model, autoReconnect)
        } catch (throwable: Throwable) {
            val message = throwable.message ?: "Unable to disconnect BP model"
            _errors.postValue(BpError.Connection(device, -1, message))
        }
    }

    fun setEncryptKey(
        model: Int,
        key: ByteArray,
        token: ByteArray,
        encryptConnect: Boolean = false,
    ) {
        try {
            bleService.setEncryptKey(model, key, token, encryptConnect)
        } catch (throwable: Throwable) {
            _errors.postValue(
                BpError.Unknown(
                    throwable.message ?: "Unable to set encrypt key",
                    throwable
                )
            )
        }
    }

    fun setEncryptKey(
        model: Int,
        key: String,
        token: String,
        encryptConnect: Boolean = false,
    ) {
        setEncryptKey(
            model = model,
            key = key.toByteArray(Charsets.UTF_8),
            token = token.toByteArray(Charsets.UTF_8),
            encryptConnect = encryptConnect,
        )
    }

    fun cleanEncryptKey() {
        try {
            bleService.cleanEncryptKey()
        } catch (throwable: Throwable) {
            _errors.postValue(
                BpError.Unknown(
                    throwable.message ?: "Unable to clean encrypt key",
                    throwable
                )
            )
        }
    }

    fun getAirBpInfo(model: Int) = callCommand(model) { bleService.airBpGetInfo(model) }
    fun getAirBpBattery(model: Int) = callCommand(model) { bleService.airBpGetBattery(model) }
    fun getAirBpConfig(model: Int) = callCommand(model) { bleService.airBpGetConfig(model) }
    fun setAirBpConfig(model: Int, beepSwitchOn: Boolean) =
        callCommand(model) { bleService.airBpSetConfig(model, beepSwitchOn) }

    fun getBp2Info(model: Int) = callCommand(model) { bleService.bp2GetInfo(model) }
    fun getBp2FileList(model: Int) = callCommand(model) { bleService.bp2GetFileList(model) }
    fun readBp2File(model: Int, filename: String) = callCommand(model) {
        currentFileName = filename
        bleService.bp2ReadFile(model, filename)
    }

    fun getBp2Config(model: Int) = callCommand(model) { bleService.bp2GetConfig(model) }
    fun setBp2Config(model: Int, config: Bp2Config) =
        callCommand(model) { bleService.bp2SetConfig(model, config) }

    fun factoryResetBp2(model: Int) = callCommand(model) { bleService.bp2FactoryReset(model) }
    fun startBp2Realtime(model: Int) = callCommand(model) { bleService.startRtTask(model) }
    fun stopBp2Realtime(model: Int) = callCommand(model) { bleService.stopRtTask(model) }

    fun getBp2wInfo(model: Int) = callCommand(model) { bleService.bp2wGetInfo(model) }
    fun getBp2wFileList(model: Int) = callCommand(model) { bleService.bp2wGetFileList(model) }
    fun readBp2wFile(model: Int, filename: String) = callCommand(model) {
        currentFileName = filename
        bleService.bp2wReadFile(model, filename)
    }

    fun getBp2wConfig(model: Int) = callCommand(model) { bleService.bp2wGetConfig(model) }
    fun setBp2wConfig(model: Int, config: Bp2wConfig) =
        callCommand(model) { bleService.bp2wSetConfig(model, config) }

    fun factoryResetBp2w(model: Int) = callCommand(model) { bleService.bp2wFactoryReset(model) }
    fun startBp2wRealtime(model: Int) = callCommand(model) { bleService.startRtTask(model) }
    fun stopBp2wRealtime(model: Int) = callCommand(model) { bleService.stopRtTask(model) }

    fun getBp3Info(model: Int) = callCommand(model) { bleService.bp3GetInfo(model) }
    fun getBp3FileList(model: Int, fileType: Int = Constant.Bp3FileType.BP_TYPE) =
        callCommand(model) { bleService.bp3GetFileList(model, fileType) }

    fun getBp3FileListCrc(model: Int, fileType: Int = Constant.Bp3FileType.BP_TYPE) =
        callCommand(model) { bleService.bp3GetFileListCrc(model, fileType) }

    fun readBp3File(model: Int, filename: String) = callCommand(model) {
        currentFileName = filename
        bleService.bp3ReadFile(model, filename)
    }

    fun getBp3Config(model: Int) = callCommand(model) { bleService.bp3GetConfig(model) }
    fun setBp3Config(model: Int, config: Bp3Config) =
        callCommand(model) { bleService.bp3SetConfig(model, config) }

    fun factoryResetBp3(model: Int) = callCommand(model) { bleService.bp3FactoryReset(model) }
    fun writeBp3UserList(model: Int, userList: UserList) =
        callCommand(model) { bleService.bp3WriteUserList(model, userList) }

    fun startBp3Realtime(model: Int) = callCommand(model) { bleService.startRtTask(model) }
    fun stopBp3Realtime(model: Int) = callCommand(model) { bleService.stopRtTask(model) }

    fun clearDevices() {
        deviceCache.clear()
        _devices.postValue(emptyList())
    }

    fun release() {
        reconnectJob?.cancel()
        reconnectAttempts = 0
        manualDisconnect = true
        currentFileName = null
        deviceCache.clear()
        _devices.postValue(emptyList())
        _scanState.postValue(BpScanState.Idle)
        _connectionState.postValue(BpConnectionState.Idle)
    }

    private fun registerObserversIfNeeded() {
        if (observersRegistered) return
        observersRegistered = true

        LiveEventBus.get<Boolean>(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit)
            .observeForever(serviceInitObserver)

        LiveEventBus.get<Bluetooth>(EventMsgConst.Discovery.EventDeviceFound)
            .observeForever(deviceFoundObserver)

        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceReady)
            .observeForever(deviceReadyObserver)

        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason)
            .observeForever(disconnectReasonObserver)

        LiveEventBus.get<InterfaceEvent>(EventMsgConst.Ble.EventBleDeviceEncryptVerificationCompleted)
            .observeForever(encryptionObserver)

        registerAirBpObservers()
        registerBp2Observers()
        registerBp2wObservers()
        registerBp3Observers()
    }

    private fun registerAirBpObservers() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpSetTime)
            .observeForever { event ->
                val data = event ?: return@observeForever
                if (data.data as? Boolean == true) {
                    markConnectionReady(data.model)
                }
                _events.postValue(
                    BpMachineEvent.AirBp.SetTime(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpGetInfo)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload =
                    data.data as? com.lepu.blepro.ext.airbp.DeviceInfo ?: return@observeForever
                _events.postValue(BpMachineEvent.AirBp.Info(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpGetBattery)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Battery ?: return@observeForever
                _events.postValue(BpMachineEvent.AirBp.BatteryInfo(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpSetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.AirBp.ConfigChanged(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpGetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.AirBp.ConfigLoaded(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpRtData)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Int ?: return@observeForever
                _events.postValue(BpMachineEvent.AirBp.Pressure(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpRtState)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Int ?: return@observeForever
                _events.postValue(BpMachineEvent.AirBp.State(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.AirBP.EventAirBpRtResult)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload =
                    data.data as? com.lepu.blepro.ext.airbp.RtResult ?: return@observeForever
                _events.postValue(BpMachineEvent.AirBp.Result(data.model, payload))
            }
    }

    private fun registerBp2Observers() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SyncTime)
            .observeForever { event ->
                val data = event ?: return@observeForever
                if (data.data as? Boolean == true) {
                    markConnectionReady(data.model)
                }
                _events.postValue(
                    BpMachineEvent.Bp2.SyncTime(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Info)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload =
                    data.data as? com.lepu.blepro.ext.bp2.DeviceInfo ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2.Info(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? ArrayList<String> ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2.FileList(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Int ?: return@observeForever
                val filename = currentFileName.orEmpty()
                _events.postValue(
                    BpMachineEvent.Bp2.FileReadProgress(
                        data.model,
                        filename,
                        payload
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? String ?: return@observeForever
                val filename = currentFileName.orEmpty()
                _events.postValue(BpMachineEvent.Bp2.FileReadError(data.model, filename, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Bp2File ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2.FileReadComplete(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2RtData)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? com.lepu.blepro.ext.bp2.RtData ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2.RtData(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2GetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Bp2Config ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2.ConfigLoaded(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2SetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.Bp2.ConfigChanged(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.Bp2.FactoryReset(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
    }

    private fun registerBp2wObservers() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSyncTime)
            .observeForever { event ->
                val data = event ?: return@observeForever
                if (data.data as? Boolean == true) {
                    markConnectionReady(data.model)
                }
                _events.postValue(
                    BpMachineEvent.Bp2W.SyncTime(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wInfo)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload =
                    data.data as? com.lepu.blepro.ext.bp2w.DeviceInfo ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2W.Info(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFileList)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? ArrayList<String> ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2W.FileList(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadingFileProgress)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Int ?: return@observeForever
                val filename = currentFileName.orEmpty()
                _events.postValue(
                    BpMachineEvent.Bp2W.FileReadProgress(
                        data.model,
                        filename,
                        payload
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileError)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? String ?: return@observeForever
                val filename = currentFileName.orEmpty()
                _events.postValue(BpMachineEvent.Bp2W.FileReadError(data.model, filename, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wReadFileComplete)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Bp2wFile ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2W.FileReadComplete(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wRtData)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? com.lepu.blepro.ext.bp2w.RtData ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2W.RtData(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wGetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Bp2wConfig ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp2W.ConfigLoaded(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wSetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.Bp2W.ConfigChanged(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2W.EventBp2wFactoryReset)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.Bp2W.FactoryReset(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
    }

    private fun registerBp3Observers() {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SetUtcTime)
            .observeForever { event ->
                val data = event ?: return@observeForever
                if (data.data as? Boolean == true) {
                    markConnectionReady(data.model)
                }
                _events.postValue(
                    BpMachineEvent.Bp3.SetTime(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetInfo)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload =
                    data.data as? com.lepu.blepro.ext.bp3.DeviceInfo ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.Info(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FileList)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? BleFile ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.FileList(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetFileListCrc)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? ListCrc ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.FileListCrc(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadingFileProgress)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Int ?: return@observeForever
                val filename = currentFileName.orEmpty()
                _events.postValue(
                    BpMachineEvent.Bp3.FileReadProgress(
                        data.model,
                        filename,
                        payload
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3ReadFileComplete)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? BleFile ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.FileReadComplete(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WritingFileProgress)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Int ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.FileWriteProgress(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3WriteFileComplete)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? ListCrc ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.FileWriteComplete(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3RtData)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? com.lepu.blepro.ext.bp3.RtData ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.RtData(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3GetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                val payload = data.data as? Bp3Config ?: return@observeForever
                _events.postValue(BpMachineEvent.Bp3.ConfigLoaded(data.model, payload))
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3SetConfig)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.Bp3.ConfigChanged(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP3.EventBp3FactoryReset)
            .observeForever { event ->
                val data = event ?: return@observeForever
                _events.postValue(
                    BpMachineEvent.Bp3.FactoryReset(
                        data.model,
                        data.data as? Boolean == true
                    )
                )
            }
    }

    private fun postDeviceSnapshot() {
        _devices.postValue(
            deviceCache.values.sortedWith(
                compareBy<BpDiscoveredDevice> { it.model }
                    .thenBy { it.name }
                    .thenBy { it.macAddress }
            )
        )
    }

    private fun ensureReady() {
        if (!sdkInitialized) {
            initializeSdk()
        }
    }

    private fun callCommand(model: Int, action: () -> Unit) {
        ensureReady()
        try {
            action()
        } catch (throwable: Throwable) {
            val message = throwable.message ?: "BP command failed"
            _errors.postValue(BpError.Command(model, -1, message))
        }
    }

    private fun scheduleReconnect(device: BpDiscoveredDevice) {
        reconnectJob?.cancel()
        if (reconnectAttempts >= BpMachineDefaults.MAX_RECONNECT_ATTEMPTS) {
            return
        }
        reconnectAttempts += 1
        val delayMs = min(
            BpMachineDefaults.MAX_RECONNECT_DELAY_MS,
            BpMachineDefaults.BASE_RECONNECT_DELAY_MS * reconnectAttempts.toLong()
        )
        _connectionState.postValue(
            BpConnectionState.Reconnecting(
                device,
                reconnectAttempts,
                delayMs
            )
        )
        reconnectJob = applicationScope.launch {
            delay(delayMs)
            if (!manualDisconnect && autoReconnectEnabled) {
                connect(device, autoReconnect = true)
            }
        }
    }

    private fun markConnectionReady(model: Int) {
        val session = currentSession ?: deviceCache.values.firstOrNull { it.model == model }
            ?: return
        reconnectAttempts = 0
        _connectionState.postValue(BpConnectionState.Connected(session))
        _events.postValue(BpMachineEvent.ConnectionReady(session))
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        _events.postValue(BpMachineEvent.BluetoothStateChanged(state == Ble.State.CONNECTED))
        if (state == Ble.State.CONNECTED) {
            markConnectionReady(model)
        }
    }
}
