package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class BandUploadData(
    val hardwareId: String,
    val data: BandUploadDeviceData,
    val recordTimestamp: Long,
)

data class BandUploadDeviceData(

    @SerializedName("SPO2")
    val spo2: List<Spo2Data> = emptyList(),

    @SerializedName("HRV")
    val hrv: List<BandHrv> = emptyList(),

    @SerializedName("DETAILACTIVITY")
    val detailActivity: List<DetailActivityData> = emptyList(),

    @SerializedName("HR")
    val hr: List<HeartRateData> = emptyList(),

    @SerializedName("TOTALACTIVITY")
    val totalActivity: List<TotalActivityData> = emptyList(),

    @SerializedName("SLEEP")
    val sleep: List<BandSleep> = emptyList(),
)

data class Spo2Data(
    val automaticSpo2Data: Int,
    val date: String,
)

data class BandHrv(
    val date: String,
    val systolicBP: Int,
    val diastolicBP: Int,
    val heartRate: Int,
    val stress: Int,
    val hrv: Int,
    val vascularAging: Int,
)

data class DetailActivityData(
    val date: String,
    val arraySteps: List<Int>,
    val step: Int,
    val distance: Double,
    val calories: Double,
)

data class HeartRateData(
    val date: String,
    val singleHR: Int,
)

data class TotalActivityData(
    val goal: String,
    val distance: Double,
    val calories: Double,
    val date: String,
    val activeMinutes: Int,
    val step: Int,
    val exerciseMinutes: Int,
)

data class BandSleep(
    val arraySleepQuality: List<Int>,
    val totalSleepTime: Int,
    val sleepUnitLength: Int,
    val startTime_SleepData: String,
    val date: String,
)

