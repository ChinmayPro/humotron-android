package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class UpdateAddressRequest(
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("contactNo") val contactNo: String?,
    @SerializedName("address1") val address1: String?,
    @SerializedName("address2") val address2: String?,
    @SerializedName("address3") val address3: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("isDefault") val isDefault: Boolean?
)
