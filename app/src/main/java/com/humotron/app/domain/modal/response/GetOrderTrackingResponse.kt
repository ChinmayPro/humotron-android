package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetOrderTrackingResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: TrackingDetails?
) : Parcelable {

    @Parcelize
    data class TrackingDetails(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("orderId")
        val orderId: String?,
        @SerializedName("createdAt")
        val createdAt: String?,
        @SerializedName("history")
        val history: List<HistoryEvent>?,
        @SerializedName("isDeleted")
        val isDeleted: Boolean?,
        @SerializedName("orderStatus")
        val orderStatus: String?,
        @SerializedName("orderStatusName")
        val orderStatusName: String?,
        @SerializedName("statusColorCode")
        val statusColorCode: String?,
        @SerializedName("updatedAt")
        val updatedAt: String?,
        @SerializedName("userId")
        val userId: String?
    ) : Parcelable

    @Parcelize
    data class HistoryEvent(
        @SerializedName("status")
        val status: String?,
        @SerializedName("statusColorCode")
        val statusColorCode: String?,
        @SerializedName("statusName")
        val statusName: String?,
        @SerializedName("timestamp")
        val timestamp: Long?,
        @SerializedName("isCompleted")
        val isCompleted: Boolean?
    ) : Parcelable
}
