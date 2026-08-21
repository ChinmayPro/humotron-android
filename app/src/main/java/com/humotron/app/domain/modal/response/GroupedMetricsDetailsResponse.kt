package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupedMetricsDetailsResponse(
    @SerializedName("data")
    val `data`: GroupedMetricsDetailsData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable

@Parcelize
data class GroupedMetricsDetailsData(
    @SerializedName("categoryDescription")
    val categoryDescription: String?,
    @SerializedName("categoryId")
    val categoryId: String?,
    @SerializedName("categoryName")
    val categoryName: String?,
    @SerializedName("deviceCount")
    val deviceCount: Int?,
    @SerializedName("devices")
    val devices: List<Device>?,
    @SerializedName("intro")
    val intro: String?,
    @SerializedName("metricCount")
    val metricCount: Int?,
    @SerializedName("moreMetrics")
    val moreMetrics: List<MoreMetric>?,
    @SerializedName("practice")
    val practice: String?,
    @SerializedName("thingsAccuracy")
    val thingsAccuracy: List<String>?,
    @SerializedName("whyMatters")
    val whyMatters: String?
) : Parcelable {
    @Parcelize
    data class Device(
        @SerializedName("deviceName")
        val deviceName: String?
    ) : Parcelable

    @Parcelize
    data class MoreMetric(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("deviceNames")
        val deviceNames: List<String>?,
        @SerializedName("metricDescription")
        val metricDescription: String?,
        @SerializedName("metricName")
        val metricName: String?,
        @SerializedName("metricUnit")
        val metricUnit: String?,
        @SerializedName("metricWhat")
        val metricWhat: String?
    ) : Parcelable
}
