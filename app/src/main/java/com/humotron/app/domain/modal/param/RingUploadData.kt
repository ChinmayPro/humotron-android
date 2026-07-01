package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class RingUploadData(
    val hardwareId: String,
    val data: RingUploadDeviceData,
    val recordTimestamp: Long,
    @SerializedName("deviceType")
    val deviceType: String = "RING",
    @SerializedName("isRing")
    val isRing: Boolean = true,
)

data class RingUploadDeviceData(
    @SerializedName("HISTORICAL")
    val historical: List<RingHistoricalReading> = emptyList(),

    @SerializedName("SLEEP_EVENTS")
    val sleepEvents: List<RingSleepEvent> = emptyList(),

    @SerializedName("ACTIVITY_INTENSITY")
    val activityIntensity: List<RingActivityIntensity> = emptyList(),

    @SerializedName("SLEEP_SESSIONS")
    val sleepSessions: List<RingSleepSession> = emptyList(),
)

data class RingHistoricalReading(
    val ts: Long,
    val sourceType: Int,
    val uuid: Int,
    val heartRate: Int? = null,
    val hrv: Int? = null,
    val spo2: Int? = null,
    val rr: Int? = null,
    val skinTemperature: Double? = null,
    val totalSteps: Int? = null,
    val motion: Int? = null,
    val batteryLevel: Int? = null,
    val workout: Int? = null,
    val ibi: Int? = null,
    val cc: Double? = null,
    val t90: Int? = null,
)

data class RingSleepEvent(
    val sleepTs: Long,
    val type: Int,
    val bedRestDuration: Int,
    val awakeningOrder: Int,
)

data class RingActivityIntensity(
    val ts: Long,
    val intensity: Int,
    val steps: Int,
)

data class RingSleepSession(
    val startTs: Long,
    val endTs: Long,
    val duration: Long,
    val efficiency: Double,
    val avgHr: Double,
    val hrv: Double,
    val rr: Double,
    val spo2: Double? = null,
    val restHr: Double? = null,
    val t90: Double? = null,
    val sleepLatencyStartTs: Long? = null,
    val sleepLatencyEndTs: Long? = null,
    val isNap: Boolean,
    val stagesJson: String? = null,
)
