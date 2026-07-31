package com.humotron.app.domain.modal

enum class ChartType(val value: String, val label: String) {
    BANDED_LINE("banded_line", "Banded Line"),
    SPREAD_COLUMNS("spread_columns", "Spread Columns"),
    GOAL_BARS("goal_bars", "Goal Bars"),
    PAIRED_RANGE("paired_range", "Paired Range"),
    COMPOSITION_STACK("composition_stack", "Composition Stack"),
    ZONE_TREND("zone_trend", "Zone Trend"),
    LEVEL_LADDER("level_ladder", "Level Ladder"),
    LEVEL_HISTORY("level_history", "Level History"),
    REFERENCE_DELTA("reference_delta", "Reference Delta"),
    DELTA_TREND("delta_trend", "Delta Trend"),
    CLINICAL_THRESHOLD("clinical_threshold", "Clinical Threshold");

    companion object {
        fun fromValue(value: String?): ChartType? {
            return entries.find { it.value == value }
        }
    }
}
