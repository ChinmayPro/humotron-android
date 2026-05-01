package com.humotron.app.util.analyzer

import android.graphics.Color

enum class HRVState(
    val value: Int,
    val title: String,
    val description: String,
    val color: Int,
) {

    NONE(
        -1,
        "",
        "",
        Color.GRAY
    ),

    THRIVING(
        0,
        "Thriving",
        "Stable HRV indicates excellent stress recovery.",
        Color.parseColor("#2176FF")
    ),

    BALANCED(
        1,
        "Balanced",
        "Your nervous system is well regulated.",
        Color.parseColor("#00C620")
    ),

    ALERT(
        2,
        "Alert",
        "Mild stress detected. Consider recovery.",
        Color.parseColor("#FFCF1B")
    ),

    CHALLENGED(
        3,
        "Challenged",
        "Elevated stress levels detected.",
        Color.parseColor("#FF881B")
    ),

    STRESSED(
        4,
        "Stressed",
        "High stress. Recovery strongly recommended.",
        Color.parseColor("#D63030")
    );

    companion object {
        fun fromValue(value: Int): HRVState {
            return entries.find { it.value == value } ?: NONE
        }
    }
}
