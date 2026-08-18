package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatConfigRequest(
    @SerializedName("preferences")
    val preferences: Preferences? = null
) : Parcelable {

    @Parcelize
    data class Preferences(
        @SerializedName("chatHistoryDays")
        val chatHistoryDays: Int? = null,
        @SerializedName("focus")
        val focus: String? = null,
        @SerializedName("context")
        val context: ContextParam? = null,
        @SerializedName("responseStyle")
        val responseStyle: String? = null
    ) : Parcelable

    @Parcelize
    data class ContextParam(
        @SerializedName("guidance")
        val guidance: Boolean? = null,
        @SerializedName("both")
        val both: Boolean? = null
    ) : Parcelable
}
