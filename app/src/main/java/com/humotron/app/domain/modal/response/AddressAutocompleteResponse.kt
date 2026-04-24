package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class AddressAutocompleteResponse(
    @SerializedName("suggestions")
    val suggestions: List<AddressSuggestion>?
)

data class AddressSuggestion(
    @SerializedName("address")
    val address: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("id")
    val id: String?
)
