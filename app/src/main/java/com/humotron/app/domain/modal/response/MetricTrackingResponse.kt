package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetricTrackingResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<MetricTrackingData>? = null
) : Parcelable

@Parcelize
data class MetricTrackingData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricUnit")
    val metricUnit: String? = null,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,
    @SerializedName("deviceId")
    val deviceId: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("metricWhat")
    val metricWhat: String? = null,
    @SerializedName("metricWhy")
    val metricWhy: String? = null,
    @SerializedName("observationLens")
    val observationLens: String? = null,
    @SerializedName("metricOrder")
    val metricOrder: Int? = null,
    @SerializedName("metricRecommendedName")
    val metricRecommendedName: String? = null,
    @SerializedName("metricDescription")
    val metricDescription: String? = null,
    @SerializedName("metricDuration")
    val metricDuration: String? = null,
    @SerializedName("metricReading")
    val metricReading: String? = null,
    @SerializedName("metricValue")
    val metricValue: MetricReadingValue? = null,
    @SerializedName("recommendations")
    val recommendations: List<RecommendationData>? = null,
    @SerializedName("metricReadingSubText")
    val metricReadingSubText: String? = null,
    @SerializedName("fontColor")
    val fontColor: String? = null,
    @SerializedName("boxColor")
    val boxColor: String? = null,
    @SerializedName("insightCount")
    val insightCount: Int? = null,
    @SerializedName("supplementCount")
    val supplementCount: Int? = null,
    @SerializedName("recipeCount")
    val recipeCount: Int? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("metricReadingUnit")
    val metricReadingUnit: String? = null
) : Parcelable

@Parcelize
data class MetricReadingValue(
    @SerializedName("fieldLabel")
    val fieldLabel: String? = null,
    @SerializedName("value")
    val value: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
) : Parcelable

@Parcelize
data class RecommendationData(
    @SerializedName("recommendationsShort")
    val recommendationsShort: String? = null,
    @SerializedName("recommendationsLong")
    val recommendationsLong: String? = null,
    @SerializedName("recommendationsTag")
    val recommendationsTag: String? = null,
    @SerializedName("isPreview")
    val isPreview: Boolean? = null
) : Parcelable
