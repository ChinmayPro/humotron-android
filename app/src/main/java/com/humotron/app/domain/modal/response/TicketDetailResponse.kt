package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TicketDetailResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: TicketDetailData?
) : Parcelable

@Parcelize
data class TicketDetailData(
    @SerializedName("ticket")
    val ticket: TicketDetail?
) : Parcelable
