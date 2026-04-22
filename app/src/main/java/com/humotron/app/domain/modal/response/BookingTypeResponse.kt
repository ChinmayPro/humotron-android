package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookingTypeResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<BookingType>?
) : Parcelable {

    @Parcelize
    data class BookingType(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("productName")
        val title: String?,
        @SerializedName("productDesc")
        val description: String?,
        @SerializedName("productPrice")
        val price: String?,
        @SerializedName("testBookingTypeCurrency")
        val currency: String? = "$",
        var isSelected: Boolean = false
    ) : Parcelable
}
