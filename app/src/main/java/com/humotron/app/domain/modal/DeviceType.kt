package com.humotron.app.domain.modal

enum class DeviceType(val value: String) {
    BAND("WristBand"),
    RING("HumotronRing"),
    UNKNOWN("Unknown");

    companion object {
        fun from(value: String?): DeviceType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}