package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class InsightConfigResponse(
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
        @SerializedName("insightStyle")
        val insightStyle: List<StyleItem>? = null,
        @SerializedName("frequency")
        val frequency: List<LabelValueItem>? = null,
        @SerializedName("focusAreas")
        val focusAreas: List<String>? = null,
        @SerializedName("triggers")
        val triggers: List<LabelValueItem>? = null
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
    data class LabelValueItem(
        @SerializedName("label")
        val label: String? = null,
        @SerializedName("value")
        val value: String? = null
    ) : Parcelable

    @Parcelize
    data class Preferences(
        @SerializedName("insightStyle")
        val insightStyleRaw: @RawValue Any? = null,
        @SerializedName("frequency")
        val frequencyRaw: @RawValue Any? = null,
        @SerializedName("focusAreas")
        val focusAreas: List<String>? = null,
        @SerializedName("triggers")
        val triggers: String? = null
    ) : Parcelable {

        fun getSelectedStyleTitle(): String {
            return when (insightStyleRaw) {
                is Map<*, *> -> (insightStyleRaw["title"] as? String) ?: (insightStyleRaw["value"] as? String) ?: "Simple"
                is String -> insightStyleRaw
                else -> "Simple"
            }
        }

        fun getSelectedFrequencyValue(): String {
            return when (frequencyRaw) {
                is Double -> frequencyRaw.toInt().toString()
                is Float -> frequencyRaw.toInt().toString()
                is Int -> frequencyRaw.toString()
                is String -> frequencyRaw
                else -> "7"
            }
        }
    }
}
