package com.humotron.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.param.DeviceMetaData
import com.humotron.app.domain.modal.param.DeviceMetaDataParam
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.GetDeviceConfigResponse
import com.humotron.app.domain.modal.response.UserHardware
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import lib.linktop.nexring.api.DeviceInfo
import lib.linktop.nexring.api.NexRingManager
import javax.inject.Inject

@HiltViewModel
class DeviceConfigViewModel @Inject constructor(
    private val repository: ProfileRepository,
) : ViewModel() {

    private val deviceConfigLiveData: SingleLiveEvent<Resource<GetDeviceConfigResponse>> =
        SingleLiveEvent()

    fun getDeviceConfigLiveData(): SingleLiveEvent<Resource<GetDeviceConfigResponse>> =
        deviceConfigLiveData

    private val deleteHardwareLiveData: SingleLiveEvent<Resource<CommonResponse>> =
        SingleLiveEvent()

    fun getDeleteHardwareLiveData(): SingleLiveEvent<Resource<CommonResponse>> =
        deleteHardwareLiveData

    private val deviceMetaDataLiveData: SingleLiveEvent<Resource<CommonResponse>> =
        SingleLiveEvent()

    fun getDeviceMetaDataLiveData(): SingleLiveEvent<Resource<CommonResponse>> =
        deviceMetaDataLiveData

    private val ringDeviceInfoLiveData = MutableLiveData<DeviceInfo>()
    fun getRingDeviceInfoLiveData(): LiveData<DeviceInfo> = ringDeviceInfoLiveData

    private val measureFrequency = MutableLiveData<Int>(60)
    fun getMeasureFrequency(): LiveData<Int> = measureFrequency

    fun setMeasureFrequency(frequency: Int) {
        measureFrequency.value = frequency
    }

    fun getDeviceConfiguration(id: String) {
        repository.getDeviceConfiguration(id).onEach { state ->
            deviceConfigLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun deleteUserHardwareById(id: String) {
        repository.deleteUserHardwareById(id).onEach { state ->
            deleteHardwareLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun addDeviceMetaData(
        deviceId: String,
        des: String,
        frequency: Int,
        lowPowerMode: Boolean = false,
    ) {
        val deviceInfo = ringDeviceInfoLiveData.value ?: return

        val param = DeviceMetaDataParam(
            deviceId = deviceId,
            data = DeviceMetaData(
                sn = "",
                mac = deviceInfo.bluetoothAddress ?: "",
                desc = des,
                fw = deviceInfo.firmwareVersion ?: "",
                measureFreq = frequency.toString(),
                lowPowerMode = lowPowerMode
            )
        )
        repository.addDeviceMetaData(param).onEach { state ->
            deviceMetaDataLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchRingDeviceInfo() {
        NexRingManager.get().deviceApi().getDeviceInfo { info ->
            ringDeviceInfoLiveData.postValue(info)
        }
    }
}
