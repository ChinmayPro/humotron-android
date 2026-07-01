package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class WeatherDetailResponse(
    val status: String?,
    val message: String?,
    val data: WeatherDetailData?
)

data class WeatherDetailData(
    @SerializedName("_id") val id: String?,
    val type: String?,
    val title: String?,
    val headline: String?,
    val sections: List<WeatherSection>?,
    val meta: WeatherDetailMeta?
)

data class WeatherDetailMeta(
    val selectedDate: String?,
    val validDayCount: Int?,
    val lookbackDays: Int?,
    val analysisWindow: AnalysisWindow?
)

data class AnalysisWindow(val start: String?, val end: String?)

data class WeatherSection(
    val section: String?,
    val status: String?,
    val narration: String?,
    val headline: String?,
    val impact_level: String?,
    val impact_badge_color: String?,
    val key_finding: String?,
    val impact_scale: List<ImpactScale>?,
    val impact_score: Int?,
    val impact_label: String?,
    val score_color: String?,
    val title: String?,
    val display_mode: String?,
    val narrative: String?,
    val series: List<WeatherSeries>?,
    val observations: List<WeatherObservation>?,
    val actions: List<WeatherAction>?
)

data class ImpactScale(
    val range: String?,
    val label: String?,
    val is_active: Boolean?
)

data class WeatherSeries(
    val label: String?,
    val unit: String?,
    val data: List<WeatherSeriesData>?
)

data class WeatherSeriesData(
    val date: String?,
    val value: Float?
)

data class WeatherObservation(
    val number: Int?,
    val title: String?,
    val description: String?,
    val dates: List<String>?
)

data class WeatherAction(
    val letter: String?,
    val title: String?,
    val description: String?,
    val bg_color: String?,
    val text_color: String?,
    val letter_bg_color: String?
)
