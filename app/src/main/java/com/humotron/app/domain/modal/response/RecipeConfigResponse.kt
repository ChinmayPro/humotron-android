package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeConfigResponse(
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
        @SerializedName("complexity")
        val complexity: List<ComplexityItem>? = null,
        @SerializedName("cookingTime")
        val cookingTime: List<String>? = null,
        @SerializedName("dietaryPreference")
        val dietaryPreference: List<String>? = null,
        @SerializedName("excludeIngredients")
        val excludeIngredients: List<String>? = null,
        @SerializedName("cuisine")
        val cuisine: List<String>? = null,
        @SerializedName("taste")
        val taste: List<String>? = null
    ) : Parcelable

    @Parcelize
    data class ComplexityItem(
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("subtitle")
        val subtitle: String? = null
    ) : Parcelable

    @Parcelize
    data class Preferences(
        @SerializedName("complexity")
        val complexity: List<String>? = null,
        @SerializedName("cookingTime")
        val cookingTime: List<String>? = null,
        @SerializedName("dietaryPreference")
        val dietaryPreference: List<String>? = null,
        @SerializedName("excludeIngredients")
        val excludeIngredients: List<String>? = null,
        @SerializedName("cuisine")
        val cuisine: List<String>? = null,
        @SerializedName("taste")
        val taste: List<String>? = null
    ) : Parcelable
}
