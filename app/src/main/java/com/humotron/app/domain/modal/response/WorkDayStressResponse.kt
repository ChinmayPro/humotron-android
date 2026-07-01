package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class WorkDayStressResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: WorkDayStressData?
)

data class WorkDayStressData(
    @SerializedName("date")
    val date: String?,
    @SerializedName("workdayStressScore")
    val workdayStressScore: Int?,
    @SerializedName("workdayStressLabel")
    val workdayStressLabel: String?,
    @SerializedName("workdayStressColor")
    val workdayStressColor: String?,
    @SerializedName("peakStress")
    val peakStress: PeakStressData?
)

data class PeakStressData(
    @SerializedName("time")
    val time: String?,
    @SerializedName("time12h")
    val time12h: String?,
    @SerializedName("avgHRV")
    val avgHRV: Int?,
    @SerializedName("duringMeeting")
    val duringMeeting: String?
)
