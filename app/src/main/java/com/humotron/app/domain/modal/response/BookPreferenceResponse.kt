package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookPreferenceResponse(
    @SerializedName("data")
    val `data`: BookData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class BookData(
        @SerializedName("books")
        val books: List<Book>?
    ) : Parcelable {
        @Parcelize
        data class Book(
            @SerializedName("bookRecommendation")
            val bookRecommendation: List<BookRecommendation>?,
            @SerializedName("category")
            val category: String?,
            @SerializedName("primaryTag")
            val primaryTag: String?
        ) : Parcelable


    }
}