package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class WristBandApiParam(
    @SerializedName("range")
    val range: String?,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("offset")
    val offset: String,
    @SerializedName("metricName")
    val metricName: String,
)