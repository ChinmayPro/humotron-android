package com.humotron.app.ui.decode.data

import androidx.annotation.ColorInt

enum class StressZone(val label: String, val colorHex: String, val maxScore: Int) {
    THRIVING("Thriving", "#C4F23E", 40),
    BALANCED("Balanced", "#5FB7C4", 48),
    ALERT("Alert", "#E7A93C", 58),
    CHALLENGED("Challenged", "#E08A4A", 70),
    STRESSED("Stressed", "#EE4D3D", Int.MAX_VALUE);

    @get:ColorInt
    val color: Int get() = android.graphics.Color.parseColor(colorHex)

    companion object {
        fun fromScore(score: Int): StressZone = entries.first { score < it.maxScore }
    }
}

data class HourData(
    val hour: Int,       // 9..17
    val stressValue: Int // 0..100
)

data class WorkdayDay(
    val date: java.util.Date,
    val dayOfWeek: Int,        // Calendar.DAY_OF_WEEK
    val score: Int,
    val zone: StressZone,
    val hours: List<HourData>,
    val hasData: Boolean = true
)

data class ZoneDistribution(
    val name: String,
    val colorHex: String,
    val percentage: Int
)

data class WorkdayReport(
    val score: String,
    val zone: String,
    val date: String,
    val name: String,
    val target: String
)

data class WeatherReport(
    val score: String,
    val zone: String,
    val date: String,
    val name: String,
    val target: String
)

data class WeatherInsight(
    val date: String,
    val title: String,
    val description: String
)

data class WeatherPairing(
    val id: String,
    val pair: String,
    val title: String,
    val date: String?,
    val score: String? = null,
    val zone: String? = null,
    val cstate: Int, // 1: Generate, 2: Collecting, 3: Ready
    val collectPct: Int? = null,
    val collectCount: String? = null
)

data class WeatherPlan(
    val letter: String,
    val title: String,
    val description: String,
    val colorCode: String // Used to map to specific colors
)

data class WeatherDetail(
    val title: String,
    val range: String,
    val a: String,
    val b: String,
    val impact: Int,
    val level: String,
    val summary: String,
    val impactNote: String,
    val aPts: List<Int>,
    val bPts: List<Int>,
    val ins: List<WeatherInsight>,
    val plan: List<WeatherPlan>
)

data class MonthlyReportItem(
    val month: String,
    val state: String, // "ready", "generate", "collecting"
    val range: String,
    val score: String? = null,
    val zone: String? = null,
    val note: String? = null,
    val pct: Int? = null
)
