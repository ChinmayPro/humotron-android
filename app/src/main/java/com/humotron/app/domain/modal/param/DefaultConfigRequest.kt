package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class DefaultConfigRequest(
    @SerializedName("payload")
    val payload: String,
    @SerializedName("iv")
    val iv: String
)
