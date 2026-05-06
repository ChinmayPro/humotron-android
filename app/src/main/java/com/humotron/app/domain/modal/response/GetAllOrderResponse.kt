package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetAllOrderResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("orderId")
    val orderList: List<Order>?
) : Parcelable {

    @Parcelize
    data class Order(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("orderNumber")
        val orderNumber: String?,
        @SerializedName("updatedAt")
        val updatedAt: String?,
        @SerializedName("orderStatusName")
        val orderStatusName: String?,
        @SerializedName("payableAmount")
        val payableAmount: Double?,
        @SerializedName("cartProducts")
        val cartProducts: List<CartProduct>?,
        @SerializedName("estimatedDelivery")
        val estimatedDelivery: String?,
        @SerializedName("statusColorCode")
        val statusColorCode: String?
    ) : Parcelable

    @Parcelize
    data class CartProduct(
        @SerializedName("productId")
        val productId: String?,
        @SerializedName("quantity")
        val quantity: Int?
    ) : Parcelable
}
