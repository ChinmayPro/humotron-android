package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeConfigRequest(
    @SerializedName("preferences")
    val preferences: Preferences? = null
) : Parcelable {

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
