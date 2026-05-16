package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class PlaceOrderResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("orderId")
    val orderId: String?
)
