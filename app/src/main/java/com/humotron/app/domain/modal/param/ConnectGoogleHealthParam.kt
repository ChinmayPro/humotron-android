package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class ConnectGoogleHealthParam(
    @SerializedName("platform")
    val platform: String = "android"
)
