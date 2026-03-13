package com.humotron.app.domain.modal.param


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddToCartParam(
    @SerializedName("cartItemId")
    val cartItemId: String?,
    @SerializedName("productId")
    val productId: String?,
    @SerializedName("productType")
    val productType: String?,
    @SerializedName("quantity")
    val quantity: Int?,
    @SerializedName("variantId")
    val variantId: String?
) : Parcelable