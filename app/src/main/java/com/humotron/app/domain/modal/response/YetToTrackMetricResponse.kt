package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class YetToTrackMetricResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<YetToTrackMetricData>? = null
) : Parcelable

@Parcelize
data class YetToTrackMetricData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null
) : Parcelable
