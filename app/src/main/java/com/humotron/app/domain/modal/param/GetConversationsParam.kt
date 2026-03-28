package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetConversationsParam(
    @SerializedName("pageCount")
    val pageCount: Int,
    @SerializedName("promptId")
    val promptId: String? = null,
    @SerializedName("conversationThreadId")
    val conversationThreadId: String? = null,
    @SerializedName("limit")
    val limit: Int
) : Parcelable
