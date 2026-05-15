package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetAllLikesResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: LikedData?
) : Parcelable {

    @Parcelize
    data class LikedData(
        @SerializedName("books")
        val books: List<BookRecommendation>?,
        @SerializedName("products")
        val products: List<SupplementItem>?,
        @SerializedName("devices")
        val devices: List<GetShopDevicesResponse.Device>?
    ) : Parcelable
}
