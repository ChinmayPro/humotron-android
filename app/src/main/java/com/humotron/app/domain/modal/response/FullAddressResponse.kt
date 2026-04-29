package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class FullAddressResponse(
    @SerializedName("postcode")
    val postcode: String?,
    @SerializedName("line_1")
    val line1: String?,
    @SerializedName("line_2")
    val line2: String?,
    @SerializedName("line_3")
    val line3: String?,
    @SerializedName("line_4")
    val line4: String?,
    @SerializedName("locality")
    val locality: String?,
    @SerializedName("town_or_city")
    val townOrCity: String?,
    @SerializedName("county")
    val county: String?,
    @SerializedName("country")
    val country: String?
)
