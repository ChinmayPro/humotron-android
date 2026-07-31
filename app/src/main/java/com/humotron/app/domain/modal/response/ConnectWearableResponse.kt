package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class ConnectWearableResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Data? = null
) {
    data class Data(
        @SerializedName("authorizationUrl")
        val authorizationUrl: String? = null,
        @SerializedName("provider")
        val provider: String? = null,
        @SerializedName("platform")
        val platform: String? = null
    )
}
