package com.humotron.app.domain.modal.response


import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class NuggetsReactionResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("nugget")
        val nugget: Nugget?
    ) : Parcelable {
        @Parcelize
        data class Nugget(
            @SerializedName("anecdoteId")
            val anecdoteId: String?,
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("detailPageView")
            val detailPageView: String?,
            @SerializedName("_id")
            val id: String?,
            @SerializedName("interaction")
            val interaction: String?,
            @SerializedName("isDeleted")
            val isDeleted: Boolean?,
            @SerializedName("nuggetId")
            val nuggetId: String?,
            @SerializedName("updatedAt")
            val updatedAt: String?,
            @SerializedName("userId")
            val userId: String?
        ) : Parcelable
    }
}