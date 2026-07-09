package com.humotron.app.ui.connect

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.core.App
import com.humotron.app.data.network.Status
import com.humotron.app.domain.modal.param.DeviceMetaDataParam
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.TAG_RING_DEBUG
import com.humotron.app.util.todayCalendar
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    val repository: SleepRepository,
    val prefUtils: PrefUtils,
    private val bandBleManager: BandBleManager,
) : AndroidViewModel(application) {

    var currBtMac: String = ""
    var selectedDate = todayCalendar()
    val dayCount = MutableLiveData(-1)
    val rhr = MutableLiveData<Int>()


    init {
        getApplication<App>().ringDeviceManager.homeViewModel = this
    }

    override fun onCleared() {
        getApplication<App>().ringDeviceManager.homeViewModel = null
        getApplication<App>().ringBleManager.disconnect()
        bandBleManager.disconnect()
        super.onCleared()
    }

    fun uploadData() {
        repository.getUnSyncData().onEach {
            if (it.status == Status.SUCCESS) {
                it.data?.data?.let { data -> repository.updateData(data) }
            }
        }.launchIn(viewModelScope)
    }

    fun uploadRingData() {
        Log.e("RingApp", "uploadRingData: " )
        viewModelScope.launch {
            val result = repository.syncRingDataOnce()
            if (result.status == Status.SUCCESS) {
                PlutoLog.e(TAG_RING_DEBUG, "Ring Data Uploaded Successfully")
            }
        }
    }

    fun addDeviceMetaData(metaData: DeviceMetaDataParam) {
        repository.addDeviceMetaData(metaData).onEach {
            if (it.status == Status.SUCCESS) {
                PlutoLog.e(TAG_RING_DEBUG, "Device Meta Data Uploaded Successfully")
            }
        }.launchIn(viewModelScope)
    }
}
