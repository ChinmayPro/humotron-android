package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddToCartResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("cart")
        val cart: Cart?,
        var id: String?
    ) : Parcelable {
        @Parcelize
        data class Cart(
            @SerializedName("cart")
            val cart: List<Cart>?,
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("isDeleted")
            val isDeleted: Boolean?,
            @SerializedName("updatedAt")
            val updatedAt: String?,
            @SerializedName("userId")
            val userId: String?
        ) : Parcelable {
            @Parcelize
            data class Cart(
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
    }
}