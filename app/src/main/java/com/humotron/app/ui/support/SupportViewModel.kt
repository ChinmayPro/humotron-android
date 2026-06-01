package com.humotron.app.ui.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.SupportRepository
import com.humotron.app.domain.modal.response.SupportHomeResponse
import com.humotron.app.domain.modal.response.MyTicketsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val supportRepository: SupportRepository
) : ViewModel() {

    private val _supportHomeData = MutableLiveData<Resource<SupportHomeResponse>>()
    val supportHomeData: LiveData<Resource<SupportHomeResponse>> get() = _supportHomeData

    private val _myTicketsData = MutableLiveData<Resource<MyTicketsResponse>>()
    val myTicketsData: LiveData<Resource<MyTicketsResponse>> get() = _myTicketsData

    fun fetchSupportHomeData() {
        supportRepository.getSupportHome().onEach { resource ->
            _supportHomeData.value = resource
        }.launchIn(viewModelScope)
    }

    fun fetchMyTickets() {
        supportRepository.getMyTickets().onEach { resource ->
            _myTicketsData.value = resource
        }.launchIn(viewModelScope)
    }
}
