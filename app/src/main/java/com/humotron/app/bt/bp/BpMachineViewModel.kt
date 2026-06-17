package com.humotron.app.bt.bp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.response.AddHardwareResponse
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.PrefUtils
import com.lepu.blepro.ext.bp2.Bp2Config
import com.lepu.blepro.ext.bp2.Bp2File
import com.lepu.blepro.ext.bp2w.Bp2wConfig
import com.lepu.blepro.ext.bp3.Bp3Config
import com.lepu.blepro.ext.bp3.UserList
import com.lepu.blepro.objs.Bluetooth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BpMachineViewModel @Inject constructor(
    private val sdkManager: BpMachineSdkManager,
    private val repository: SleepRepository,
    private val bpMachineRepository: BpMachineRepository,
    private val prefUtils: PrefUtils,
) : ViewModel() {

    private val addHardwareLiveData: MutableLiveData<Resource<AddHardwareResponse>> =
        MutableLiveData()

    fun addHardwareData(): LiveData<Resource<AddHardwareResponse>> {
        return addHardwareLiveData
    }

    fun addHardwareInProfile(param: AddHardware) {
        repository.addHardwareInProfile(param).onEach { state ->
            addHardwareLiveData.value = state
        }.launchIn(viewModelScope)
    }

    val sdkState: LiveData<BpSdkState> = sdkManager.sdkState
    val scanState: LiveData<BpScanState> = sdkManager.scanState
    val connectionState: LiveData<BpConnectionState> = sdkManager.connectionState
    val devices: LiveData<List<BpDiscoveredDevice>> = sdkManager.devices
    val events: LiveData<BpMachineEvent> = sdkManager.events
    val errors: LiveData<BpError> = sdkManager.errors

    private val _syncStatus = MutableLiveData<BpSyncStatus>(BpSyncStatus.IDLE)
    val syncStatus: LiveData<BpSyncStatus> = _syncStatus

    private val _serverSyncStatus = MutableLiveData<BpServerSyncStatus>(BpServerSyncStatus.IDLE)
    val serverSyncStatus: LiveData<BpServerSyncStatus> = _serverSyncStatus

    private var isServerSyncing = false

    private var pendingFiles = mutableListOf<String>()
    private var isSyncing = false
    private var fileListHandled = false

    private var totalFileCount = 1

    init {
        sdkManager.events.asFlow()
            .onEach { event ->
                when (event) {
                    is BpMachineEvent.Bp2.FileList -> {
                        Log.e("BP Machine", "call handleBp2FileList")
                        handleBp2FileList(event.model, event.data)
                    }

                    is BpMachineEvent.Bp2.FileReadComplete -> {
                        totalFileCount++

                        Log.e(
                            "BP Machine",
                            "call handleBp2FileComplete ${event.data.fileName} ${totalFileCount} ${if (event.data.type == 1) "BP" else "ECG"}"
                        )
                        handleBp2FileComplete(event.model, event.data)
                    }

                    is BpMachineEvent.Bp2.FileReadError -> {
                        Log.e("BP Machine", "call handleBp2FileError")
                        handleBp2FileError(event.model, event.filename, event.message)
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun initialize() = sdkManager.initializeSdk()

    fun startScan(models: IntArray = BpMachineDefaults.DEFAULT_SCAN_MODELS) =
        sdkManager.startScan(models)

    fun stopScan() = sdkManager.stopScan()

    fun connect(device: BpDiscoveredDevice, autoReconnect: Boolean = true) =
        sdkManager.connect(device, autoReconnect)

    fun connect(model: Int, macAddress: String, autoReconnect: Boolean = true) =
        sdkManager.connect(model, macAddress, autoReconnect)

    fun disconnect(autoReconnect: Boolean = false) = sdkManager.disconnect(autoReconnect)

    fun disconnect(model: Int, autoReconnect: Boolean = false) =
        sdkManager.disconnect(model, autoReconnect)

    fun setEncryptKey(model: Int, key: String, token: String) =
        sdkManager.setEncryptKey(model, key, token)

    fun setEncryptKey(
        model: Int,
        key: ByteArray,
        token: ByteArray,
        encryptConnect: Boolean = false,
    ) = sdkManager.setEncryptKey(model, key, token, encryptConnect)

    fun cleanEncryptKey() = sdkManager.cleanEncryptKey()

    fun getAirBpInfo(model: Int) = sdkManager.getAirBpInfo(model)
    fun getAirBpBattery(model: Int) = sdkManager.getAirBpBattery(model)
    fun getAirBpConfig(model: Int) = sdkManager.getAirBpConfig(model)
    fun setAirBpConfig(model: Int, beepSwitchOn: Boolean) =
        sdkManager.setAirBpConfig(model, beepSwitchOn)

    fun getBp2Info(model: Int) = sdkManager.getBp2Info(model)
    fun getBp2FileList(model: Int) = sdkManager.getBp2FileList(model)
    fun readBp2File(model: Int, filename: String) = sdkManager.readBp2File(model, filename)
    fun getBp2Config(model: Int) = sdkManager.getBp2Config(model)
    fun setBp2Config(model: Int, config: Bp2Config) = sdkManager.setBp2Config(model, config)
    fun factoryResetBp2(model: Int) = sdkManager.factoryResetBp2(model)
    fun startBp2Realtime(model: Int) = sdkManager.startBp2Realtime(model)
    fun stopBp2Realtime(model: Int) = sdkManager.stopBp2Realtime(model)

    fun getBp2wInfo(model: Int) = sdkManager.getBp2wInfo(model)
    fun getBp2wFileList(model: Int) = sdkManager.getBp2wFileList(model)
    fun readBp2wFile(model: Int, filename: String) = sdkManager.readBp2wFile(model, filename)
    fun getBp2wConfig(model: Int) = sdkManager.getBp2wConfig(model)
    fun setBp2wConfig(model: Int, config: Bp2wConfig) = sdkManager.setBp2wConfig(model, config)
    fun factoryResetBp2w(model: Int) = sdkManager.factoryResetBp2w(model)
    fun startBp2wRealtime(model: Int) = sdkManager.startBp2wRealtime(model)
    fun stopBp2wRealtime(model: Int) = sdkManager.stopBp2wRealtime(model)

    fun getBp3Info(model: Int) = sdkManager.getBp3Info(model)
    fun getBp3FileList(model: Int, fileType: Int) = sdkManager.getBp3FileList(model, fileType)
    fun getBp3FileListCrc(model: Int, fileType: Int) =
        sdkManager.getBp3FileListCrc(model, fileType)

    fun readBp3File(model: Int, filename: String) = sdkManager.readBp3File(model, filename)
    fun getBp3Config(model: Int) = sdkManager.getBp3Config(model)
    fun setBp3Config(model: Int, config: Bp3Config) = sdkManager.setBp3Config(model, config)
    fun factoryResetBp3(model: Int) = sdkManager.factoryResetBp3(model)
    fun writeBp3UserList(model: Int, userList: UserList) =
        sdkManager.writeBp3UserList(model, userList)

    fun startBp3Realtime(model: Int) = sdkManager.startBp3Realtime(model)
    fun stopBp3Realtime(model: Int) = sdkManager.stopBp3Realtime(model)

    fun asSdkDevice(device: Bluetooth): BpDiscoveredDevice = device.toBpDevice()

    fun startSync() {
        Log.e("BP Machine", "call startSync")
        if (isSyncing) return
        Log.e("BP Machine", "startSync Started")
        val device = (connectionState.value as? BpConnectionState.Connected)?.device ?: return

        isSyncing = true
        fileListHandled = false
        pendingFiles.clear()
        _syncStatus.value = BpSyncStatus.FETCHING_FILE_LIST
        getBp2FileList(device.model)
    }

    private fun handleBp2FileList(model: Int, files: List<String>) {
        if (!isSyncing || fileListHandled) return
        fileListHandled = true
        viewModelScope.launch {
            Log.e("BP Machine", "Files ${files.toString()}")
            val syncedFiles = bpMachineRepository.getSyncedFileNames()
            Log.e("BP Machine", "syncedFiles ${syncedFiles.size}")
            pendingFiles = files.filter { it !in syncedFiles }.toMutableList()
            Log.e("BP Machine", "pendingFiles ${pendingFiles.size}")

            if (pendingFiles.isEmpty()) {
                finishSync(BpSyncStatus.COMPLETED)
            } else {
                _syncStatus.value = BpSyncStatus.SYNCING_FILES
                readNextFile(model)
            }
        }
    }

    private fun readNextFile(model: Int) {
        if (pendingFiles.isEmpty()) {
            finishSync(BpSyncStatus.COMPLETED)
            return
        }
        val nextFile = pendingFiles.first()
        sdkManager.readBp2File(model, nextFile)
        Log.e("BP Machine", "readNextFile ${nextFile}")
    }

    private fun handleBp2FileComplete(model: Int, file: Bp2File) {
        viewModelScope.launch {
            val device = (connectionState.value as? BpConnectionState.Connected)?.device
            if (device != null) {
                try {
                    bpMachineRepository.saveBp2File(model, prefUtils.getBpHardwareId() ?: "", file)
                } catch (e: Exception) {
                    Log.e("BP Machine", "Failed to save BP2 file: ${file.fileName}")
                }
            }
            pendingFiles.remove(file.fileName)
            readNextFile(model)
        }
    }

    private fun handleBp2FileError(model: Int, filename: String, message: String) {
        Timber.e("Error reading file $filename: $message")
        pendingFiles.remove(filename)
        readNextFile(model)
    }

    private fun finishSync(status: BpSyncStatus) {
        Log.e("BP Machine", "call finishSync")
        isSyncing = false
        _syncStatus.postValue(status)
        if (status == BpSyncStatus.COMPLETED) {
            startServerSync()
        }
    }

    private fun startServerSync() {
        Log.e("BP Machine", "call startServerSync")
        if (isServerSyncing) return
        isServerSyncing = true
        _serverSyncStatus.postValue(BpServerSyncStatus.SYNCING)

        viewModelScope.launch {
            try {
                // Sync BpData
                var bpData = bpMachineRepository.getUnsyncedBpData()
                while (bpData != null) {
                    val result = bpMachineRepository.syncBpDataToServer(bpData)
                    if (result.status == com.humotron.app.data.network.Status.SUCCESS) {
                        bpMachineRepository.updateBpSyncStatus(bpData.id, true)
                    } else {
                        break
                    }
                    bpData = bpMachineRepository.getUnsyncedBpData()
                }

                // Sync EcgData
                var ecgData = bpMachineRepository.getUnsyncedEcgData()
                while (ecgData != null) {
                    val result = bpMachineRepository.syncEcgDataToServer(ecgData)
                    if (result.status == com.humotron.app.data.network.Status.SUCCESS) {
                        bpMachineRepository.updateEcgSyncStatus(ecgData.id, true)
                    } else {
                        break
                    }
                    ecgData = bpMachineRepository.getUnsyncedEcgData()
                }

                _serverSyncStatus.postValue(BpServerSyncStatus.COMPLETED)
                Log.e("BP Machine", "call ServerSync Complete")
            } catch (e: Exception) {
                Log.e("BP Machine", "Server sync failed", e)
                _serverSyncStatus.postValue(BpServerSyncStatus.FAILED)
            } finally {
                isServerSyncing = false
            }
        }
    }
}
