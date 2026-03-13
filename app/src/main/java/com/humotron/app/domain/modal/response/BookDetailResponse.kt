package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookDetailResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("book")
        val book: Book?
    ) : Parcelable {
        @Parcelize
        data class Book(
            @SerializedName("author1")
            val author1: String?,
            @SerializedName("author2")
            val author2: String?,
            @SerializedName("bookId")
            val bookId: String?,
            @SerializedName("bookTitle")
            val bookTitle: String?,
            @SerializedName("category")
            val category: Category?,
            @SerializedName("coverUrl")
            val coverUrl: String?,
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("crossSellCallout")
            val crossSellCallout: String?,
            @SerializedName("endorseBy")
            val endorseBy: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("isDeleted")
            val isDeleted: Boolean?,
            @SerializedName("keyTakeaways")
            val keyTakeaways: ArrayList<String>?,
            @SerializedName("longDesc")
            val longDesc: String?,
            @SerializedName("price")
            val price: String?,
            @SerializedName("primaryTag")
            val primaryTag: PrimaryTag?,
            @SerializedName("rating")
            val rating: String?,
            @SerializedName("referralLink")
            val referralLink: String?,
            @SerializedName("relatedBooks")
            val relatedBooks: List<RelatedBook>?,
            @SerializedName("shortDesc")
            val shortDesc: String?,
            @SerializedName("updatedAt")
            val updatedAt: String?,
            @SerializedName("userFeedback")
            val userFeedback: String?,
            @SerializedName("userReview")
            val userReview: ArrayList<String>?,
            @SerializedName("vat")
            val vat: String?
        ) : Parcelable {
            @Parcelize
            data class Category(
                @SerializedName("_id")
                val id: String?,
                @SerializedName("tagName")
                val tagName: String?
            ) : Parcelable

            @Parcelize
            data class PrimaryTag(
                @SerializedName("_id")
                val id: String?,
                @SerializedName("tagName")
                val tagName: String?
            ) : Parcelable

            @Parcelize
            data class RelatedBook(
                @SerializedName("bookTitle")
                val bookTitle: String?,
                @SerializedName("_id")
                val id: String?
            ) : Parcelable
        }
    }
}