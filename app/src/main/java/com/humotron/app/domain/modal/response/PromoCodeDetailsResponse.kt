package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PromoCodeDetailsResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("error")
    val error: String?,
    @SerializedName("coupon")
    val coupon: @RawValue com.google.gson.JsonElement?,
    @SerializedName("data")
    val data: @RawValue com.google.gson.JsonElement?
) : Parcelable
