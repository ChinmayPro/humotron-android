package com.humotron.app.util.analyzer

import android.graphics.Color

enum class SpO2State(
    val value: Int,
    val title: String,
    val description: String,
    val color: Int,
) {

    NONE(
        -1,
        title = "",
        description = "",
        color = Color.GRAY
    ),

    OPTIMAL(
        0,
        title = "Optimal",
        description = "Oxygen saturation aligns with personal baseline, supporting efficient oxygen delivery to tissues and steady cognitive performance.",
        color = Color.parseColor("#00C620")
    ),

    SLIGHT_DIP(
        1,
        title = "Slight Dip",
        description = "A mild deviation from baseline oxygen levels. This may reflect subtle shifts in breathing efficiency or environmental conditions.",
        color = Color.parseColor("#FFCF1B")
    ),

    LOW(
        2,
        title = "Low",
        description = "Oxygen saturation is noticeably below personal norm, indicating reduced oxygen availability relative to usual levels.",
        color = Color.parseColor("#FF881B")
    ),

    SIGNIFICANT(
        3,
        title = "Significant",
        description = "A marked deviation from baseline oxygen saturation. Oxygen delivery efficiency is meaningfully reduced compared to normal patterns.",
        color = Color.parseColor("#D63030")
    );

    companion object {
        fun fromValue(value: Int): SpO2State {
            return entries.find { it.value == value } ?: NONE
        }
    }
}
