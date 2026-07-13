package com.humotron.app.domain.modal

enum class DeviceType(val value: String) {
    BAND("WristBand"),
    RING("HumotronRing"),
    SMART_CUFF("BPMachine"),
    WEIGHT_MACHINE("WeightMachine"),
    UNKNOWN("Unknown");

    companion object {
        fun from(value: String?): DeviceType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}