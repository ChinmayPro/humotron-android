package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseIntensityMetric(
    @SerializedName("value")
    val value: List<Value?>?,

    @SerializedName("time")
    val time: String?,

    @SerializedName("score")
    val score: Double?,

    @SerializedName("unit")
    val unit: String?,

    @SerializedName("metricName")
    val metricName: String?,

    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String?,
    @SerializedName("metricId")
    val metricId: String?,
) : Parcelable {

    @Parcelize
    data class Value(
        @SerializedName("type")
        val type: String?,

        @SerializedName("timeSpent")
        val timeSpent: Double?,

        @SerializedName("goalCompletion")
        val goalCompletion: Double?,

        @SerializedName("zone")
        val zone: Int?,

        @SerializedName("targetTime")
        val targetTime: Int?,

        @SerializedName("range")
        val range: String?,
    ) : Parcelable
}
