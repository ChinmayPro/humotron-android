package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsightMetricsOverviewResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: InsightMetricsOverviewData? = null
) : Parcelable

@Parcelize
data class InsightMetricsOverviewData(
    @SerializedName("isPaid")
    val isPaid: Boolean? = null,
    @SerializedName("boosterPrice")
    val boosterPrice: Double? = null,
    @SerializedName("iosProductId")
    val iosProductId: String? = null,
    @SerializedName("androidProductId")
    val androidProductId: String? = null,
    @SerializedName("boosterName")
    val boosterName: String? = null,
    @SerializedName("boosterId")
    val boosterId: String? = null,
    @SerializedName("boosterHeroCopy")
    val boosterHeroCopy: String? = null,
    @SerializedName("groupedMetrics")
    val groupedMetrics: List<InsightGroupedMetric>? = null,
    @SerializedName("individualMetrics")
    val individualMetrics: List<InsightMetricItem>? = null
) : Parcelable

@Parcelize
data class InsightGroupedMetric(
    @SerializedName("groupLastSyncDate")
    val groupLastSyncDate: String? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("categoryDescription")
    val categoryDescription: String? = null,
    @SerializedName("groupAvailableDays")
    val groupAvailableDays: Int? = null,
    @SerializedName("categoryId")
    val categoryId: String? = null,
    @SerializedName("groupState")
    val groupState: String? = null,
    @SerializedName("groupInsightMinData")
    val groupInsightMinData: Int? = null,
    @SerializedName("categoryName")
    val categoryName: String? = null,
    @SerializedName("metrics")
    val metrics: List<InsightMetricItem>? = null
) : Parcelable

@Parcelize
data class InsightMetricItem(
    @SerializedName("availableDays")
    val availableDays: Int? = null,
    @SerializedName("lastSyncDate")
    val lastSyncDate: String? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("hasInsight")
    val hasInsight: Boolean? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricOrder")
    val metricOrder: Int? = null,
    @SerializedName("insightMinData")
    val insightMinData: Int? = null,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,
    @SerializedName("metricId")
    val metricId: String? = null,
    @SerializedName("state")
    val state: String? = null
) : Parcelable
