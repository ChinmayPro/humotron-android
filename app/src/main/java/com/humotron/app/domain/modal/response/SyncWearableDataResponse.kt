package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class SyncWearableDataResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Data? = null
) {
    data class Data(
        @SerializedName("taskId")
        val taskId: String? = null,
        @SerializedName("provider")
        val provider: String? = null,
        @SerializedName("isHistorical")
        val isHistorical: Boolean? = null
    )
}
