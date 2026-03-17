package com.humotron.app.domain.repository

import com.google.gson.Gson
import com.humotron.app.core.Preference
import com.humotron.app.data.local.dao.SleepDao
import com.humotron.app.data.local.entity.DoubleUploadMapper
import com.humotron.app.data.local.entity.HrData
import com.humotron.app.data.local.entity.HrvData
import com.humotron.app.data.local.entity.IntUploadMapper
import com.humotron.app.data.local.entity.SleepEntity
import com.humotron.app.data.local.entity.SleepUploadMapper
import com.humotron.app.data.local.entity.StepData
import com.humotron.app.data.local.entity.TempData
import com.humotron.app.data.local.entity.UploadData
import com.humotron.app.data.local.entity.UploadDeviceData
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.param.DailyCalculatedMetricsParam
import com.humotron.app.domain.modal.param.RingReadingParam
import com.humotron.app.domain.modal.param.WristBandApiParam
import com.humotron.app.domain.modal.response.AddDeviceDataResponse
import com.humotron.app.domain.modal.response.AddHardwareResponse
import com.humotron.app.domain.modal.response.AllMetricsResponse
import com.humotron.app.domain.modal.response.DailyCalculatedMetricsResponse
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.response.HardwareListData
import com.humotron.app.domain.modal.response.MetricResponse
import com.humotron.app.domain.modal.response.RingReadingData
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.WristBandSleepDurationResponse
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.loge
import com.pluto.Pluto
import com.pluto.plugins.logger.PlutoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import lib.linktop.nexring.api.SleepData
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


val sf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}


