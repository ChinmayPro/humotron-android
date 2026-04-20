package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookAddToCartResponse(
    @SerializedName("data")
    val data: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("cart")
        val cart: List<CartItem>?,
        var id: String?
    ) : Parcelable

    @Parcelize
    data class CartItem(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("productId")
        val productId: String?,
        @SerializedName("productType")
        val productType: String?,
        @SerializedName("quantity")
        val quantity: Int?,
        @SerializedName("totalAmount")
        val totalAmount: Double?,
        @SerializedName("variantId")
        val variantId: String?
    ) : Parcelable
}
