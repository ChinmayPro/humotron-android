package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

// --- Requests ---
data class WorkDayStressReportRequest(
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("range") val range: String
)

// --- getWorkDayStressReport Responses ---
data class WorkDayStressReportResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<WorkDayStressReportDay>?
)

data class WorkDayStressReportDay(
    @SerializedName("date") val date: String?,
    @SerializedName("baselineHRV") val baselineHRV: Int?,
    @SerializedName("baselineStatus") val baselineStatus: String?,
    @SerializedName("workdayStressScore") val workdayStressScore: Int?,
    @SerializedName("workdayStressLabel") val workdayStressLabel: String?,
    @SerializedName("workdayStressColor") val workdayStressColor: String?,
    @SerializedName("workdayStressMessage") val workdayStressMessage: String?,
    @SerializedName("zoneBreakdown") val zoneBreakdown: ZoneBreakdown?,
    @SerializedName("hourlyStress") val hourlyStress: List<HourlyStress>?
)

data class ZoneBreakdown(
    @SerializedName("minutes") val minutes: Map<String, Int>?,
    @SerializedName("percents") val percents: Map<String, Double>?,
    @SerializedName("totalAnalysedMinutes") val totalAnalysedMinutes: Int?,
    @SerializedName("colors") val colors: Map<String, String>?
)

data class HourlyStress(
    @SerializedName("hour") val hour: String?,
    @SerializedName("hrLabel") val hrLabel: String?,
    @SerializedName("score") val score: Int?,
    @SerializedName("label") val label: String?,
    @SerializedName("zone") val zone: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("avgHRV") val avgHRV: Int?,
    @SerializedName("dataPoints") val dataPoints: Int?
)

// --- workdayStressReport/overview Responses ---
data class WorkDayStressOverviewResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: WorkDayStressOverviewData?
)

data class WorkDayStressOverviewData(
    @SerializedName("is_eligible") val isEligible: Boolean?,
    @SerializedName("is_min_data") val isMinData: Boolean?,
    @SerializedName("reports") val reports: List<WorkDayOverviewReport>?
)

data class WorkDayOverviewReport(
    @SerializedName(value = "_id", alternate = ["id"]) val id: String?,
    @SerializedName(value = "selectedDate", alternate = ["selected_date"]) val selectedDate: String?,
    @SerializedName(value = "analysisWindow", alternate = ["analysis_window"]) val analysisWindow: AnalysisWindow?,
    @SerializedName(value = "createdAt", alternate = ["created_at"]) val createdAt: String?,
    @SerializedName("summary") val summary: OverviewSummary?,
    @SerializedName(value = "validDayCount", alternate = ["valid_day_count", "validDayCounts"]) val validDayCount: Int?
)


data class OverviewSummary(
    @SerializedName(value = "avgWorkdayStress", alternate = ["avg_workday_stress"]) val avgWorkdayStress: Double?,
    @SerializedName(value = "stressLevel", alternate = ["stress_level"]) val stressLevel: String?,
    @SerializedName(value = "highestWeek", alternate = ["highest_week"]) val highestWeek: Double?,
    @SerializedName(value = "lowestWeek", alternate = ["lowest_week"]) val lowestWeek: Double?,
    @SerializedName(value = "bestWeek", alternate = ["best_week"]) val bestWeek: Double?,
    @SerializedName(value = "stressLevelColor", alternate = ["stress_level_color"]) val stressLevelColor: String?
)
