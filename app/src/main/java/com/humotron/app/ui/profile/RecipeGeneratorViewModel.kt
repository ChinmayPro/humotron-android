package com.humotron.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.param.RecipeConfigRequest
import com.humotron.app.domain.modal.response.RecipeConfigResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class RecipeGeneratorViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val recipeConfigLiveData = MutableLiveData<Resource<RecipeConfigResponse>>()
    fun getRecipeConfigLiveData(): LiveData<Resource<RecipeConfigResponse>> = recipeConfigLiveData

    private val saveRecipeLiveData = MutableLiveData<Resource<RecipeConfigResponse>>()
    fun getSaveRecipeLiveData(): LiveData<Resource<RecipeConfigResponse>> = saveRecipeLiveData

    fun fetchRecipeConfig() {
        repository.getRecipeConfigAndPreferences().onEach { state ->
            recipeConfigLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun saveRecipeConfig(param: RecipeConfigRequest) {
        repository.saveRecipeConfigAndPreferences(param).onEach { state ->
            saveRecipeLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
