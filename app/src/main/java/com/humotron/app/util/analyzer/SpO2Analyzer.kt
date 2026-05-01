package com.humotron.app.util.analyzer

object SpO2Analyzer {

    val infoList: List<SpO2State>
        get() = listOf(
            SpO2State.OPTIMAL,
            SpO2State.SLIGHT_DIP,
            SpO2State.LOW,
            SpO2State.SIGNIFICANT
        )

    fun percentageChange(baselineValue: Double, currentValue: Double): Double {
        if (baselineValue == 0.0) return 0.0
        return ((currentValue - baselineValue) / baselineValue) * 100
    }

    fun analyze(
        baselineValue: Double,
        currentValue: Double,
    ): SpO2State {
        val delta = percentageChange(baselineValue, currentValue)

        return when {
            delta >= -1 -> SpO2State.OPTIMAL
            delta >= -2 -> SpO2State.SLIGHT_DIP
            delta >= -3 -> SpO2State.LOW
            else -> SpO2State.SIGNIFICANT
        }
    }
}
