package com.humotron.app.domain.modal

enum class BPMachineBpResult(
    val value: Int,
    val title: String,
    val description: String,
) {
    NORMAL(
        0,
        title = "Normal",
        description = "Normal blood pressure result."
    ),
    UNABLE_TO_ANALYZE(
        1,
        title = "Unable to analyze",
        description = "Cuff is too loose, inflation is slow, slow air leakage, or large air volume."
    ),
    WAVEFORM_DISORDER(
        2,
        title = "Waveform disorder",
        description = "Arm movement or other interference detected during pumping."
    ),
    WEAK_SIGNAL(
        3,
        title = "Weak signal",
        description = "Unable to detect pulse wave, possibly due to clothing or sleeve interference."
    ),
    EQUIPMENT_ERROR(
        4,
        title = "Equipment error",
        description = "Valve blocking, over-range blood pressure measurement, serious cuff leakage, software system abnormality, hardware system error, or other abnormalities."
    );

    companion object {
        fun fromValue(value: Int): BPMachineBpResult {
            return entries.find { it.value == value } ?: EQUIPMENT_ERROR
        }
    }
}
