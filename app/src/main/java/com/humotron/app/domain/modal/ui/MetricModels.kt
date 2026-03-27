package com.humotron.app.domain.modal.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActiveMetric(
    val id: String,
    val value: String,
    val label: String,
    val dateRange: String
) : Parcelable

@Parcelize
data class PendingMetric(
    val id: String,
    val label: String
) : Parcelable
