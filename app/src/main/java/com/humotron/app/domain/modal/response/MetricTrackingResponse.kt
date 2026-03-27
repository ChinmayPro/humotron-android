package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetricTrackingResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<MetricTrackingData>? = null
) : Parcelable

@Parcelize
data class MetricTrackingData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricUnit")
    val metricUnit: String? = null,
    @SerializedName("metricReading")
    val metricReading: String? = null,
    @SerializedName("metricReadingUnit")
    val metricReadingUnit: String? = null,
    @SerializedName("metricDuration")
    val metricDuration: String? = null
) : Parcelable
