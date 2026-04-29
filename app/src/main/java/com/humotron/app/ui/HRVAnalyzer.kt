package com.humotron.app.ui

object HRVAnalyzer {

    val infoList: List<HRVState>
        get() = listOf(
            HRVState.THRIVING,
            HRVState.BALANCED,
            HRVState.ALERT,
            HRVState.CHALLENGED,
            HRVState.STRESSED
        )

    fun percentageChange(baselineValue: Double, currentValue: Double): Double {
        if (baselineValue == 0.0) return 0.0

        return ((currentValue - baselineValue) / baselineValue) * 100
    }

    fun analyze(
        baselineValue: Double,
        currentValue: Double
    ): HRVState {
        val delta = percentageChange(baselineValue, currentValue)

        return when {
            delta >= 5 -> HRVState.THRIVING
            delta >= -5 -> HRVState.BALANCED
            delta >= -15 -> HRVState.ALERT
            delta >= -30 -> HRVState.CHALLENGED
            else -> HRVState.STRESSED
        }
    }
}
