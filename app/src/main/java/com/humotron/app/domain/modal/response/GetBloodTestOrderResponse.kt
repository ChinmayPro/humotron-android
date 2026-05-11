package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetBloodTestOrderResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val orderList: List<BloodTestOrder>?
) : Parcelable {

    @Parcelize
    data class BloodTestOrder(
        @SerializedName("orderId")
        val orderId: String?,
        @SerializedName("orderNumber")
        val orderNumber: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("statusColor")
        val statusColor: String?,
        @SerializedName("paymentDate")
        val paymentDate: String?,
        @SerializedName("paymentMethod")
        val paymentMethod: String?,
        @SerializedName("amountPaid")
        val amountPaid: Double?,
        @SerializedName("productType")
        val productType: String?,
        @SerializedName("bookingType")
        val bookingType: String?,
        @SerializedName("addressId")
        val addressId: String?,
        @SerializedName("address")
        val address: Address?,
        @SerializedName("date")
        val date: String?,
        @SerializedName("time")
        val time: String?,
        @SerializedName("labId")
        val labId: String?,
        @SerializedName("labName")
        val labName: String?,
        @SerializedName("labAddress")
        val labAddress: LabAddress?,
        @SerializedName("labPrice")
        val labPrice: Double?
    ) : Parcelable

    @Parcelize
    data class Address(
        @SerializedName("firstName")
        val firstName: String?,
        @SerializedName("lastName")
        val lastName: String?,
        @SerializedName("contactNo")
        val contactNo: String?,
        @SerializedName("address1")
        val address1: String?,
        @SerializedName("address2")
        val address2: String?,
        @SerializedName("address3")
        val address3: String?,
        @SerializedName("postcode")
        val postcode: String?,
        @SerializedName("city")
        val city: String?,
        @SerializedName("country")
        val country: String?,
        @SerializedName("isDefault")
        val isDefault: Boolean?,
        @SerializedName("_id")
        val id: String?
    ) : Parcelable

    @Parcelize
    data class LabAddress(
        @SerializedName("line1")
        val line1: String?,
        @SerializedName("line2")
        val line2: String?,
        @SerializedName("city")
        val city: String?,
        @SerializedName("postcode")
        val postcode: String?,
        @SerializedName("country")
        val country: String?,
        @SerializedName("latitude")
        val latitude: Double?,
        @SerializedName("longitude")
        val longitude: Double?
    ) : Parcelable
}
