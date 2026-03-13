package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookLikeResponse(
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
            @SerializedName("bookId")
            val bookId: String?,
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("isDeleted")
            val isDeleted: Boolean?,
            @SerializedName("isLiked")
            val isLiked: Boolean?,
            @SerializedName("updatedAt")
            val updatedAt: String?,
            @SerializedName("userId")
            val userId: String?
        ) : Parcelable
    }
}