package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NuggetPreference(
    @SerializedName("data")
    val nuggetsData: NuggetsData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable

@Parcelize
data class NuggetsData(
    @SerializedName("hasUserSetPreferences")
    val hasUserSetPreferences: Boolean?,
    @SerializedName("nugget")
    val nugget: List<Nugget>?
) : Parcelable

@Parcelize
data class Nugget(
    @SerializedName("anecdotes")
    val anecdotes: List<Anecdote>?,
    @SerializedName("category")
    val category: Category?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("learningLevel")
    val learningLevel: String?,
    @SerializedName("nuggetTopic")
    val nuggetTopic: String?,
    @SerializedName("primaryTag")
    val primaryTag: PrimaryTag?
) : Parcelable


@Parcelize
data class Anecdote(
    @SerializedName("content")
    val content: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("tag")
    val tag: String?,
    @SerializedName("tagName")
    val tagName: String?
) : Parcelable


@Parcelize
data class Category(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("tagName")
    val tagName: String?,
) : Parcelable

@Parcelize
data class PrimaryTag(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("tagName")
    val tagName: String?,
) : Parcelable


