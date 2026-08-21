package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UntrackedMetricResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: UntrackedMetricDataWrapper? = null,
    @SerializedName("untrackedMetricCount")
    val untrackedMetricCount: Int? = null
) : Parcelable

@Parcelize
data class UntrackedMetricDataWrapper(
    @SerializedName("individualMetrics")
    val individualMetrics: List<UntrackedMetricData>? = null,
    @SerializedName("groupedMetrics")
    val groupedMetrics: List<GroupedMetricData>? = null
) : Parcelable

@Parcelize
data class UntrackedMetricData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricWhat")
    val metricWhat: String? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("deviceId")
    val deviceId: String? = null
) : Parcelable
