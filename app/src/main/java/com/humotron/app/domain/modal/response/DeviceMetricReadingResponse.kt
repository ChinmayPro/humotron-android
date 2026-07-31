package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceMetricReadingResponse(

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<DeviceMetricData>? = null,

    @SerializedName("averageReading")
    val averageReading: Double? = null,

    @SerializedName("calculationPeriod")
    val calculationPeriod: String? = null,

    @SerializedName("typicalRange")
    val typicalRange: List<Int>? = null,

    @SerializedName("range")
    val range: String? = null,

    @SerializedName("chartType")
    val chartType: String? = null,

    ) : Parcelable {

    @Parcelize
    data class DeviceMetricData(

        @SerializedName("value")
        val value: String? = null,

        @SerializedName("type")
        val type: String? = null,

        @SerializedName("time")
        val time: String? = null,

        @SerializedName("sleepStartTime")
        val sleepStartTime: String? = null,

        @SerializedName("sleepEndTime")
        val sleepEndTime: String? = null,

        ) : Parcelable
}

fun DeviceMetricReadingResponse.DeviceMetricData.splitBloodPressure(): List<DeviceMetricReadingResponse.DeviceMetricData> {

    if (value.isNullOrEmpty()) return emptyList()

    return if (value.contains("/")) {
        val parts = value.split("/")

        val systolic = parts.getOrNull(0)
        val diastolic = parts.getOrNull(1)

        listOfNotNull(
            systolic?.let {
                this.copy(value = it)
            },
            diastolic?.let {
                this.copy(value = it)
            }
        )
    } else {
        listOf(this)
    }
}