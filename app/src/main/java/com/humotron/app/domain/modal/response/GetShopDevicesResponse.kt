package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetShopDevicesResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<CategoryData>?
) : Parcelable {

    @Parcelize
    data class CategoryData(
        @SerializedName("deviceCategoryName")
        val deviceCategoryName: String?,
        @SerializedName("deviceCategoryId")
        val deviceCategoryId: String?,
        @SerializedName("devices")
        val devices: List<Device>?
    ) : Parcelable

    @Parcelize
    data class Device(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("deviceName")
        val deviceName: String?,
        @SerializedName("deviceFacingName")
        val deviceFacingName: String?,
        @SerializedName("deviceImage")
        val deviceImage: List<String>?,
        @SerializedName("deviceModel")
        val deviceModel: DeviceModel?,
        @SerializedName("metrics")
        val metrics: List<Metric>?,
        @SerializedName("deviceUrl")
        val deviceUrl: List<String>?,
        @SerializedName("orderStatus")
        val orderStatus: String?,
        @SerializedName("isLiked")
        val isLiked: Boolean?
    ) : Parcelable

    @Parcelize
    data class DeviceModel(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("deviceModelName")
        val deviceModelName: String?,
        @SerializedName("deviceModelPrice")
        val deviceModelPrice: String?,
        @SerializedName("deviceModelDesc")
        val deviceModelDesc: String?
    ) : Parcelable

    @Parcelize
    data class Metric(
        @SerializedName("metricName")
        val metricName: String?,
        @SerializedName("metricWhat")
        val metricWhat: String?
    ) : Parcelable
}
