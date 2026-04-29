package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HrvSaveScanResponse(
    @SerializedName("status")
    val status: String = "",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: HrvScanData = HrvScanData(),
) : Parcelable

@Parcelize
data class HrvScanData(
    @SerializedName("userId")
    val userId: String = "",

    @SerializedName("deviceId")
    val deviceId: String = "",

    @SerializedName("baseline")
    val baseline: Double = 0.0,

    @SerializedName("current")
    val current: Double = 0.0,

    @SerializedName("isDeleted")
    val isDeleted: Boolean = false,

    @SerializedName("_id")
    val id: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("type")
    val type: String = "",

    @SerializedName("createdAt")
    val createdAt: String = "",
) : Parcelable
