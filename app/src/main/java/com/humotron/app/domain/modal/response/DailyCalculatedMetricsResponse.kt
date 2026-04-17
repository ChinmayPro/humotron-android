package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyCalculatedMetricsResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<DailyMetricItem>
) : Parcelable

@Parcelize
data class DailyMetricItem(
    @SerializedName("key")
    val key: String,
    @SerializedName("value")
    val value: List<MetricDetail>
) : Parcelable

@Parcelize
data class MetricDetail(
    @SerializedName("_id")
    val id: String,
    @SerializedName("metricName")
    val metricName: String,
    @SerializedName("metricUnit")
    val metricUnit: String,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String,
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("metricWhat")
    val metricWhat: String,
    @SerializedName("metricWhy")
    val metricWhy: String,
    @SerializedName("observationLens")
    val observationLens: String,
    @SerializedName("metricRecommendedName")
    val metricRecommendedName: String,
    @SerializedName("metricDescription")
    val metricDescription: String,
    @SerializedName("metricValue")
    val metricValue: DailyMetricValue,
    @SerializedName("metricDuration")
    val metricDuration: String,
    @SerializedName("calculationPeriod")
    val calculationPeriod: Int,
    @SerializedName("recommendations")
    val recommendations: @kotlinx.parcelize.RawValue List<Any>,
    @SerializedName("metricReadingSubText")
    val metricReadingSubText: String,
    @SerializedName("fontColor")
    val fontColor: String,
    @SerializedName("boxColor")
    val boxColor: String,
    @SerializedName("insightCount")
    val insightCount: Int,
    @SerializedName("supplementCount")
    val supplementCount: Int,
    @SerializedName("recipeCount")
    val recipeCount: Int,
    @SerializedName("metricReading")
    val metricReading: String,
    @SerializedName("zones")
    val zones: List<Zone>
) : Parcelable

@Parcelize
data class DailyMetricValue(
    @SerializedName("fieldLabel")
    val fieldLabel: String,
    @SerializedName("value")
    val value: String,
    @SerializedName("timestamp")
    val timestamp: String
) : Parcelable

@Parcelize
data class Zone(
    @SerializedName("type")
    val type: String,
    @SerializedName("zone")
    val zone: Int,
    @SerializedName("targetTime")
    val targetTime: Int,
    @SerializedName("range")
    val range: String,
    @SerializedName("timeSpent")
    val timeSpent: Double,
    @SerializedName("goalCompletion")
    val goalCompletion: Double
) : Parcelable
