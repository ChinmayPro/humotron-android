package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class YetToTrackMetricDetailsResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: YetToTrackMetricDetailsData? = null
) : Parcelable

@Parcelize
data class YetToTrackMetricDetailsData(
    @SerializedName("metricId")
    val metricId: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricUnit")
    val metricUnit: String? = null,
    @SerializedName("metricDescription")
    val metricDescription: String? = null,
    @SerializedName("metricWhat")
    val metricWhat: String? = null,
    @SerializedName("metricWhy")
    val metricWhy: String? = null,
    @SerializedName("intro")
    val intro: String? = null,
    @SerializedName("practice")
    val practice: String? = null,
    @SerializedName("whyMatters")
    val whyMatters: String? = null,
    @SerializedName("thingsAccuracy")
    val thingsAccuracy: List<String>? = null,
    @SerializedName("category")
    val category: YetToTrackCategory? = null,
    @SerializedName("devices")
    val devices: List<YetToTrackDevice>? = null,
    @SerializedName("moreMetrics")
    val moreMetrics: List<YetToTrackMoreMetric>? = null,
    @SerializedName("supportedDeviceCount")
    val supportedDeviceCount: Int? = null
) : Parcelable

@Parcelize
data class YetToTrackCategory(
    @SerializedName("categoryId")
    val categoryId: String? = null,
    @SerializedName("categoryName")
    val categoryName: String? = null,
    @SerializedName("categoryDescription")
    val categoryDescription: String? = null
) : Parcelable

@Parcelize
data class YetToTrackDevice(
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("deviceImage")
    val deviceImage: List<String>? = null,
    @SerializedName("deviceModelDesc")
    val deviceModelDesc: String? = null,
    @SerializedName("deviceModelPrice")
    val deviceModelPrice: String? = null
) : Parcelable

@Parcelize
data class YetToTrackMoreMetric(
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricDescription")
    val metricDescription: String? = null,
    @SerializedName("deviceNames")
    val deviceNames: List<String>? = null
) : Parcelable
