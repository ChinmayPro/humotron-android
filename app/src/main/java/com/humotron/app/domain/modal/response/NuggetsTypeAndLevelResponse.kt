package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NuggetsTypeAndLevelResponse(
    @SerializedName("data")
    val nuggetsData: List<NuggetsTypeLevelData>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable

@Parcelize
data class NuggetsTypeLevelData(
    @SerializedName("tagLevel")
    val tagLevel: String?,
    @SerializedName("tagType")
    val tagType: String?,
    @SerializedName("tags")
    val tags: List<Tag>?
) : Parcelable

@Parcelize
data class Tag(
    @SerializedName("id")
    val id: String?,
    @SerializedName("isSelected")
    val isSelected: Boolean?,
    @SerializedName("primaryTag")
    val primaryTag: List<PrimaryTagLevel>?,
    @SerializedName("tagDescription")
    val tagDescription: String?,
    @SerializedName("tagName")
    val tagName: String?,
    var isChecked: Boolean = false
) : Parcelable

@Parcelize
data class PrimaryTagLevel(
    @SerializedName("id")
    val id: String?,
    @SerializedName("isSelected")
    val isSelected: Boolean?,
    @SerializedName("tagDescription")
    val tagDescription: String?,
    @SerializedName("tagName")
    val tagName: String?,
    var isChecked: Boolean = false
) : Parcelable
