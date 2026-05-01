package com.humotron.app.util.analyzer

import android.graphics.Color

enum class ThermalBalanceState(
    val title: String,
    val description: String,
    val color: Int,
) {

    NONE(
        title = "",
        description = "",
        color = Color.GRAY
    ),

    STABLE(
        title = "Stable",
        description = "Skin temperature aligns with expected circadian patterns, indicating steady thermoregulation and balanced recovery load.",
        color = Color.parseColor("#00C620")
    ),

    WARM(
        title = "Warm",
        description = "A mild upward deviation from baseline temperature rhythm, reflecting increased metabolic or environmental influence.",
        color = Color.parseColor("#FFCF1B")
    ),

    ELEVATED(
        title = "Elevated",
        description = "A clear rise above expected temperature curve. The body is allocating additional resources toward thermoregulation.",
        color = Color.parseColor("#FF881B")
    ),

    HIGH_DRIFT(
        title = "High Drift",
        description = "A substantial deviation from baseline thermal rhythm. This state often reflects heightened recovery demand or environmental strain.",
        color = Color.parseColor("#D63030")
    )
}