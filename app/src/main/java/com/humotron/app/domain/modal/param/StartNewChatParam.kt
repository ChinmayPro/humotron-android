package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StartNewChatParam(
    @SerializedName("type")
    val type: String,
    @SerializedName("promptId")
    val promptId: String,
    @SerializedName("metricName")
    val metricName: String
) : Parcelable
