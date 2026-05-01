package com.humotron.app.util.analyzer

object ThermalAnalyzer {

    val infoList: List<ThermalBalanceState>
        get() = listOf(
            ThermalBalanceState.STABLE,
            ThermalBalanceState.WARM,
            ThermalBalanceState.ELEVATED,
            ThermalBalanceState.HIGH_DRIFT
        )

    fun analyze(
        baselineValue: Double,
        currentValue: Double
    ): ThermalBalanceState {
        val delta = currentValue - baselineValue

        return when {
            delta >= 0.7 -> ThermalBalanceState.HIGH_DRIFT
            delta >= 0.4 -> ThermalBalanceState.ELEVATED
            delta >= 0.2 -> ThermalBalanceState.WARM
            else -> ThermalBalanceState.STABLE
        }
    }
}