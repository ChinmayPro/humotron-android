package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class PastScanResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<PastScanData>?
)

data class PastScanData(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("userId")
    val userId: String?,
    @SerializedName("deviceId")
    val deviceId: String?,
    @SerializedName("baseline")
    val baseline: Double?,
    @SerializedName("current")
    val current: Double?,
    @SerializedName("isDeleted")
    val isDeleted: Boolean?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("createdAt")
    val createdAt: String?
)
