package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BoosterResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("booster")
        val booster: List<Booster>?,
        @SerializedName("totalRecords")
        val totalRecords: Int?
    ) : Parcelable

    @Parcelize
    data class Booster(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("iosProductId")
        val iosProductId: String?,
        @SerializedName("androidProductId")
        val androidProductId: String?,
        @SerializedName("price")
        val price: Double?,
        @SerializedName("boosterName")
        val name: String?,
        @SerializedName("tagline")
        val tagline: String?,
        @SerializedName("boosterId")
        val boosterId: String?,
        @SerializedName("priceModel")
        val priceModel: String?,
        @SerializedName("validity")
        val validity: Int?,
        @SerializedName("isSubscribed")
        val isSubscribed: Boolean?,
        @SerializedName("ctaCopy")
        val ctaCopy: String?,
        @SerializedName("imageUrl")
        val imageUrl: String?,
        @SerializedName("whatUnlock")
        val whatUnlock: List<String>?,
        @SerializedName("heroCopy")
        val heroCopy: String?,
        @SerializedName("bbcTitle")
        val bbcTitle: String?,
        @SerializedName("bbcDescription")
        val bbcDescription: String?
    ) : Parcelable {
        val displayName: String get() = name ?: ""
        val displayDescription: String get() = tagline ?: ""
        
        // Return a clean fallback price format (e.g. $7.90)
        val displayPriceFallback: String get() = if (price != null) {
            val formatted = String.format("%.2f", price)
            if (formatted.endsWith(".00")) {
                "$" + String.format("%.0f", price)
            } else {
                "$$formatted"
            }
        } else {
            "Free"
        }

        // Play Store product ID matches boosterId from backend API
        val playStoreProductId: String get() =
            androidProductId.takeIf { !it.isNullOrEmpty() } ?: boosterId ?: ""
    }
}
