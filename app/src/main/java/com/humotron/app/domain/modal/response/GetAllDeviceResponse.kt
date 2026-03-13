package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetAllDeviceResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("dataSyncPercentage")
    val dataSyncPercentage: Int?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("Wearables")
        val wearables: List<Wearable>?
    ) : Parcelable {
        @Parcelize
        data class Wearable(
            @SerializedName("dataSync")
            val dataSync: String?,
            @SerializedName("deviceCategoryName")
            val deviceCategoryName: String?,
            @SerializedName("deviceFacingName")
            val deviceFacingName: String?,
            @SerializedName("deviceImage")
            val deviceImage: List<String?>?,
            @SerializedName("deviceModelId")
            val deviceModelId: String?,
            @SerializedName("deviceModelName")
            val deviceModelName: String?,
            @SerializedName("deviceName")
            val deviceName: String?,
            @SerializedName("deviceSubCategoryId")
            val deviceSubCategoryId: String?,
            @SerializedName("deviceSubCategoryName")
            val deviceSubCategoryName: String?,
            @SerializedName("deviceTextMessage")
            val deviceTextMessage: String?,
            @SerializedName("deviceType")
            val deviceType: String?,
            @SerializedName("deviceUrl")
            val deviceUrl: List<String>?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("metrics")
            val metrics: List<Metric>?,
            @SerializedName("orderStatus")
            val orderStatus: String?
        ) : Parcelable {
            @Parcelize
            data class Metric(
                @SerializedName("key")
                val key: String?,
                @SerializedName("shortMetricName")
                val shortMetricName: String?,
                @SerializedName("unit")
                val unit: String?,
                @SerializedName("value")
                val value: String?
            ) : Parcelable
        }
    }
}