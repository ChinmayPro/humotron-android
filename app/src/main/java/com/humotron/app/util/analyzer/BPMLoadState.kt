package com.humotron.app.util.analyzer

import android.graphics.Color

enum class BPMLoadState(
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

    STABLE(
        0,
        title = "Stable",
        description = "Heart rate aligns with resting baseline expectations, indicating efficient cardiovascular demand and steady physiological load.",
        color = Color.parseColor("#00C620")
    ),

    ELEVATED(
        1,
        title = "Elevated",
        description = "Heart rate is moderately above resting baseline, suggesting increased internal workload even in the absence of intense activity.",
        color = Color.parseColor("#FFCF1B")
    ),

    HIGH_LOAD(
        2,
        title = "High Load",
        description = "Cardiovascular demand is significantly elevated relative to baseline. The body is operating at a heightened output level.",
        color = Color.parseColor("#FF881B")
    ),

    EXCESS_LOAD(
        3,
        title = "Excess",
        description = "Heart rate reflects sustained high demand relative to expected resting levels. This state indicates substantial physiological strain.",
        color = Color.parseColor("#D63030")
    );

    companion object {
        fun fromValue(value: Int): BPMLoadState {
            return entries.find { it.value == value } ?: NONE
        }
    }
}
