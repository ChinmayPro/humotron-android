package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.humotron.app.domain.modal.response.GetCartResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryOptionResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: DeliveryData?
) : Parcelable {
    @Parcelize
    data class DeliveryData(
        @SerializedName("deliveryOptions")
        val deliveryOptions: List<GetCartResponse.DeliveryMethod>?,
        @SerializedName("totalRecords")
        val totalRecords: Int?
    ) : Parcelable
}
