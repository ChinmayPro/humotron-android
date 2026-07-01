package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsightSummaryResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: InsightSummaryData? = null
) : Parcelable

@Parcelize
data class InsightSummaryData(
    @SerializedName("currentFrequency")
    val currentFrequency: Int? = null,
    @SerializedName("totalWindows")
    val totalWindows: Int? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("booster_ai_insights")
    val boosterAiInsights: BoosterAiInsights? = null,
    @SerializedName("windows")
    val windows: List<SummaryWindow>? = null,
    @SerializedName("hardwareId")
    val hardwareId: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,
    @SerializedName("metricId")
    val metricId: String? = null
) : Parcelable

@Parcelize
data class BoosterAiInsights(
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
    @SerializedName("hasBoosterInsightActive")
    val hasBoosterInsightActive: Boolean? = null,
    @SerializedName("boosterHeroCopy")
    val boosterHeroCopy: String? = null
) : Parcelable

@Parcelize
data class SummaryWindow(
    @SerializedName("availableDays")
    val availableDays: Int? = null,
    @SerializedName("minData")
    val minData: Int? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("observationLenses")
    val observationLenses: List<ObservationLens>? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("patternDays")
    val patternDays: Int? = null,
    @SerializedName("windowAction")
    val windowAction: String? = null
) : Parcelable

@Parcelize
data class ObservationLens(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("lens")
    val lens: String? = null,
    @SerializedName("insightId")
    val insightId: String? = null,
    @SerializedName("action")
    val action: String? = null
) : Parcelable
