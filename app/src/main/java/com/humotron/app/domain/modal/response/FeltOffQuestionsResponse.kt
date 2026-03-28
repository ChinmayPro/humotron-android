package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeltOffQuestionsResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: FeltOffQuestionListData? = null
) : Parcelable

@Parcelize
data class FeltOffQuestionListData(
    @SerializedName("questions")
    val questions: List<FeltOffQuestionData>? = null
) : Parcelable

@Parcelize
data class FeltOffQuestionData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("questionText")
    val question: String? = null
) : Parcelable
