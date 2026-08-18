package com.humotron.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.param.InsightConfigRequest
import com.humotron.app.domain.modal.response.InsightConfigResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val insightConfigLiveData = MutableLiveData<Resource<InsightConfigResponse>>()
    fun getInsightConfigLiveData(): LiveData<Resource<InsightConfigResponse>> = insightConfigLiveData

    private val saveInsightLiveData = MutableLiveData<Resource<InsightConfigResponse>>()
    fun getSaveInsightLiveData(): LiveData<Resource<InsightConfigResponse>> = saveInsightLiveData

    fun fetchInsightConfig() {
        repository.getInsightConfigAndPreferences().onEach { state ->
            insightConfigLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun saveInsightConfig(param: InsightConfigRequest) {
        repository.saveInsightConfigAndPreferences(param).onEach { state ->
            saveInsightLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
