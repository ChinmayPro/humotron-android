package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TemperatureResponse(

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<TemperatureData>? = null,

    @SerializedName("averageReading")
    val averageReading: Double? = null,

    @SerializedName("calculationPeriod")
    val calculationPeriod: String? = null,

    @SerializedName("typicalRange")
    val typicalRange: List<Int>? = null,

    ) : Parcelable {

    @Parcelize
    data class TemperatureData(

        @SerializedName("value")
        val value: String? = null,

        @SerializedName("type")
        val type: String? = null,

        @SerializedName("time")
        val time: String? = null,
    ) : Parcelable
}

fun TemperatureResponse.TemperatureData.splitBloodPressure(): List<TemperatureResponse.TemperatureData> {

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