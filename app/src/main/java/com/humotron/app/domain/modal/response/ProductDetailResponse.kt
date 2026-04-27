package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductDetailResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: ProductData?
) : Parcelable {

    @Parcelize
    data class ProductData(
        @SerializedName("product")
        val product: Product?
    ) : Parcelable

    @Parcelize
    data class Product(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("productName")
        val productName: String?,
        @SerializedName("productDesc")
        val productDesc: String?,
        @SerializedName("supplyDuration")
        val supplyDuration: String?,
        @SerializedName("whenToTakeSentence")
        val whenToTakeSentence: String?,
        @SerializedName("whenToTakeOneWord")
        val whenToTakeOneWord: String?,
        @SerializedName("productType")
        val productType: String?,
        @SerializedName("productPrice")
        val productPrice: String?,
        @SerializedName("productDossage")
        val productDossage: String?,
        @SerializedName("packSize")
        val packSize: String?,
        @SerializedName("keyIngredients")
        val keyIngredients: List<KeyIngredient>?,
        @SerializedName("brandName")
        val brandName: String?,
        @SerializedName("howToTake")
        val howToTake: String?,
        @SerializedName("howToWork")
        val howToWork: String?,
        @SerializedName("courseLength")
        val courseLength: String?,
        @SerializedName("whyProduct")
        val whyProduct: WhyProduct?,
        @SerializedName("productImage")
        val productImage: String?,
        @SerializedName("vat")
        val vat: String?,
        @SerializedName("productFaqs")
        val productFaqs: List<ProductFaq>?,
        @SerializedName("chatPrompts")
        val chatPrompts: List<ChatPrompt>?,
        @SerializedName("isLiked")
        val isLiked: Boolean?
    ) : Parcelable

    @Parcelize
    data class KeyIngredient(
        @SerializedName("name")
        val name: String?,
        @SerializedName("description")
        val description: String?
    ) : Parcelable

    @Parcelize
    data class WhyProduct(
        @SerializedName("title")
        val title: String?,
        @SerializedName("list")
        val list: List<WhyProductItem>?
    ) : Parcelable

    @Parcelize
    data class WhyProductItem(
        @SerializedName("short")
        val short: String?,
        @SerializedName("long")
        val long: String?
    ) : Parcelable

    @Parcelize
    data class ProductFaq(
        @SerializedName("question")
        val question: String?,
        @SerializedName("answer")
        val answer: String?
    ) : Parcelable

    @Parcelize
    data class ChatPrompt(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("type")
        val type: String?
    ) : Parcelable
}
