package com.humotron.app.data.local.entity.ring

import com.google.gson.Gson
import com.smartsleep.sleepstaging.ActData
import com.smartsleep.sleepstaging.HealthData
import com.smartsleep.sleepstaging.SleepStagingNative
import com.smartsleep.sleepstaging.SleepStagingType
import com.smartsleep.sleepstaging.SlpWakeData
import com.smartsleep.sleepstaging.Spo2Data

data class RingSleepStageItem(
    val type: String,
    val startTs: Long,
    val endTs: Long,
)

fun buildRingSleepSessions(
    hardwareId: String,
    ringHistoricalData: List<RingHistoricalDataEntity>,
    sleepEvents: List<RingSleepEventEntity>,
    activityIntensity: List<RingActivityIntensityEntity>,
): List<RingSleepSessionEntity> {
    val healthDataList = ringHistoricalData
        .asSequence()
        .filter { it.sourceType == 0 }
        .sortedBy { it.ts }
        .mapNotNull { item ->
            val hr = item.heartRate ?: return@mapNotNull null
            HealthData(
                item.ts,
                hr,
                item.hrv ?: 0,
                item.motion ?: 0,
                item.totalSteps ?: 0,
            )
        }
        .toList()

    if (healthDataList.size < 4) return emptyList()

    val slpWakeList = sleepEvents.map {
        SlpWakeData(it.sleepTs, it.type, it.bedRestDuration.toLong(), it.awakeningOrder)
    }

    val spo2DataList227 = ringHistoricalData
        .asSequence()
        .filter { it.sourceType == 227 && it.spo2 != null }
        .sortedBy { it.ts }
        .map {
            Spo2Data(it.ts, it.spo2 ?: 0, it.t90?.toInt() ?: 0)
        }
        .toList()

    val spo2DataList228 = ringHistoricalData
        .asSequence()
        .filter { it.sourceType == 228 && it.spo2 != null }
        .sortedBy { it.ts }
        .map {
            Spo2Data(it.ts, it.spo2 ?: 0, it.t90?.toInt() ?: 0)
        }
        .toList()

    val actDataList = activityIntensity
        .asSequence()
        .sortedBy { it.ts }
        .map {
            ActData(it.ts, it.intensity, it.steps)
        }
        .toList()

    val result = SleepStagingNative.analysisSleepFusion(
        slpWakeList,
        healthDataList,
        spo2DataList227,
        spo2DataList228,
        actDataList,
    )

    val gson = Gson()

    return result.stagingList.orEmpty().map { session ->
        val startTs = session.startTime
        val endTs = session.endTime
        val duration = (endTs - startTs).coerceAtLeast(0L)
        val sleepDuration = session.stagingList.sumOf { stage ->
            val stageDuration = (stage.endTime - stage.startTime).coerceAtLeast(0L)
            if (stage.stagingType == SleepStagingType.WAKE) 0L else stageDuration
        }
        val efficiency = if (duration > 0L) {
            sleepDuration.toDouble() * 100.0 / duration.toDouble()
        } else {
            0.0
        }

        val window = ringHistoricalData.filter { it.ts in startTs..endTs }
        val avgHrv = window.mapNotNull { it.hrv?.toDouble() }.averageOrNull()
        val avgRr = window.mapNotNull { it.rr?.toDouble() }.averageOrNull()
        val avgSpo2 = window.mapNotNull { it.spo2?.toDouble() }.averageOrNull()
            ?: session.t90?.data?.map { it.spo2.toDouble() }?.averageOrNull()

        val isNap = session.stagingList.any { it.stagingType == SleepStagingType.NAP }
        var sleepLatencyStartTs: Long? = null
        var sleepLatencyEndTs: Long? = null
        if (!isNap) {
            session.stagingList.firstOrNull()?.let { firstStage ->
                if (firstStage.stagingType == SleepStagingType.WAKE) {
                    sleepLatencyStartTs = firstStage.startTime
                    sleepLatencyEndTs = firstStage.endTime
                }
            }
        }

        RingSleepSessionEntity(
            hardwareId = hardwareId,
            startTs = startTs / 1000,
            endTs = endTs / 1000,
            duration = duration,
            efficiency = efficiency,
            avgHr = session.averageHr,
            hrv = avgHrv ?: 0.0,
            rr = avgRr ?: 0.0,
            spo2 = avgSpo2,
            restHr = session.restHr.toDouble(),
            t90 = session.t90?.value,
            sleepLatencyStartTs = sleepLatencyStartTs,
            sleepLatencyEndTs = sleepLatencyEndTs,
            isNap = isNap,
            stagesJson = gson.toJson(
                session.stagingList.map {
                    RingSleepStageItem(
                        type = it.stagingType.toString(),
                        startTs = it.startTime,
                        endTs = it.endTime,
                    )
                }
            ),
            sync = false,
        )
    }
}

private fun List<Double>.averageOrNull(): Double? = if (isEmpty()) null else average()
