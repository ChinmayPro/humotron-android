package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class UpdateAddressResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any? = null
)
