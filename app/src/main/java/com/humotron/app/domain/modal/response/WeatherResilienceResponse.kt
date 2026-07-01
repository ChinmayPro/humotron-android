package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class WeatherResilienceResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("data")
    val data: WeatherResilienceData?
)

data class WeatherResilienceData(
    @SerializedName("averageScore")
    val averageScore: Double?,
    @SerializedName("selectedDate")
    val selectedDate: String?,
    @SerializedName("reportCount")
    val reportCount: Int?
)
