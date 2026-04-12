package com.humotron.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ShopRepository
import com.humotron.app.domain.modal.response.DeviceDetailResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    private val devicesLiveData: SingleLiveEvent<Resource<GetShopDevicesResponse>> = SingleLiveEvent()
    private val deviceDetailLiveData: SingleLiveEvent<Resource<DeviceDetailResponse>> = SingleLiveEvent()

    fun getDevicesLiveData(): SingleLiveEvent<Resource<GetShopDevicesResponse>> = devicesLiveData
    fun getDeviceDetailLiveData(): SingleLiveEvent<Resource<DeviceDetailResponse>> = deviceDetailLiveData

    fun fetchShopDevices() {
        repository.getShopDevices().onEach { state ->
            devicesLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchDeviceDetail(id: String) {
        repository.getDeviceDetail(id).onEach { state ->
            deviceDetailLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
