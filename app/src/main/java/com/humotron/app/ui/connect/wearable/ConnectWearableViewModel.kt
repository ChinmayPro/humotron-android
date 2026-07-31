package com.humotron.app.ui.connect.wearable

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.domain.modal.param.ConfirmWearableConnectionParam
import com.humotron.app.domain.modal.param.ConnectGoogleHealthParam
import com.humotron.app.domain.modal.param.ConnectProviderParam
import com.humotron.app.domain.modal.param.SyncWearableDataParam
import com.humotron.app.domain.modal.response.ConfirmWearableConnectionResponse
import com.humotron.app.domain.modal.response.ConnectWearableResponse
import com.humotron.app.domain.modal.response.GoogleHealthBackfillResponse
import com.humotron.app.domain.modal.response.ProviderResponse
import com.humotron.app.domain.modal.response.SyncWearableDataResponse
import com.humotron.app.domain.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectWearableViewModel @Inject constructor(
    private val repository: SleepRepository,
) : ViewModel() {

    private val _providers = MutableLiveData<Resource<ProviderResponse>>(Resource.loading())
    val providers: LiveData<Resource<ProviderResponse>> = _providers

    private val _connectResponse = MutableLiveData<Resource<ConnectWearableResponse>>()
    val connectResponse: LiveData<Resource<ConnectWearableResponse>> = _connectResponse

    private val _confirmResponse = MutableLiveData<Resource<ConfirmWearableConnectionResponse>>()
    val confirmResponse: LiveData<Resource<ConfirmWearableConnectionResponse>> = _confirmResponse

    private val _syncResponse = MutableLiveData<Resource<SyncWearableDataResponse>>()
    val syncResponse: LiveData<Resource<SyncWearableDataResponse>> = _syncResponse

    private val _backfillResponse = MutableLiveData<Resource<GoogleHealthBackfillResponse>>()
    val backfillResponse: LiveData<Resource<GoogleHealthBackfillResponse>> = _backfillResponse

    init {
        getAllProviders()
    }

    fun getAllProviders() {
        viewModelScope.launch {
            repository.getAllProvider().collectLatest {
                _providers.value = it
            }
        }
    }

    fun connectWearableProvider(provider: String) {
        viewModelScope.launch {
            repository.connectWearableProvider(ConnectProviderParam(provider)).collectLatest {
                _connectResponse.value = it
            }
        }
    }

    fun connectGoogleHealth() {
        viewModelScope.launch {
            repository.connectGoogleHealth(ConnectGoogleHealthParam()).collectLatest {
                _connectResponse.value = it
            }
        }
    }

    fun confirmWearableConnection(provider: String) {
        viewModelScope.launch {
            repository.confirmWearableConnection(ConfirmWearableConnectionParam(provider))
                .collectLatest {
                    _confirmResponse.value = it
                }
        }
    }

    fun getGoogleHealthStatus() {
        viewModelScope.launch {
            repository.getGoogleHealthStatus().collectLatest {
                _confirmResponse.value = it
            }
        }
    }

    fun backfillGoogleHealth() {
        viewModelScope.launch {
            repository.backfillGoogleHealth().collectLatest {
                _backfillResponse.value = it
            }
        }
    }

    fun syncWearableData(provider: String) {
        viewModelScope.launch {
            repository.syncWearableData(SyncWearableDataParam(provider))
                .collectLatest {
                    _syncResponse.value = it
                }
        }
    }

    fun resetConnectResponse() {
        _connectResponse.value = null
    }
}
