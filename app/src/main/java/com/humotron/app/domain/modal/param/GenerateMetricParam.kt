package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class GenerateMetricParam(
    @SerializedName("pdfId")
    val pdfId: String
)
