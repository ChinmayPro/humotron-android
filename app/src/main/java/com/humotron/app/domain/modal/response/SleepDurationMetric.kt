package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SleepDurationMetric(
    @SerializedName("value")
    val value: Value?,

    @SerializedName("time")
    val time: String?,

    @SerializedName("metricName")
    val metricName: String?,

    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String?,
    @SerializedName("metricId")
    val metricId: String?,
) : Parcelable {

    @Parcelize
    data class Value(
        @SerializedName("totalSleepHours")
        val totalSleepHours: String?,

        @SerializedName("avgEfficiency")
        val avgEfficiency: Double?,

        @SerializedName("avgOxygenSaturation")
        val avgOxygenSaturation: String?,

        @SerializedName("avgBreathingRate")
        val avgBreathingRate: String?,

        @SerializedName("deepDurationMinutes")
        val deepDurationMinutes: String?,

        @SerializedName("avgHr")
        val avgHr: String?,

        @SerializedName("avgHrv")
        val avgHrv: String?,

        @SerializedName("avgHrDip")
        val avgHrDip: String?,

        @SerializedName("sessionCount")
        val sessionCount: Int?,

        @SerializedName("calculatedAt")
        val calculatedAt: String?,
    ) : Parcelable
}
