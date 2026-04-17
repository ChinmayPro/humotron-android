package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceFaqResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<FaqData>?
) : Parcelable {

    @Parcelize
    data class FaqData(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("question")
        val question: String?,
        @SerializedName("answer")
        val answer: String?
    ) : Parcelable
}
