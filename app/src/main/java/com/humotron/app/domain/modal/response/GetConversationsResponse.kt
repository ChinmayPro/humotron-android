package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetConversationsResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<ConversationData>? = null,
    @SerializedName("totalRecords")
    val totalRecords: Int? = null
) : Parcelable





@Parcelize
data class ConversationData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("userMessage")
    val userMessage: String? = null,
    @SerializedName("botResponse")
    val botResponse: BotResponse? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("conversationThreadId")
    val conversationThreadId: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("booster_ai_chat")
    val boosterAiChat: BoosterAiChat? = null,
    val isNewMessage: Boolean = false
) : Parcelable


@Parcelize
data class BotResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("isBoosterActive")
    val isBoosterActive: Boolean? = null
) : Parcelable


@Parcelize
data class BoosterAiChat(
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    @SerializedName("expiredAt")
    val expiredAt: String? = null,
    @SerializedName("boosterOriginId")
    val boosterOriginId: String? = null,
    @SerializedName("boosterName")
    val boosterName: String? = null,
    @SerializedName("boosterHeroCopy")
    val boosterHeroCopy: String? = null,
    @SerializedName("boosterPrice")
    val boosterPrice: Double? = null,
    @SerializedName("iosProductId")
    val iosProductId: String? = null,
    @SerializedName("androidProductId")
    val androidProductId: String? = null
) : Parcelable {
    fun toBooster(): BoosterResponse.Booster {
        return BoosterResponse.Booster(
            id = boosterOriginId,
            iosProductId = iosProductId,
            androidProductId = androidProductId,
            price = boosterPrice,
            name = boosterName,
            tagline = boosterHeroCopy,
            boosterId = boosterOriginId,
            priceModel = "monthly",
            validity = 30,
            isSubscribed = isActive,
            ctaCopy = null,
            imageUrl = null,
            whatUnlock = null,
            heroCopy = boosterHeroCopy,
            bbcTitle = null,
            bbcDescription = null
        )
    }
}
