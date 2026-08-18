package com.humotron.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.param.HealthProfileConfigRequest
import com.humotron.app.domain.modal.response.HealthProfileConfigResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val healthProfileConfigLiveData = MutableLiveData<Resource<HealthProfileConfigResponse>>()
    fun getHealthProfileConfigLiveData(): LiveData<Resource<HealthProfileConfigResponse>> = healthProfileConfigLiveData

    private val saveGoalsLiveData = MutableLiveData<Resource<HealthProfileConfigResponse>>()
    fun getSaveGoalsLiveData(): LiveData<Resource<HealthProfileConfigResponse>> = saveGoalsLiveData

    fun fetchHealthProfileConfig() {
        repository.getHealthProfileConfigAndPreferences().onEach { state ->
            healthProfileConfigLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun saveGoals(healthGoals: List<String>, medicalConditions: List<String> = listOf("asthma")) {
        val param = HealthProfileConfigRequest(
            healthGoals = healthGoals,
            medicalConditions = medicalConditions
        )
        repository.saveHealthProfileConfigAndPreferences(param).onEach { state ->
            saveGoalsLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
