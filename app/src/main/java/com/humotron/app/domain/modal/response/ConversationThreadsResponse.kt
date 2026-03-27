package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class ConversationThreadsResponse(
    @SerializedName("data")
    val data: List<ConversationThread>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
)

data class ConversationThread(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("userId")
    val userId: String?
)
