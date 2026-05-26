package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class CreatePaymentIntentResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) {
    data class Data(
        @SerializedName("clientSecret")
        val clientSecret: String?,
        @SerializedName("paymentIntentId")
        val paymentIntentId: String?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("customerId")
        val customerId: String?,
        @SerializedName("ephemeralKey")
        val ephemeralKey: String?,
        @SerializedName("publishableKey")
        val publishableKey: String?
    )
}
