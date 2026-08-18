package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthProfileConfigResponse(
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
        @SerializedName("healthGoals")
        val healthGoals: List<HealthItem>? = null,
        @SerializedName("medicalConditions")
        val medicalConditions: List<HealthItem>? = null
    ) : Parcelable

    @Parcelize
    data class HealthItem(
        @SerializedName("_id")
        val id: String? = null,
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("slug")
        val slug: String? = null,
        @SerializedName("isSelected")
        val isSelected: Boolean? = null
    ) : Parcelable

    @Parcelize
    data class Preferences(
        @SerializedName("healthGoals")
        val healthGoals: List<String>? = null,
        @SerializedName("medicalConditions")
        val medicalConditions: List<String>? = null
    ) : Parcelable
}
