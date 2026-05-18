package com.humotron.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.response.GetDeviceConfigResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DeviceConfigViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val deviceConfigLiveData: SingleLiveEvent<Resource<GetDeviceConfigResponse>> = SingleLiveEvent()
    fun getDeviceConfigLiveData(): SingleLiveEvent<Resource<GetDeviceConfigResponse>> = deviceConfigLiveData

    private val deleteHardwareLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getDeleteHardwareLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = deleteHardwareLiveData

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
}
