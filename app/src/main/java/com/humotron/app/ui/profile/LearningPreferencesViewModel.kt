package com.humotron.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.BioHackRepository
import com.humotron.app.domain.modal.response.NuggetsTypeAndLevelResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LearningPreferencesViewModel @Inject constructor(
    private val bioHackRepository: BioHackRepository
) : ViewModel() {

    private val learningPreferencesLiveData = MutableLiveData<Resource<NuggetsTypeAndLevelResponse>>()
    fun getLearningPreferencesLiveData(): LiveData<Resource<NuggetsTypeAndLevelResponse>> = learningPreferencesLiveData

    fun fetchLearningPreferences() {
        bioHackRepository.getNuggetsTypeAndLevel().onEach { state ->
            learningPreferencesLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
