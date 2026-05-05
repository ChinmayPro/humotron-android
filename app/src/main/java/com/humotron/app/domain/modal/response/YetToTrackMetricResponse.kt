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
    val data: YetToTrackMetricDataWrapper? = null
) : Parcelable

@Parcelize
data class YetToTrackMetricDataWrapper(
    @SerializedName("individualMetrics")
    val individualMetrics: List<YetToTrackMetricData>? = null,
    @SerializedName("groupedMetrics")
    val groupedMetrics: List<YetToTrackGroupedMetricData>? = null
) : Parcelable

@Parcelize
data class YetToTrackGroupedMetricData(
    @SerializedName("categoryId")
    val categoryId: String? = null,
    @SerializedName("categoryName")
    val categoryName: String? = null,
    @SerializedName("categoryDescription")
    val categoryDescription: String? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null
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
