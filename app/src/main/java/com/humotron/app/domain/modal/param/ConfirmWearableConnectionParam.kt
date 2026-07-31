package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class ConfirmWearableConnectionParam(
    @SerializedName("provider")
    val provider: String? = null
)
