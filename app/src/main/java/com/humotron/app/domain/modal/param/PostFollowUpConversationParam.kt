package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostFollowUpConversationParam(
    @SerializedName("conversationThreadId")
    val conversationThreadId: String,
    @SerializedName("promptType")
    val promptType: String = "NO",
    @SerializedName("followUpQuestion")
    val followUpQuestion: String
) : Parcelable
