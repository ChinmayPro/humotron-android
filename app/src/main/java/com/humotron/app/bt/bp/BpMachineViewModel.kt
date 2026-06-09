package com.humotron.app.bt.bp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.response.AddHardwareResponse
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.PrefUtils
import com.lepu.blepro.ext.bp2.Bp2Config
import com.lepu.blepro.ext.bp2w.Bp2wConfig
import com.lepu.blepro.ext.bp3.Bp3Config
import com.lepu.blepro.ext.bp3.UserList
import com.lepu.blepro.objs.Bluetooth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class BpMachineViewModel @Inject constructor(
    private val sdkManager: BpMachineSdkManager,
    private val repository: SleepRepository,
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
}
