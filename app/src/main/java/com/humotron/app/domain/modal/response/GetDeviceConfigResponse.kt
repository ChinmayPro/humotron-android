package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetDeviceConfigResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("device")
        val device: Device?,
        @SerializedName("deviceMeta")
        val deviceMeta: DeviceMeta?,
        @SerializedName("insight")
        val insight: Insight?,
        @SerializedName("warrantyRemainingDays")
        val warrantyRemainingDays: Int?
    ) : Parcelable {
        @Parcelize
        data class Device(
            @SerializedName("serialNumber")
            val serialNumber: String?
        ) : Parcelable

        @Parcelize
        data class DeviceMeta(
            @SerializedName("sn")
            val sn: String?,
            @SerializedName("mac")
            val mac: String?,
            @SerializedName("desc")
            val desc: String?,
            @SerializedName("fw")
            val fw: String?,
            @SerializedName("mf")
            val measureFreq: String?,
            @SerializedName("lpm")
            val lowPowerMode: Boolean?
        ) : Parcelable

        @Parcelize
        data class Insight(
            @SerializedName("usage")
            val usage: String?,
            @SerializedName("daysUsed")
            val daysUsed: Int?,
            @SerializedName("last10DaysUsage")
            val last10DaysUsage: List<Int>?
        ) : Parcelable
    }
}
