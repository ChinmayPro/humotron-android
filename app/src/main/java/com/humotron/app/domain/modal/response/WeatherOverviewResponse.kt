package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class WeatherOverviewResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("is_weather_connected") val isWeatherConnected: Boolean?,
    @SerializedName("data") val data: List<WeatherOverviewData>?
)

data class WeatherOverviewData(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subTitle") val subTitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("is_eligible") val isEligible: Boolean?,
    @SerializedName("is_min_data") val isMinData: Boolean?,
    @SerializedName("validDayCount") val validDayCount: Int?,
    @SerializedName("totalDayCount") val totalDayCount: Int?,
    @SerializedName("score") val score: String?,
    @SerializedName("zone") val zone: String?,
    @SerializedName("recent_reports") val recentReports: List<RecentReport>?
)

data class RecentReport(
    @SerializedName("_id") val id: String?,
    @SerializedName("selectedDate") val selectedDate: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("headline") val headline: String?,
    @SerializedName("validDayCount") val validDayCount: Int?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("score") val score: String?,
    @SerializedName("zone") val zone: String?,
    @SerializedName("impact_score") val impactScore: Int?,
    @SerializedName("impact_label") val impactLabel: String?
)
