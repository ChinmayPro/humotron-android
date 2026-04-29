package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class SaveScanDataParam(
    @SerializedName("baseline")
    val baseline: Double,
    @SerializedName("current")
    val current: Double,
    @SerializedName("type")
    val type: String,
    @SerializedName("deviceId")
    val deviceId: String
)
