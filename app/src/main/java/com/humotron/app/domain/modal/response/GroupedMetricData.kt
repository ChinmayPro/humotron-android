package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupedMetricData(
    @SerializedName("categoryId")
    val categoryId: String? = null,
    @SerializedName("categoryName")
    val categoryName: String? = null,
    @SerializedName("categoryDescription")
    val categoryDescription: String? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("deviceId")
    val deviceId: String? = null,
    @SerializedName("hasActiveBooster")
    val hasActiveBooster: Boolean? = null
) : Parcelable
