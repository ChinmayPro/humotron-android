package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NuggetDetailResponse(
    @SerializedName("data")
    val `data`: NuggetDetail?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class NuggetDetail(
        @SerializedName("nugget")
        val nugget: Nugget?
    ) : Parcelable {
        @Parcelize
        data class Nugget(
            @SerializedName("anecdotes")
            val anecdotes: List<Anecdote>?,
            @SerializedName("bookRecommendation")
            val bookRecommendation: List<BookRecommendation>?,
            @SerializedName("callAction")
            val callAction: String?,
            @SerializedName("category")
            val category: Category?,
            @SerializedName("chatPrompts")
            val chatPrompts: List<String>?,
            @SerializedName("communityExperience")
            val communityExperience: String?,
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("deepDives")
            val deepDives: List<DeepDive>?,
            @SerializedName("expertCommentary")
            val expertCommentary: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("isDeleted")
            val isDeleted: Boolean?,
            @SerializedName("learningLevel")
            val learningLevel: String?,
            @SerializedName("nuggetTopic")
            val nuggetTopic: String?,
            @SerializedName("overview")
            val overview: String?,
            @SerializedName("primaryTag")
            val primaryTag: PrimaryTag?,
            @SerializedName("uniqueId")
            val uniqueId: String?,
            @SerializedName("updatedAt")
            val updatedAt: String?
        ) : Parcelable {
            @Parcelize
            data class Anecdote(
                @SerializedName("content")
                val content: String?,
                @SerializedName("source")
                val source: String?,
                @SerializedName("tag")
                val tag: Tag?
            ) : Parcelable {
                @Parcelize
                data class Tag(
                    @SerializedName("_id")
                    val id: String?,
                    @SerializedName("tagName")
                    val tagName: String?
                ) : Parcelable
            }

            @Parcelize
            data class BookRecommendation(
                @SerializedName("author")
                val author: String?,
                @SerializedName("bookRecommendation")
                val bookRecommendation: com.humotron.app.domain.modal.response.BookRecommendation?,
                @SerializedName("bookTitle")
                val bookTitle: String?,
                @SerializedName("purchaseLink")
                val purchaseLink: String?
            ) : Parcelable

            @Parcelize
            data class Category(
                @SerializedName("_id")
                val id: String?,
                @SerializedName("tagName")
                val tagName: String?
            ) : Parcelable

            @Parcelize
            data class DeepDive(
                @SerializedName("content")
                val content: String?,
                @SerializedName("prompt1")
                val prompt1: String?,
                @SerializedName("prompt2")
                val prompt2: String?,
                @SerializedName("source")
                val source: String?,
                @SerializedName("subtitle")
                val subtitle: String?,
                @SerializedName("title")
                val title: String?
            ) : Parcelable

            @Parcelize
            data class PrimaryTag(
                @SerializedName("_id")
                val id: String?,
                @SerializedName("tagName")
                val tagName: String?
            ) : Parcelable
        }
    }
}