package com.humotron.app.util.analyzer

object BPMLoadAnalyzer {

    val infoList: List<BPMLoadState>
        get() = listOf(
            BPMLoadState.STABLE,
            BPMLoadState.ELEVATED,
            BPMLoadState.HIGH_LOAD,
            BPMLoadState.EXCESS_LOAD
        )

    fun analyze(
        baselineValue: Double,
        currentValue: Double,
    ): BPMLoadState {
        val delta = currentValue - baselineValue

        return when {
            delta <= 5 -> BPMLoadState.STABLE
            delta <= 12 -> BPMLoadState.ELEVATED
            delta <= 20 -> BPMLoadState.HIGH_LOAD
            else -> BPMLoadState.EXCESS_LOAD
        }
    }
}
