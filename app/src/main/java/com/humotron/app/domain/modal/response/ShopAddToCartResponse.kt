package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopAddToCartResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("userId")
        val userId: String?,
        @SerializedName("cart")
        val cart: List<CartItem>?,
        @SerializedName("isDeleted")
        val isDeleted: Boolean?,
        @SerializedName("createdAt")
        val createdAt: String?,
        @SerializedName("updatedAt")
        val updatedAt: String?
    ) : Parcelable

    @Parcelize
    data class CartItem(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("productId")
        val productId: String?,
        @SerializedName("variantId")
        val variantId: String?,
        @SerializedName("quantity")
        val quantity: Int?,
        @SerializedName("productType")
        val productType: String?,
        @SerializedName("totalAmount")
        val totalAmount: Double?
    ) : Parcelable
}
