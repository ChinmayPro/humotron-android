package com.humotron.app.ui.connect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.response.AddHardwareResponse
import com.humotron.app.domain.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DeviceConnectionViewModel @Inject constructor(val repository: SleepRepository) : ViewModel() {

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
}