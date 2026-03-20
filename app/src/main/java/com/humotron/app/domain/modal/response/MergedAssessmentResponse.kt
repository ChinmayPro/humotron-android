package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class MergedAssessmentResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<MergedAssessment>
)
