package com.humotron.app.ui.device.adapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthScanItem(
    val title: String,
    val header: String,
    val description: String,
    val type: HealthScanType,
    val metricName: String = ""
) : Parcelable