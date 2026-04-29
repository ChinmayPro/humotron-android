package com.humotron.app.ui.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.domain.modal.param.BaselineScanDataParam
import com.humotron.app.domain.modal.param.SaveScanDataParam
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.HealthScanResponse
import com.humotron.app.domain.modal.response.HrvSaveScanResponse
import com.humotron.app.domain.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HealthScanViewModel @Inject constructor(
    private val sleepRepository: SleepRepository,
) : ViewModel() {

    private val baselineScanData: MutableLiveData<Resource<HealthScanResponse>> =
        MutableLiveData()

    private val saveScanDataResponse: MutableLiveData<Resource<HrvSaveScanResponse>> =
        MutableLiveData()

    fun getBaselineScanDataResponse(): MutableLiveData<Resource<HealthScanResponse>> {
        return baselineScanData
    }

    fun getSaveScanDataResponse(): MutableLiveData<Resource<HrvSaveScanResponse>> {
        return saveScanDataResponse
    }

    fun getBaselineScanData(baselineScanDataParam: BaselineScanDataParam) {
        sleepRepository.getBaselineScanData(baselineScanDataParam).onEach { state ->
            baselineScanData.value = state
        }.launchIn(viewModelScope)
    }

    fun saveScanData(saveScanDataParam: SaveScanDataParam) {
        sleepRepository.saveScanData(saveScanDataParam).onEach { state ->
            saveScanDataResponse.value = state
        }.launchIn(viewModelScope)
    }
}
