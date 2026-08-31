package com.humotron.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.CheckVersionBaseResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VersionCheckState {
    object Idle : VersionCheckState()
    object Loading : VersionCheckState()
    data class Success(val response: CheckVersionBaseResponse) : VersionCheckState()
    data class Error(val message: String) : VersionCheckState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appApi: AppApi
) : ViewModel() {

    private val _versionCheckState = MutableStateFlow<VersionCheckState>(VersionCheckState.Idle)
    val versionCheckState: StateFlow<VersionCheckState> = _versionCheckState

    fun checkAppVersion(currentVersion: String) {
        viewModelScope.launch {
            _versionCheckState.value = VersionCheckState.Loading
            try {
                val response = appApi.checkVersion(version = currentVersion)
                if (response.isSuccessful && response.body() != null) {
                    _versionCheckState.value = VersionCheckState.Success(response.body()!!)
                } else {
                    _versionCheckState.value = VersionCheckState.Error("API call failed")
                }
            } catch (e: Exception) {
                _versionCheckState.value = VersionCheckState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
