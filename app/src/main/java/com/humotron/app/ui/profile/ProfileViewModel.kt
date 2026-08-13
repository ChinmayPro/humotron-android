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

    private val deleteUserLiveData: androidx.lifecycle.MutableLiveData<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = androidx.lifecycle.MutableLiveData()
    fun getDeleteUserLiveData(): androidx.lifecycle.LiveData<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = deleteUserLiveData

    fun deleteUserById(id: String) {
        repository.deleteUserById(id).onEach { state ->
            deleteUserLiveData.value = state
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

    var isReturningFromDetail: Boolean = false

    private val dataSourcesLiveData: androidx.lifecycle.MutableLiveData<Resource<com.humotron.app.domain.modal.response.DataSourcesResponse>> = androidx.lifecycle.MutableLiveData()
    fun getDataSourcesLiveData(): androidx.lifecycle.LiveData<Resource<com.humotron.app.domain.modal.response.DataSourcesResponse>> = dataSourcesLiveData

    fun fetchDataSources() {
        repository.getDataSources().onEach { state ->
            dataSourcesLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val dataSourceDetailLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.DataSourceDetailResponse>> = SingleLiveEvent()
    fun getDataSourceDetailLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.DataSourceDetailResponse>> = dataSourceDetailLiveData

    fun fetchDataSourceDetail(sourceKey: String) {
        repository.getDataSourceDetail(sourceKey).onEach { state ->
            dataSourceDetailLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val updateDataSourceUsageLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getUpdateDataSourceUsageLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = updateDataSourceUsageLiveData

    fun updateDataSourceUsage(
        sourceKey: String,
        param: com.humotron.app.domain.modal.param.UpdateDataSourceUsageParam
    ) {
        repository.updateDataSourceUsage(sourceKey, param).onEach { state ->
            updateDataSourceUsageLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val pauseDataSourceLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getPauseDataSourceLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = pauseDataSourceLiveData

    fun pauseDataSource(
        sourceKey: String,
        param: com.humotron.app.domain.modal.param.PauseDataSourceParam
    ) {
        repository.pauseDataSource(sourceKey, param).onEach { state ->
            pauseDataSourceLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val updateDataSourceTopicsLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getUpdateDataSourceTopicsLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = updateDataSourceTopicsLiveData

    fun updateDataSourceTopics(sourceKey: String, body: Map<String, Boolean>) {
        repository.updateDataSourceTopics(sourceKey, body).onEach { state ->
            updateDataSourceTopicsLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val deleteDataSourceDataLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getDeleteDataSourceDataLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = deleteDataSourceDataLiveData

    fun deleteDataSourceData(sourceKey: String) {
        repository.deleteDataSourceData(sourceKey).onEach { state ->
            deleteDataSourceDataLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
