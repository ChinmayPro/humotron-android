package com.humotron.app.data.repository

import com.humotron.app.data.local.dao.SleepDao
import com.humotron.app.data.local.entity.HrMapper
import com.humotron.app.data.local.entity.HrvMapper
import com.humotron.app.data.local.entity.SleepMapper
import com.humotron.app.data.local.entity.StepMapper
import com.humotron.app.data.local.entity.TempMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceRepository(val dao: SleepDao) {

    val dateSf = SimpleDateFormat("MMM dd,yyyy", Locale.UK)
    val timeSf = SimpleDateFormat("h:mm a", Locale.UK)


    fun getLatestHeartRate(): Flow<HrMapper?> {
        return dao.getLatestHr().map { data ->
            data?.let {
                val date = Date(it.time)
                HrMapper(it.id, it.hr, dateSf.format(date), timeSf.format(date))
            }
        }
    }

    fun getLatestHrv(): Flow<HrvMapper?> {
        return dao.getLatestHrv().map { data ->
            data?.let {
                val date = Date(data.time)
                HrvMapper(data.id, data.hrv, dateSf.format(date), timeSf.format(date), data.time)
            }
        }
    }

    fun getLatestTemp(): Flow<TempMapper?> {
        return dao.getLatestTemp().map { data ->
            data?.let {
                val date = Date(data.time)
                TempMapper(data.id, data.temp, dateSf.format(date), timeSf.format(date))
            }
        }
    }

    fun getLatestStepData(): Flow<StepMapper?> {
        return dao.getLatestStepData().map { data ->
            data?.let {
                val date = Date(data.time)
                StepMapper(data.id, data.step, dateSf.format(date), timeSf.format(date))
            }
        }
    }

    fun getLatestSleepData(): Flow<SleepMapper?> {
        return dao.getLatestSleepData().map { data ->
            data?.let {
                val date = Date(data.endTs)
                SleepMapper(
                    data.id,
                    data.duration,
                    data.efficiency,
                    data.sleepStates.getOrNull(3)?.percent ?: 0f,
                    dateSf.format(date)
                )
            }
        }
    }




}