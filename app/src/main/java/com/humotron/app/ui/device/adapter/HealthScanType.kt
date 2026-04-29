package com.humotron.app.ui.device.adapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class HealthScanType(val value: String, val type: Int) : Parcelable {
    HRV("HRV", 1),
    HR("HR", 2),
    SPO2("SPO2", 3),
    TEMPERATURE("TEMPERATURE", 4);

    fun getDisplayName(): String {
        return when (this) {
            HRV -> "HRV"
            HR -> "Heart Rate"
            SPO2 -> "SPO2"
            TEMPERATURE -> "Temperature"
        }
    }

    fun getDisplayName2(): String {
        return when (this) {
            HRV -> "HRV"
            HR -> "HR"
            SPO2 -> "SPO2"
            TEMPERATURE -> "Temperature"
        }
    }

    fun getDisplayName3(): String {
        return when (this) {
            HRV -> "Stress Scan?"
            HR -> "Body Load Scan?"
            SPO2 -> "Oxygen Check?"
            TEMPERATURE -> "Thermal Balance"
        }
    }

    fun getUnit(): String {
        return when (this) {
            HRV -> "ms"
            HR -> "bpm"
            SPO2 -> "%"
            TEMPERATURE -> "°c"
        }
    }

}
