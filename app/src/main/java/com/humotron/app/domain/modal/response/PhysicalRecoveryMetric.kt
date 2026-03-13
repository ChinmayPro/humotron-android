package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhysicalRecoveryMetric(
    @SerializedName("time")
    val time: String?,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("value")
    val value: Double?,
    @SerializedName("metricName")
    val metricName: String?,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String?,
    @SerializedName("metricId")
    val metricId: String?,
) : Parcelable
