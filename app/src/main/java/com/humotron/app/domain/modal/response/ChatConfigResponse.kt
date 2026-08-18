package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class ChatConfigResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Data? = null
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("configuration")
        val configuration: Configuration? = null,
        @SerializedName("preferences")
        val preferences: Preferences? = null
    ) : Parcelable

    @Parcelize
    data class Configuration(
        @SerializedName("responseStyle")
        val responseStyle: List<StyleItem>? = null,
        @SerializedName("focus")
        val focus: List<StyleItem>? = null,
        @SerializedName("context")
        val context: List<ContextItem>? = null,
        @SerializedName("chatHistory")
        val chatHistory: List<LabelValueItem>? = null
    ) : Parcelable

    @Parcelize
    data class StyleItem(
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("subtitle")
        val subtitle: String? = null,
        @SerializedName("value")
        val value: String? = null
    ) : Parcelable

    @Parcelize
    data class ContextItem(
        @SerializedName("key")
        val key: String? = null,
        @SerializedName("label")
        val label: String? = null
    ) : Parcelable

    @Parcelize
    data class LabelValueItem(
        @SerializedName("label")
        val label: String? = null,
        @SerializedName("value")
        val value: @RawValue Any? = null
    ) : Parcelable

    @Parcelize
    data class Preferences(
        @SerializedName("chatHistoryDays")
        val chatHistoryDaysRaw: @RawValue Any? = null,
        @SerializedName("focus")
        val focusRaw: @RawValue Any? = null,
        @SerializedName("context")
        val context: ContextPreference? = null,
        @SerializedName("responseStyle")
        val responseStyleRaw: @RawValue Any? = null
    ) : Parcelable {

        fun getSelectedResponseStyle(): String {
            return when (responseStyleRaw) {
                is Map<*, *> -> (responseStyleRaw["title"] as? String) ?: (responseStyleRaw["value"] as? String) ?: "Deep"
                is String -> responseStyleRaw
                else -> "Deep"
            }
        }

        fun getSelectedFocus(): String {
            return when (focusRaw) {
                is Map<*, *> -> (focusRaw["title"] as? String) ?: (focusRaw["value"] as? String) ?: "Understanding"
                is String -> focusRaw
                else -> "Understanding"
            }
        }

        fun getSelectedChatHistoryDays(): Int {
            return when (chatHistoryDaysRaw) {
                is Double -> chatHistoryDaysRaw.toInt()
                is Float -> chatHistoryDaysRaw.toInt()
                is Int -> chatHistoryDaysRaw
                is String -> chatHistoryDaysRaw.toIntOrNull() ?: 90
                else -> 90
            }
        }
    }

    @Parcelize
    data class ContextPreference(
        @SerializedName("guidance")
        val guidance: Boolean? = true,
        @SerializedName("both")
        val both: Boolean? = false
    ) : Parcelable
}
