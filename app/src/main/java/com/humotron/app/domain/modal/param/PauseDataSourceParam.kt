package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class PauseDataSourceParam(
    @SerializedName("isPaused")
    val isPaused: Boolean? = null,
    @SerializedName("includeInAnalysis")
    val includeInAnalysis: Boolean? = null
)
