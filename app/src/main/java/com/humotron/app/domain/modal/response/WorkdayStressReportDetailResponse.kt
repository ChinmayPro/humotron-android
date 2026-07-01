package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

data class WorkdayStressReportDetailResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: WorkdayStressReportDetailData?
)

data class WorkdayStressReportDetailData(
    @SerializedName(value = "_id", alternate = ["id"]) val id: String?,
    @SerializedName(value = "selectedDate", alternate = ["selected_date"]) val selectedDate: String?,
    @SerializedName(value = "analysisWindow", alternate = ["analysis_window"]) val analysisWindow: AnalysisWindow?,
    @SerializedName(value = "validDayCount", alternate = ["valid_day_count", "validDayCounts"]) val validDayCount: Int?,
    @SerializedName("summary") val summary: WorkdayReportSummary?,
    @SerializedName(value = "sections", alternate = ["report"]) val sections: List<WorkdayReportSection>?
)

data class WorkdayReportSummary(
    @SerializedName(value = "avgWorkdayStress", alternate = ["avg_workday_stress"]) val avgWorkdayStress: Double?,
    @SerializedName(value = "stressLevel", alternate = ["stress_level"]) val stressLevel: String?,
    @SerializedName(value = "stressLevelColor", alternate = ["stress_level_color"]) val stressLevelColor: String?,
    @SerializedName(value = "highestWeek", alternate = ["highest_week"]) val highestWeek: Double?,
    @SerializedName(value = "lowestWeek", alternate = ["lowest_week"]) val lowestWeek: Double?,
    @SerializedName(value = "bestWeek", alternate = ["best_week"]) val bestWeek: Double?
)

data class WorkdayReportSection(
    @SerializedName(value = "section", alternate = ["lens"]) val section: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("headline") val headline: String?,
    @SerializedName(value = "narration", alternate = ["narrative"]) val narration: String?,
    @SerializedName(value = "display_mode", alternate = ["displayMode"]) val displayMode: String?,
    
    @SerializedName(value = "data", alternate = ["chartData", "chart_data", "evidence", "line_evidence", "stats", "badges"])
    val data: JsonElement?,

    @SerializedName(value = "series", alternate = ["series_data"]) val series: List<WorkdayReportSeries>?,
    @SerializedName(value = "bars", alternate = ["bar_charts", "bar_data"]) val bars: List<WorkdayReportBar>?,
    @SerializedName(value = "comparisons", alternate = ["comparison_data"]) val comparisons: List<WorkdayReportComparison>?
)

data class WorkdayReportSeries(
    @SerializedName("label") val label: String?,
    @SerializedName(value = "data", alternate = ["points", "seriesData"]) val data: List<WorkdayReportSeriesPoint>?
)

data class WorkdayReportSeriesPoint(
    @SerializedName(value = "x", alternate = ["name", "label", "date"]) val x: String?,
    @SerializedName(value = "y", alternate = ["val", "value", "score", "pts"]) val y: Float?
)

data class WorkdayReportBar(
    @SerializedName(value = "label", alternate = ["name"]) val label: String?,
    @SerializedName(value = "value", alternate = ["val", "score"]) val value: Float?,
    @SerializedName(value = "percentage", alternate = ["pct"]) val percentage: Float?,
    @SerializedName("color") val color: String?
)

data class WorkdayReportComparison(
    @SerializedName(value = "label", alternate = ["cmpt", "title"]) val label: String?,
    @SerializedName(value = "date", alternate = ["cmpd"]) val date: String?,
    @SerializedName(value = "day", alternate = ["cmpm", "subtext"]) val day: String?,
    @SerializedName(value = "score", alternate = ["val", "value"]) val score: Int?,
    @SerializedName("color") val color: String?
)
