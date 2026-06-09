package com.humotron.app.domain.modal

enum class BPMachineDeviceStatus(
    val value: Int,
    val label: String,
) {
    STATUS_SLEEP(
        0,
        "Device is sleeping"
    ),
    STATUS_MEMERY(
        1,
        "Viewing memory records"
    ),
    STATUS_CHARGE(
        2,
        "Device is charging"
    ),
    STATUS_READY(
        3,
        "Device is ready"
    ),
    STATUS_BP_MEASURING(
        4,
        "Blood pressure measurement in progress"
    ),
    STATUS_BP_MEASURE_END(
        5,
        "Blood pressure measurement completed"
    ),
    STATUS_ECG_MEASURING(
        6,
        "ECG measurement in progress"
    ),
    STATUS_ECG_MEASURE_END(
        7,
        "ECG measurement completed"
    ),
    STATUS_VEN(
        20,
        "Venous mode active"
    ),
    UNKNOWN(
        -1,
        "Unknown device status"
    );

    companion object {
        fun fromValue(value: Int): BPMachineDeviceStatus {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}
