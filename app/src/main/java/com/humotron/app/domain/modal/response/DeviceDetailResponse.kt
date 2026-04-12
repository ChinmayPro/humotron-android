package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceDetailResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<DeviceDetail>?
) : Parcelable {

    @Parcelize
    data class DeviceDetail(
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
        val isLiked: Boolean?,
        @SerializedName("vat")
        val vat: String?,
        @SerializedName("deviceCategoryName")
        val deviceCategoryName: String?,
        @SerializedName("deviceTextMessage")
        val deviceTextMessage: String?,
        @SerializedName("dataSync")
        val dataSync: String?,
        @SerializedName("deviceCategory")
        val deviceCategory: DeviceCategory?,
        @SerializedName("deviceSubCategory")
        val deviceSubCategory: DeviceSubCategory?
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

    @Parcelize
    data class DeviceCategory(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("deviceCategoryName")
        val deviceCategoryName: String?,
        @SerializedName("deviceCategoryShortDesc")
        val deviceCategoryShortDesc: String?,
        @SerializedName("deviceCategoryLongDesc")
        val deviceCategoryLongDesc: String?
    ) : Parcelable

    @Parcelize
    data class DeviceSubCategory(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("deviceSubCategoryName")
        val deviceSubCategoryName: String?,
        @SerializedName("deviceCategoryId")
        val deviceCategoryId: String?
    ) : Parcelable
}
