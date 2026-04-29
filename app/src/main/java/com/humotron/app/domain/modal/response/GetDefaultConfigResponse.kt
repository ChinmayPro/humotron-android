package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetDefaultConfigResponse(
    @SerializedName("vatNumber")
    val vatNumber: String?,
    @SerializedName("coupon")
    val coupon: GetCartResponse.CouponDetails?,
    @SerializedName("deliveryMethod")
    val deliveryMethod: GetCartResponse.DeliveryMethod?,
    @SerializedName("emailInvoice")
    val emailInvoice: Boolean?,
    @SerializedName("ringFingerSize")
    val ringFingerSize: String?,
    @SerializedName("address")
    val address: GetCartResponse.Address?
) : Parcelable
