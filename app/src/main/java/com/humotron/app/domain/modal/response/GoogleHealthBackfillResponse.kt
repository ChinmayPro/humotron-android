package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class GoogleHealthBackfillResponse(
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
        @SerializedName("days")
        val days: Int? = null
    )
}
