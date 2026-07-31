package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class ConnectProviderParam(
    @SerializedName("provider")
    val provider: String,
    @SerializedName("platform")
    val platform: String = "android"
)
