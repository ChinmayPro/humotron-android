package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class GetAllScanByTypeParam(
    @SerializedName("type")
    val type: String,
    @SerializedName("deviceId")
    val deviceId: String
)
