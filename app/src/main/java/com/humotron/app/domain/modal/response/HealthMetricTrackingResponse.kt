package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthMetricTrackingResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: HealthMetricTrackingDataWrapper? = null,
    @SerializedName("trackingMetricCount")
    val trackingMetricCount: Int? = null
) : Parcelable

@Parcelize
data class HealthMetricTrackingDataWrapper(
    @SerializedName("individualMetrics")
    val individualMetrics: List<HealthMetricTrackingData>? = null,
    @SerializedName("groupMetrics")
    val groupedMetrics: List<GroupedMetricData>? = null
) : Parcelable

@Parcelize
data class HealthMetricTrackingData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("fieldName")
    val fieldName: List<HealthMetricFieldName>? = null,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("deviceId")
    val deviceId: List<String>? = null,
    @SerializedName("metricUnit")
    val metricUnit: String? = null,
    @SerializedName("metricWhat")
    val metricWhat: String? = null,
    @SerializedName("metricWhy")
    val metricWhy: String? = null,
    @SerializedName("observationLens")
    val observationLens: String? = null,
    @SerializedName("metricOrder")
    val metricOrder: Int? = null,
    @SerializedName("allMetrics")
    val allMetrics: HealthAllMetricsInfo? = null,
    @SerializedName("categoryId")
    val categoryId: String? = null,
    @SerializedName("metricType")
    val metricType: String? = null,
    @SerializedName("metricReading")
    val metricReading: String? = null,
    @SerializedName("metricValue")
    val metricValue: HealthMetricReadingValue? = null,
    @SerializedName("insightCount")
    val insightCount: Int? = null,
    @SerializedName("supplementCount")
    val supplementCount: Int? = null,
    @SerializedName("recipeCount")
    val recipeCount: Int? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("hasActiveBooster")
    val hasActiveBooster: Boolean? = null,
    @SerializedName("boosterId")
    val boosterId: String? = null,
    @SerializedName("boosterName")
    val boosterName: String? = null,
    @SerializedName("boosterHeroCopy")
    val boosterHeroCopy: String? = null,
    @SerializedName("boosterPrice")
    val boosterPrice: Double? = null,
    @SerializedName("iosProductId")
    val iosProductId: String? = null,
    @SerializedName("androidProductId")
    val androidProductId: String? = null
) : Parcelable

@Parcelize
data class HealthMetricFieldName(
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("value")
    val value: String? = null,
    @SerializedName("isRaw")
    val isRaw: Boolean? = null
) : Parcelable

@Parcelize
data class HealthAllMetricsInfo(
    @SerializedName("enabled")
    val enabled: Boolean? = null,
    @SerializedName("displayType")
    val displayType: String? = null
) : Parcelable

@Parcelize
data class HealthMetricReadingValue(
    @SerializedName("fieldLabel")
    val fieldLabel: String? = null,
    @SerializedName("value")
    val value: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null
) : Parcelable
