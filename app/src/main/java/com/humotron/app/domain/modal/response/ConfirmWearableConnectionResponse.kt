package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class ConfirmWearableConnectionResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Data? = null
) {
    data class Data(
        @SerializedName("provider")
        val provider: String? = null,
        @SerializedName("isConnected")
        val isConnected: Boolean? = null,
        @SerializedName("connectionStatus")
        val connectionStatus: String? = null
    )
}
