package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetAllLabResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("Lab")
        val labList: List<Lab>?
    ) : Parcelable

    @Parcelize
    data class Lab(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("labName")
        val labName: String?,
        @SerializedName("address")
        val address: Address?
    ) : Parcelable

    @Parcelize
    data class Address(
        @SerializedName("line1")
        val line1: String?,
        @SerializedName("line2")
        val line2: String?,
        @SerializedName("city")
        val city: String?,
        @SerializedName("postcode")
        val postcode: String?,
        @SerializedName("country")
        val country: String?
    ) : Parcelable
}
