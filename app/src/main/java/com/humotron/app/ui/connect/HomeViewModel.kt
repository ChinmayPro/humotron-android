package com.humotron.app.ui.connect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.data.local.entity.HrData
import com.humotron.app.data.local.entity.HrvData
import com.humotron.app.data.local.entity.StepData
import com.humotron.app.data.local.entity.TempData
import com.humotron.app.data.network.Status
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.domain.repository.sf
import com.humotron.app.domain.repository.toSleepEntity
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.TAG_RING_DEBUG
import com.humotron.app.util.loge
import com.humotron.app.util.todayCalendar
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lib.linktop.nexring.api.NexRingManager
import java.util.Calendar
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

    fun loadDateData() = Thread {
        PlutoLog.e(TAG_RING_DEBUG, "HomeViewModel loadDateData $currBtMac")
        with(NexRingManager.get().sleepApi()) {

            getDayCount(currBtMac) {
                PlutoLog.e(TAG_RING_DEBUG,"getDayCount -> $it")
                if (dayCount.value != it) {
                    dayCount.postValue(it)
                }
            }

            getRhr(currBtMac, selectedDate) {
                rhr.postValue(it)
            }
            for (item in 0..(dayCount.value ?: 0)) {
                val calender = Calendar.getInstance().apply {
                    this.add(Calendar.DATE, item * -1)
                }
                getSleepDataByDate(currBtMac, calender) { sleepDBDataList ->
                    if (!sleepDBDataList.isNullOrEmpty()) {
                        viewModelScope.launch {
                            sleepDBDataList.forEach { sleepItem ->
                                repository.saveSleepData(sleepItem.toSleepEntity())
                            }
                        }
                    }
                }

            }
            getHrList(currBtMac, 0, Long.MAX_VALUE) {
                loge("DCF", "getHrList list stat -> ${Gson().toJson(it)}")
                viewModelScope.launch {
                    val hrData = it?.second?.map {
                        HrData(time = it.ts, hr = it.value, timeStamp = sf.format(it.ts))
                    } ?: listOf()
                    repository.insertHrList(hrData)
                }
            }

            getHrvList(currBtMac, 0, Long.MAX_VALUE) {
                loge("DCF", "getHrvList list stat ->  ${Gson().toJson(it)}")
                viewModelScope.launch {
                    val hrvData = it?.second?.map {
                        HrvData(time = it.ts, hrv = it.value, timeStamp = sf.format(it.ts))
                    } ?: listOf()
                    repository.insertHrvList(hrvData)
                }
            }

            getStepsList(currBtMac, 0, Long.MAX_VALUE) {
                loge("DCF", "getStepsList  list stat -> ${Gson().toJson(it)}")
                viewModelScope.launch {
                    val stepData = it?.map {
                        StepData(time = it.ts, step = it.value, timeStamp = sf.format(it.ts))
                    } ?: listOf()
                    repository.insertSteps(stepData)
                }
            }

            getFingerTemperatureList(currBtMac, 0, Long.MAX_VALUE) {
                loge("DCF", "getFingerTemperatureList  list stat -> ${Gson().toJson(it)}")
                viewModelScope.launch {
                    val tempData = it?.second?.map {
                        TempData(time = it.ts, temp = it.value, timeStamp = sf.format(it.ts))
                    } ?: listOf()
                    repository.insertTemperature(tempData)
                }
            }
            prefUtils.setLong(Preference.RECORD_DATE, System.currentTimeMillis())
            uploadData()
        }
    }.start()


    fun uploadData() {
        loge("Upload device data ")
        repository.getUnSyncData().onEach {
            if (it.status == Status.SUCCESS) {
                it.data?.data?.let { data -> repository.updateData(data) }
            }
        }.launchIn(viewModelScope)
    }
}