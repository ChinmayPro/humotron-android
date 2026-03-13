package com.humotron.app.domain.modal.param


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateNuggetPrefParam(
    @SerializedName("selectedTags")
    val selectedTags: List<SelectedTag>?
) : Parcelable

@Parcelize
data class SelectedTag(
    @SerializedName("selectedItems")
    val selectedItems: List<String>?,
    @SerializedName("tagLevel")
    val tagLevel: String?,
    @SerializedName("tagType")
    val tagType: String?
) : Parcelable
