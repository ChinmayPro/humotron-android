package com.humotron.app.domain.modal.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthScanResult(
    val stress: Int,
    val lowPressure: Int,
    val heartRate: Int,
    val temperature: Float,
    val bloodOxygen: Int,
    val hrv: Int,
    val highPressure: Int,
    val timestamp: Long = System.currentTimeMillis(),
) : Parcelable
