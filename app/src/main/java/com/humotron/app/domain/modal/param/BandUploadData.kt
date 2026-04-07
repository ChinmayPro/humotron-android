package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class BandUploadData(
    val hardwareId: String,
    val data: BandUploadDeviceData,
    val recordTimestamp: Long,
)

data class BandUploadDeviceData(
    @SerializedName("HRV")
    val hrv: List<BandHrvUploadMapper>,
)

data class BandHrvUploadMapper(
    val date: String,
    val highBP: Int,
    val lowBP: Int,
    val heartRate: Int,
    val stress: Int,
    val hrv: Int,
    val vascularAging: Int,
)

