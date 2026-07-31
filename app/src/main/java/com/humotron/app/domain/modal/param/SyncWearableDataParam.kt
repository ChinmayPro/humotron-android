package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class SyncWearableDataParam(
    @SerializedName("provider")
    val provider: String,
    @SerializedName("isHistorical")
    val isHistorical: Boolean = true
)
