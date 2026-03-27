package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetConversationsResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<ConversationData>? = null,
    @SerializedName("totalRecords")
    val totalRecords: Int? = null
) : Parcelable





@Parcelize
data class ConversationData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("userMessage")
    val userMessage: String? = null,
    @SerializedName("botResponse")
    val botResponse: BotResponse? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("conversationThreadId")
    val conversationThreadId: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable


@Parcelize
data class BotResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("message")
    val message: String? = null
) : Parcelable
