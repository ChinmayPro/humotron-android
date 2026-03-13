package com.humotron.app.ui.metrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.DeviceRepository
import com.humotron.app.domain.modal.param.DailyCalculatedMetricsParam
import com.humotron.app.domain.modal.param.RingReadingParam
import com.humotron.app.domain.modal.param.WristBandApiParam
import com.humotron.app.domain.modal.response.DailyCalculatedMetricsResponse
import com.humotron.app.domain.modal.response.MetricType
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.WristBandSleepDurationResponse
import com.humotron.app.domain.repository.SleepRepository
import com.humotron.app.util.DefaultDayReadingTimeSlotNavigator
import com.humotron.app.util.DefaultHourReadingTimeSlotNavigator
import com.humotron.app.util.DefaultWeekReadingTimeSlotNavigator
import com.humotron.app.util.ReadingTimeSlotNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class MetricDetailsViewModel @Inject constructor(
    val repository: DeviceRepository,
    val sleepRepository: SleepRepository,
) : ViewModel() {

    private var metric: MetricType? = null

    fun setMetric(metric: MetricType) {
        this.metric = metric
    }

    fun getMetric(): MetricType? {
        return metric
    }

    private val _dailyCalculatedMetrics =
        MutableLiveData<Resource<DailyCalculatedMetricsResponse>>()
    val dailyCalculatedMetrics: LiveData<Resource<DailyCalculatedMetricsResponse>> =
        _dailyCalculatedMetrics

    private val _wristBandSleepDuration =
        MutableLiveData<Resource<WristBandSleepDurationResponse>>()
    val wristBandSleepDuration: LiveData<Resource<WristBandSleepDurationResponse>> =
        _wristBandSleepDuration

    private var navigator: ReadingTimeSlotNavigator? = null

    private val _dateText = MutableStateFlow("")
    val dateText = _dateText.asStateFlow()

    private val _canGoBack = MutableStateFlow(true)
    val canGoBack = _canGoBack.asStateFlow()

    private val _canGoNext = MutableStateFlow(true)
    val canGoNext = _canGoNext.asStateFlow()

    private val _dateRange = MutableStateFlow(DateRange(null, null))
    val dateRange = _dateRange.asStateFlow()

    private val getRingReadingTemperatureLiveData: MutableLiveData<Resource<TemperatureResponse>> =
        MutableLiveData()

    fun getRingReadingTemperatureData(): LiveData<Resource<TemperatureResponse>> {
        return getRingReadingTemperatureLiveData
    }

    private val temperatureCache = mutableMapOf<String, Resource<TemperatureResponse>>()

    fun getDailyCalculatedMetrics(
        deviceId: String,
        metricId: String,
        startDate: String,
        endDate: String,
    ) {
        val timeZone = TimeZone.getDefault()
        val offset = timeZone.rawOffset
        val hours = offset / (60 * 60 * 1000)
        val minutes = abs(offset / (60 * 1000)) % 60
        val offsetString = String.format("%+03d:%02d", hours, minutes)

        val param = DailyCalculatedMetricsParam(
            offset = offsetString,
            metricId = metricId,
            startDate = startDate,
            endDate = endDate
        )

        viewModelScope.launch {
            sleepRepository.getDailyCalculatedMetrics(deviceId, param).collect {
                _dailyCalculatedMetrics.postValue(it)
            }
        }
    }

    fun getWristBandSleepDurationData(
        deviceId: String,
        startDate: String,
        endDate: String,
    ) {
        val timeZone = TimeZone.getDefault()
        val offset = timeZone.rawOffset
        val hours = offset / (60 * 60 * 1000)
        val minutes = abs(offset / (60 * 1000)) % 60
        val offsetString = String.format("%+03d:%02d", hours, minutes)

        viewModelScope.launch {
            sleepRepository.getWristBandSleepDurationData(deviceId, startDate, endDate, offsetString)
                .collect {
                    _wristBandSleepDuration.postValue(it)
                }
        }
    }

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

        sleepRepository.getWristBandGraphData(deviceId,
            param).onEach { state ->
            if (state.status == Status.SUCCESS) {
                temperatureCache[cacheKey] = state
            }
            getRingReadingTemperatureLiveData.value = state
        }.launchIn(viewModelScope)
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
}

data class DateRange(
    val start: String?,
    val end: String?,
)
