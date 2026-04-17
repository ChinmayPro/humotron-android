package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class ProductVariantResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ProductVariantData? = null
)

data class ProductVariantData(
    @SerializedName("isUniversal")
    val isUniversal: Boolean? = null,
    @SerializedName("color")
    val color: List<ColorVariant>? = null,
    @SerializedName("offers")
    val offers: List<Any>? = null,
    @SerializedName("deviceName")
    val deviceName: String? = null,
    @SerializedName("price")
    val price: String? = null,
    @SerializedName("isLiked")
    val isLiked: Boolean? = null,
    @SerializedName("size")
    val size: List<SizeVariant>? = null,
    @SerializedName("deviceTextMessage")
    val deviceTextMessage: String? = null,
    @SerializedName("deviceUrl")
    val deviceUrl: List<String>? = null
)

data class ColorVariant(
    @SerializedName("quantity")
    val quantity: Int? = null,
    @SerializedName("isSelected")
    val isSelected: Boolean? = null,
    @SerializedName("image")
    val image: List<String>? = null,
    @SerializedName("colorName")
    val colorName: String? = null,
    @SerializedName("price")
    val price: Int? = null
)

data class SizeVariant(
    @SerializedName("size")
    val size: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("quantity")
    val quantity: Int? = null,
    @SerializedName("isSelected")
    val isSelected: Boolean? = null,
    @SerializedName("price")
    val price: Int? = null,
    @SerializedName("colorName")
    val colorName: String? = null
)
