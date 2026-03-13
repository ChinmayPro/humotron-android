package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UseCaseResponse(
    @SerializedName("data")
    val data: UseCaseData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable

@Parcelize
data class UseCaseData(
    @SerializedName("interests")
    val interests: List<UseCaseInterest>?,
    @SerializedName("totalRecords")
    val totalRecords: Int?
) : Parcelable

@Parcelize
data class UseCaseInterest(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("interestCount")
    val interestCount: Int?,
    @SerializedName("interestQuestionId")
    val interestQuestionId: List<String>?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("title")
    val title: String?,
    var isChecked: Boolean = false
) : Parcelable

