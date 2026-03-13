package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class NuggetsInteraction(
    val nuggetId: String,
    val anecdoteId: String?,
    @SerializedName("interaction")
    val interactionType: String,
    @SerializedName("detailPageView")
    val detailPage: Boolean
)