package com.humotron.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.response.DeliveryOptionResponse
import com.humotron.app.domain.modal.response.GetDefaultConfigResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val deliveryOptionsLiveData: SingleLiveEvent<Resource<DeliveryOptionResponse>> = SingleLiveEvent()
    fun getDeliveryOptionsLiveData(): SingleLiveEvent<Resource<DeliveryOptionResponse>> = deliveryOptionsLiveData

    private val defaultConfigLiveData: SingleLiveEvent<Resource<GetDefaultConfigResponse>> = SingleLiveEvent()
    fun getDefaultConfigLiveData(): SingleLiveEvent<Resource<GetDefaultConfigResponse>> = defaultConfigLiveData

    private val updateUserLiveData: androidx.lifecycle.MutableLiveData<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = androidx.lifecycle.MutableLiveData()
    fun getUpdateUserLiveData(): androidx.lifecycle.LiveData<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = updateUserLiveData

    fun fetchAllDeliveryOptions() {
        repository.getAllDeliveryOptionByLimit().onEach { state ->
            deliveryOptionsLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchDefaultConfiguration() {
        repository.getDefaultConfiguration().onEach { state ->
            defaultConfigLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun updateUserById(userId: String, data: HashMap<String, Any>) {
        repository.updateUserById(userId, data).onEach { state ->
            updateUserLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val promoCodeDetailsLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>> = SingleLiveEvent()
    fun getPromoCodeDetailsLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>> = promoCodeDetailsLiveData

    fun getPromoCodeDetailsByPromoCode(promoCode: String) {
        repository.getPromoCodeDetailsByPromoCode(promoCode).onEach { state ->
            promoCodeDetailsLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun removePromoCodeByUser(userId: String) {
        repository.removePromoCodeByUser(userId).onEach { state ->
            updateUserLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun clearUpdateUserLiveData() {
        updateUserLiveData.value = null
    }

    fun clearPromoCodeDetailsLiveData() {
        promoCodeDetailsLiveData.value = null
    }
}
