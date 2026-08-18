package com.humotron.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.param.ChatConfigRequest
import com.humotron.app.domain.modal.response.ChatConfigResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val chatConfigLiveData = MutableLiveData<Resource<ChatConfigResponse>>()
    fun getChatConfigLiveData(): LiveData<Resource<ChatConfigResponse>> = chatConfigLiveData

    private val saveChatLiveData = MutableLiveData<Resource<ChatConfigResponse>>()
    fun getSaveChatLiveData(): LiveData<Resource<ChatConfigResponse>> = saveChatLiveData

    fun fetchChatConfig() {
        repository.getChatConfigAndPreferences().onEach { state ->
            chatConfigLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun saveChatConfig(param: ChatConfigRequest) {
        repository.saveChatConfigAndPreferences(param).onEach { state ->
            saveChatLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