class SleepRepository(
    private val api: AppApi,
    private val sleepDao: SleepDao,
    private val prefUtils: PrefUtils,
    private val responseHandler: ResponseHandler,
) {

    private val _deviceCache =
        MutableStateFlow<Resource<GetAllDeviceResponse>?>(null)

    val deviceCache = _deviceCache.asStateFlow()

    suspend fun saveSleepData(data: SleepEntity) {
        sleepDao.insert(data)
    }

    suspend fun insertHrList(data: List<HrData>) {
        sleepDao.insertHrList(data)
    }

    suspend fun insertHrvList(data: List<HrvData>) {
        sleepDao.insertHrvList(data)
    }

    suspend fun insertSteps(data: List<StepData>) {
        sleepDao.insertSteps(data)
    }

    suspend fun insertTemperature(data: List<TempData>) {
        sleepDao.insertTemperature(data)
    }


    val hrFlow = sleepDao.getUnSyncHr()
    val hrvFlow = sleepDao.getUnSyncHrv()
    val stepFlow = sleepDao.getUnSyncStepData()
    val sleepFlow = sleepDao.getUnSyncSleepData()
    val tempFlow = sleepDao.getUnSyncTemp()

    fun getUnSyncData(): Flow<Resource<AddDeviceDataResponse>> = flow {
        val hrList = hrFlow.first()
        val hrvList = hrvFlow.first()
        val stepList = stepFlow.first()
        val sleepList = sleepFlow.first()
        val tempList = tempFlow.first()
        val emptyData =
            hrList.isEmpty() && hrvList.isEmpty() && stepList.isEmpty() && sleepList.isEmpty() && tempList.isEmpty()
        if (emptyData) {
            emit(Resource.success(AddDeviceDataResponse(null, null, null)))
            return@flow
        }

        val hrUploadMapper = hrList.map {
            IntUploadMapper("HeartRate", it.hr, sf.format(it.time))
        }
        val hrvUploadMapper = hrvList.map {
            IntUploadMapper("HRV", it.hrv, sf.format(it.time))
        }
        val stepUploadMapper = stepList.map {
            IntUploadMapper("STEPS", it.step, sf.format(it.time))
        }
        val tempUploadMapper = tempList.map {
            DoubleUploadMapper("Temperature", it.temp, sf.format(it.time))
        }
        val sleepUploadMapper = sleepList.map {
            SleepUploadMapper(
                spo2 = it.spo2?.toInt() ?: 0,
                sleepStart = (it.startTs / 1000L),
                hr = it.hr,
                efficiency = it.efficiency,
                duration = it.duration / 1000L,
                time = sf.format(it.startTs),
                type = "Sleep",
                hrv = it.hrv,
                br = it.rr,
                hrDip = it.hrDip,
                sleepEnd = (it.endTs / 1000L),
                deepDuration = it.duration / 1000L,
                isNap = it.isNap
            )
        }

        val hardwareID = prefUtils.getHardwareId() ?: ""
        val recordTime = prefUtils.getLong(Preference.RECORD_DATE)
        val uploadData = UploadData(
            hardwareID, UploadDeviceData(
                heartRate = hrUploadMapper,
                heartRateVariability = hrvUploadMapper,
                temperature = tempUploadMapper,
                steps = stepUploadMapper,
                sleep = sleepUploadMapper
            ), recordTime / 1000
        )
        loge("SleepRepository", Gson().toJson(uploadData))

        try {
            val response =
                responseHandler.handleResponse(api.sendDataToServer(uploadData), false)

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }


    fun addHardwareInProfile(hardware: AddHardware): Flow<Resource<AddHardwareResponse>> = flow {
        try {
            val response =
                responseHandler.handleResponse(api.addHardwareId(hardware), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getUserDeviceData(forceRefresh: Boolean = false) {
        if (!forceRefresh && _deviceCache.value != null) return
        CoroutineScope(Dispatchers.IO).launch {
            _deviceCache.value = Resource.loading()
            try {
                val response =
                    responseHandler.handleResponse(api.getAllDeviceData(), false)
                _deviceCache.value = response
            } catch (e: Exception) {
                _deviceCache.value =
                    responseHandler.handleException(e)
            }
        }
    }

    fun getHardwareList(): Flow<Resource<HardwareListData>> = flow {
        try {
            val response =
                responseHandler.handleResponse(api.getHardwareList(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getRingReadingData(deviceId: String): Flow<Resource<RingReadingData>> = flow {
        try {
            val response =
                responseHandler.handleResponse(api.getRingReadingData(deviceId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getRingReadingGraphData(
        endpoint: String,
        ringId: String,
        param: RingReadingParam,
    ): Flow<Resource<TemperatureResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.getRingReadingGraphData(endpoint, ringId, param),
                    false
                )
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWristBandGraphData(
        deviceId: String,
        param: WristBandApiParam,
    ): Flow<Resource<TemperatureResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWristBandGraphData(deviceId, param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getDailyCalculatedMetrics(
        deviceId: String,
        param: DailyCalculatedMetricsParam,
    ): Flow<Resource<DailyCalculatedMetricsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.getDailyCalculatedMetrics(deviceId, param),
                    false
                )
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getRecommendationsByMetricId(metricId: String): Flow<Resource<MetricResponse>> = flow {
        try {
            val response =
                responseHandler.handleResponse(api.getRecommendationsByMetricId(metricId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllMetricsByDeviceId(deviceId: String): Flow<Resource<AllMetricsResponse>> = flow {
        try {
            val response =
                responseHandler.handleResponse(api.getAllMetricsByDeviceId(deviceId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWristBandSleepDurationData(
        deviceId: String,
        startDate: String,
        endDate: String,
        offset: String,
    ): Flow<Resource<WristBandSleepDurationResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.getWristBandSleepDurationData(deviceId, startDate, endDate, offset),
                    false
                )
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }


    suspend fun updateData(data: AddDeviceDataResponse.Data) {
        data.hardwareSpecificDetail?.data?.let { deviceData ->
            deviceData.hrv?.mapNotNull { it.date }?.let {
                sleepDao.syncHrvData(it)
            }

            deviceData.steps?.mapNotNull { it.date }?.let {
                sleepDao.syncStepData(it)
            }

            deviceData.heartRate?.mapNotNull { it.date }?.let {
                sleepDao.syncHrData(it)
            }

            deviceData.temperature?.mapNotNull { it.date }?.let {
                sleepDao.syncTempData(it)
            }

            deviceData.sleep?.mapNotNull { it.time }?.let {
                sleepDao.syncSleepData(it)
            }
        }
    }


}


fun SleepData.toSleepEntity(): SleepEntity {
    return SleepEntity(
        btMac = btMac,
        startTs = startTs,
        endTs = endTs,
        sleepStages = sleepStages,
        sleepStates = sleepStates.toList(),
        hr = hr,
        rr = rr,
        spo2 = spo2,
        hrv = hrv,
        hrDip = hrDip,
        efficiency = efficiency,
        duration = duration,
        isNap = isNap,
        startTimeStamp = sf.format(startTs),
        endTimeStamp = sf.format(endTs)
    )
}
