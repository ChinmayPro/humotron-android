package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetConversationThreadsParam(
    @SerializedName("sortingOrder")
    val sortingOrder: String = "desc"
) : Parcelable
