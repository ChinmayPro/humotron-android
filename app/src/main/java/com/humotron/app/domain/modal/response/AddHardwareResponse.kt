package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddHardwareResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("deviceDetails")
        val deviceDetails: DeviceDetails?,
        @SerializedName("userHardware")
        val userHardware: UserHardware?
    ) : Parcelable {
        @Parcelize
        data class DeviceDetails(
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
            val deviceUrl: List<String?>?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("metrics")
            val metrics: List<Metric?>?,
            @SerializedName("orderStatus")
            val orderStatus: String?
        ) : Parcelable {
            @Parcelize
            data class Metric(
                @SerializedName("key")
                val key: String?,
                @SerializedName("value")
                val value: String?
            ) : Parcelable
        }


    }
}

@Parcelize
data class UserHardware(
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("hardwareType")
    val hardwareType: String?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("isDeleted")
    val isDeleted: Boolean?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("userHardwareUUID")
    val userHardwareUUID: String?,
    @SerializedName("userId")
    val userId: String?
) : Parcelable