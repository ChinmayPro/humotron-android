package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsightTimelineResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: InsightTimelineData? = null
) : Parcelable

@Parcelize
data class InsightTimelineData(
    @SerializedName("currentWindow")
    val currentWindow: TimelineWindow? = null,
    @SerializedName("hardwareId")
    val hardwareId: String? = null,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,
    @SerializedName("timelineState")
    val timelineState: String? = null,
    @SerializedName("currentFrequency")
    val currentFrequency: Int? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("metricId")
    val metricId: String? = null,
    @SerializedName("hasHistory")
    val hasHistory: Boolean? = null,
    @SerializedName("pastInsightCount")
    val pastInsightCount: Int? = null,
    @SerializedName("insightWindows")
    val insightWindows: List<TimelineWindow>? = null,
    @SerializedName("metricName")
    val metricName: String? = null
) : Parcelable

@Parcelize
data class TimelineWindow(
    @SerializedName("availableDays")
    val availableDays: Int? = null,
    @SerializedName("minData")
    val minData: Int? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("patternDays")
    val patternDays: Int? = null,
    @SerializedName("insightId")
    val insightId: String? = null,
    @SerializedName("windowAction")
    val windowAction: String? = null
) : Parcelable
