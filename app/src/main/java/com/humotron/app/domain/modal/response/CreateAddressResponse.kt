package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class CreateAddressResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Any? = null
)
