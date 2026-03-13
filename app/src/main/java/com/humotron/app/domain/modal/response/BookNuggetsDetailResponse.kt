package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookNuggetsDetailResponse(
    @SerializedName("data")
    val `data`: BookData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable

@Parcelize
data class BookData(
    @SerializedName("books")
    val books: List<Book>?
) : Parcelable

@Parcelize
data class Book(
    @SerializedName("bookRecommendation")
    val bookRecommendation: List<BookRecommendation>?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("primaryTag")
    val primaryTag: String?
) : Parcelable

@Parcelize
data class BookRecommendation(
    @SerializedName("author1")
    val author1: String?,
    @SerializedName("bookTitle")
    val bookTitle: String?,
    @SerializedName("category")
    val category: Category?,
    @SerializedName("coverUrl")
    val coverUrl: String?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("isCart")
    val isCart: Boolean?,
    @SerializedName("isLiked")
    val isLiked: Boolean?,
    @SerializedName("longDesc")
    val longDesc: String?,
    @SerializedName("price")
    val price: String?,
    @SerializedName("primaryTag")
    val primaryTag: PrimaryTag?,
    @SerializedName("shortDesc")
    val shortDesc: String?,
    @SerializedName("vat")
    val vat: String?
) : Parcelable


data class BookModal(
    val type: Int,
    val primaryTag: TagModal? = null,
    val bookRecommendation: List<BookRecommendation>? = null,
)

data class TagModal(
    val primaryTag: String?,
    val category: String?
)


enum class BookType {
    TAG, BOOK, NUGGET, GIST, SEE_MORE
}




