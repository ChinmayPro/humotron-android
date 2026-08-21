package com.humotron.app.domain.modal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GmailSearchFilters(
    val keywords: List<String>,
    val hasAttachments: Boolean,
    val dateRange: String
) : Parcelable
