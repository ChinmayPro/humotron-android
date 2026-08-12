package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class UpdateDataSourceUsageParam(
    @SerializedName("aiChat")
    val aiChat: Boolean? = null,
    @SerializedName("aiInsights")
    val aiInsights: Boolean? = null,
    @SerializedName("productSuggestions")
    val productSuggestions: Boolean? = null,
    @SerializedName("deepDives")
    val deepDives: Boolean? = null
)
