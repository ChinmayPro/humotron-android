package com.humotron.app.ui.device

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.local.entity.HrMapper
import com.humotron.app.data.local.entity.HrvMapper
import com.humotron.app.data.local.entity.SleepMapper
import com.humotron.app.data.local.entity.StepMapper
import com.humotron.app.data.local.entity.TempMapper
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.DeviceRepository
import com.humotron.app.data.repository.MedicalRepository
import com.humotron.app.domain.modal.param.GetAllScanByTypeParam
import com.humotron.app.domain.modal.param.RingReadingParam
import com.humotron.app.domain.modal.param.WristBandApiParam
import com.humotron.app.domain.modal.response.AllMetricsResponse
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.response.HardwareListData
import com.humotron.app.domain.modal.response.MedicalPdfResponse
import com.humotron.app.domain.modal.response.MergedAssessmentResponse
import com.humotron.app.domain.modal.response.MetricResponse
import com.humotron.app.domain.modal.response.PastScanResponse
import com.humotron.app.domain.modal.response.RingReadingData
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.DefaultDayReadingTimeSlotNavigator
import com.humotron.app.util.DefaultHourReadingTimeSlotNavigator
import com.humotron.app.util.DefaultWeekReadingTimeSlotNavigator
import com.humotron.app.util.ReadingTimeSlotNavigator
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    val repository: DeviceRepository,
    val sleepRepository: SleepRepository,
    val medicalRepository: MedicalRepository,
) : ViewModel() {

    private var navigator: ReadingTimeSlotNavigator? = null

    private val _dateText = MutableStateFlow("")
    val dateText = _dateText.asStateFlow()

    private val _canGoBack = MutableStateFlow(true)
    val canGoBack = _canGoBack.asStateFlow()

    private val _canGoNext = MutableStateFlow(true)
    val canGoNext = _canGoNext.asStateFlow()

    private val _dateRange = MutableStateFlow(DateRange(null, null))
    val dateRange = _dateRange.asStateFlow()

    init {
        updateText()
    }

    val deviceData = MutableStateFlow(DeviceDataState())

    fun getDeviceData() {
        getHrLatestData()
        getHrvLatestData()
        getTempLatestData()
        getStepLatestData()
        getSleepData()
    }

    private val getDeviceListLiveData: MutableLiveData<Resource<GetAllDeviceResponse>> =
        MutableLiveData()

    fun getDeviceListData(): LiveData<Resource<GetAllDeviceResponse>> {
        return getDeviceListLiveData
    }

   /* fun getUserDeviceData() {
        sleepRepository.getUserDeviceData().onEach { state ->
            getDeviceListLiveData.value = state
        }.launchIn(viewModelScope)
    }*/

    fun observeUserDeviceData() {
        sleepRepository.deviceCache
            .onEach { state ->
                state?.let {
                    getDeviceListLiveData.value = it
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshUserDeviceData(forceRefresh: Boolean = false) {
        sleepRepository.getUserDeviceData(forceRefresh)
    }

    private val getHardwareListLiveData: MutableLiveData<Resource<HardwareListData>> =
        MutableLiveData()

    fun getHardwareListData(): LiveData<Resource<HardwareListData>> {
        return getHardwareListLiveData
    }

    fun getHardwareList() {
        sleepRepository.getHardwareList().onEach { state ->
            getHardwareListLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val getRingReadingLiveData: MutableLiveData<Resource<RingReadingData>> =
        MutableLiveData()

    fun getRingReadingData(): LiveData<Resource<RingReadingData>> {
        return getRingReadingLiveData
    }

    fun getRingReadingData(deviceId: String) {
        sleepRepository.getRingReadingData(deviceId).onEach { state ->
            getRingReadingLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val getRingReadingTemperatureLiveData: MutableLiveData<Resource<TemperatureResponse>> =
        MutableLiveData()

    fun getRingReadingTemperatureData(): LiveData<Resource<TemperatureResponse>> {
        return getRingReadingTemperatureLiveData
    }

    private val temperatureCache = mutableMapOf<String, Resource<TemperatureResponse>>()

    fun getRingReadingGraphData(endpoint: String, ringId: String, param: RingReadingParam) {
        val cacheKey = "${endpoint}_${ringId}_${param.range}_${param.startDate}_${param.endDate}"
        if (temperatureCache.containsKey(cacheKey)) {
            getRingReadingTemperatureLiveData.value = temperatureCache[cacheKey]
            return
        }

        sleepRepository.getRingReadingGraphData(endpoint, ringId, param).onEach { state ->
            if (state.status == Status.SUCCESS) {
                temperatureCache[cacheKey] = state
            }
            getRingReadingTemperatureLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun getWristBandGraphData(deviceId: String, param: WristBandApiParam) {
        val cacheKey = "wristBand_${deviceId}_${param.range}_${param.startDate}_${param.endDate}"
        if (temperatureCache.containsKey(cacheKey)) {
            getRingReadingTemperatureLiveData.value = temperatureCache[cacheKey]
            return
        }

        sleepRepository.getWristBandGraphData(deviceId, param).onEach { state ->
            if (state.status == Status.SUCCESS) {
                temperatureCache[cacheKey] = state
            }
            getRingReadingTemperatureLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val getRecommendationsByMetricIdLiveData: MutableLiveData<Resource<MetricResponse>> =
        MutableLiveData()

    fun getRecommendationsByMetricIdData(): LiveData<Resource<MetricResponse>> {
        return getRecommendationsByMetricIdLiveData
    }

    fun getRecommendationsByMetricId(metricId: String) {
        sleepRepository.getRecommendationsByMetricId(metricId).onEach { state ->
            getRecommendationsByMetricIdLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val getAllMetricsByDeviceIdLiveData: MutableLiveData<Resource<AllMetricsResponse>> =
        MutableLiveData()

    fun getAllMetricsByDeviceIdData(): LiveData<Resource<AllMetricsResponse>> {
        return getAllMetricsByDeviceIdLiveData
    }

    fun getAllMetricsByDeviceId(deviceId: String) {
        sleepRepository.getAllMetricsByDeviceId(deviceId).onEach { state ->
            getAllMetricsByDeviceIdLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val _mergedAssessmentListLiveData: MutableLiveData<Resource<MergedAssessmentResponse>> =
        MutableLiveData()

    val mergedAssessmentListLiveData: LiveData<Resource<MergedAssessmentResponse>> = _mergedAssessmentListLiveData

    fun getMergedAssessmentList(forceRefresh: Boolean = false) {
        if (!forceRefresh && _mergedAssessmentListLiveData.value?.status == Status.SUCCESS) return
        
        sleepRepository.getMergedAssessmentList().onEach { state ->
            _mergedAssessmentListLiveData.value = state
            Log.e("TAG", "initObsdwdddfervers: $state ", )

        }.launchIn(viewModelScope)
    }

    private val _medicalPdfListLiveData: MutableLiveData<Resource<MedicalPdfResponse>> =
        MutableLiveData()

    val medicalPdfListLiveData: LiveData<Resource<MedicalPdfResponse>> = _medicalPdfListLiveData

    fun getMedicalPdfList(forceRefresh: Boolean = false) {
        if (!forceRefresh && _medicalPdfListLiveData.value?.status == Status.SUCCESS) return
        
        medicalRepository.getAllPdfList().onEach { state ->
            _medicalPdfListLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val _removePdfLiveData: SingleLiveEvent<Resource<CommonResponse>> =
        SingleLiveEvent()

    val removePdfLiveData: LiveData<Resource<CommonResponse>> = _removePdfLiveData

    fun removePdfByPdfId(pdfId: String) {
        medicalRepository.removePdfByPdfId(pdfId).onEach { state ->
            _removePdfLiveData.value = state
            if (state.status == Status.SUCCESS) {
                getMedicalPdfList(true)
            }
        }.launchIn(viewModelScope)
    }


    private val _pastScanLiveData: MutableLiveData<Resource<PastScanResponse>> =
        MutableLiveData()

    val pastScanLiveData: LiveData<Resource<PastScanResponse>> = _pastScanLiveData

    fun getAllScanByType(type: String, deviceId: String) {
        sleepRepository.getAllScanByType(GetAllScanByTypeParam(type, deviceId)).onEach { state ->
            _pastScanLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun getHrLatestData() {
        viewModelScope.launch {
            repository.getLatestHeartRate().collect { hr ->
                hr?.let {
                    deviceData.update { it.copy(hrMapper = hr) }
                }
            }
        }
    }

    fun getHrvLatestData() {
        viewModelScope.launch {
            repository.getLatestHrv().collect { hrv ->
                hrv?.let {
                    deviceData.update { it.copy(hrvMapper = hrv) }
                }

            }
        }
    }

    fun getTempLatestData() {
        viewModelScope.launch {
            repository.getLatestTemp().collect { temp ->
                temp?.let {
                    deviceData.update { it.copy(tempMapper = temp) }
                }
            }
        }
    }

    fun getStepLatestData() {
        viewModelScope.launch {
            repository.getLatestStepData().collect { step ->
                step?.let {
                    deviceData.update { it.copy(stepMapper = step) }
                }
            }
        }
    }

    fun getSleepData() {
        viewModelScope.launch {
            repository.getLatestSleepData().collect { sleep ->
                sleep?.let {
                    deviceData.update { it.copy(sleepMapper = sleep) }
                }
            }
        }
    }

    fun setMode(mode: String?, apiDateString: String) {
        if (apiDateString.isBlank()) return

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val parsedDate = runCatching {
            OffsetDateTime.parse(apiDateString).toLocalDateTime()
        }.recoverCatching {
            OffsetDateTime.parse(apiDateString, formatter).toLocalDateTime()
        }.getOrNull() ?: return

        navigator = when (mode) {
            "Hour" -> DefaultHourReadingTimeSlotNavigator(currentDate = parsedDate)
            "Day" -> DefaultDayReadingTimeSlotNavigator(currentDate = parsedDate.toLocalDate())
            "Week" -> DefaultWeekReadingTimeSlotNavigator(currentDate = parsedDate.toLocalDate())
            else -> DefaultDayReadingTimeSlotNavigator(currentDate = parsedDate.toLocalDate())
        }
        updateText()
    }

    fun previous() {
        navigator?.let {
            it.goToPreviousTimeRangeSlot()
            updateText()
        }
    }

    fun next() {
        navigator?.let {
            it.goToNextTimeRangeSlot()
            updateText()
        }
    }

    private fun updateText() {
        navigator?.let {
            _dateText.value = it.getCurrentTimeRangeSlot()
            _canGoBack.value = it.canGoBack
            _canGoNext.value = it.canGoNext
            _dateRange.value = DateRange(it.startDateStringUTC, it.endDateStringUTC)
        }
    }

    public fun formatXAxisLabel(time: String?, tab: String?): String {
        if (time.isNullOrEmpty()) return ""
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = inputFormat.parse(time) ?: return ""

        return when (tab) {
            "Hour" -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }

            "Day" -> {
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
            }

            "Week" -> {
                SimpleDateFormat("EEE", Locale.getDefault()).format(date) // Sun, Mon
            }

            else -> ""
        }
    }

    fun getTimeAgo(dataSync: String?): String {
        if (dataSync.isNullOrBlank()) return "NA"

        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

            val syncInstant = runCatching {
                OffsetDateTime.parse(dataSync).toInstant()
            }.recoverCatching {
                OffsetDateTime.parse(dataSync, formatter).toInstant()
            }.getOrNull() ?: return "NA"

            val now = Instant.now()
            val seconds = Duration.between(syncInstant, now).seconds

            when {
                seconds < 0 -> "Just now"

                seconds < 10 -> "Just now"

                seconds < 60 -> "$seconds seconds ago"

                seconds < 3600 -> {
                    val minutes = seconds / 60
                    if (minutes == 1L) "1 minute ago"
                    else "$minutes minutes ago"
                }

                seconds < 86400 -> {
                    val hours = seconds / 3600
                    if (hours == 1L) "1 hour ago"
                    else "$hours hours ago"
                }

                seconds < 604800 -> {
                    val days = seconds / 86400
                    if (days == 1L) "1 day ago"
                    else "$days days ago"
                }

                seconds < 2592000 -> {
                    val weeks = seconds / 604800
                    if (weeks == 1L) "1 week ago"
                    else "$weeks weeks ago"
                }

                else -> {
                    val months = seconds / 2592000
                    if (months == 1L) "1 month ago"
                    else "$months months ago"
                }
            }

        } catch (e: Exception) {
            "NA"
        }
    }
}

data class DeviceDataState(
    val hrMapper: HrMapper = HrMapper(),
    val hrvMapper: HrvMapper = HrvMapper(),
    val tempMapper: TempMapper = TempMapper(),
    val stepMapper: StepMapper = StepMapper(),
    val sleepMapper: SleepMapper = SleepMapper(),
)

data class DateRange(
    val start: String?,
    val end: String?,
)