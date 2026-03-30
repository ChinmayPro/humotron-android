package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PromptContextResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: PromptContextData? = null
) : Parcelable

@Parcelize
data class PromptContextData(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("conversationThreadId")
    val conversationThreadId: String? = null,
    @SerializedName("promptId")
    val promptId: String? = null,
    @SerializedName("promptTitle")
    val promptTitle: String? = null,
    @SerializedName("metricName")
    val metricName: List<String>? = null,
    @SerializedName("promptContext")
    val promptContext: PromptContext? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
) : Parcelable

@Parcelize
data class PromptContext(
    @SerializedName("demographics")
    val demographics: Demographics? = null,
    @SerializedName("metrics")
    val metrics: Metrics? = null,
    @SerializedName("assessment")
    val assessment: AssessmentChat? = null
) : Parcelable

@Parcelize
data class Demographics(
    @SerializedName("available")
    val available: Boolean? = null,
    @SerializedName("age")
    val age: Int? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("height")
    val height: String? = null,
    @SerializedName("heightUnit")
    val heightUnit: String? = null,
    @SerializedName("weight")
    val weight: String? = null,
    @SerializedName("weightUnit")
    val weightUnit: String? = null,
    @SerializedName("bmi")
    val bmi: Double? = null
) : Parcelable

@Parcelize
data class Metrics(
    @SerializedName("available")
    val available: Boolean? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("dateRange")
    val dateRange: DateRange? = null
) : Parcelable

@Parcelize
data class DateRange(
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null
) : Parcelable

@Parcelize
data class Assessment(
    @SerializedName("available")
    val available: Boolean? = null,
    @SerializedName("items")
    val items: List<AssessmentItem>? = null
) : Parcelable

@Parcelize
data class AssessmentItem(
    @SerializedName("status")
    val status: Boolean? = null,
    @SerializedName("assessmentQuestionName")
    val assessmentQuestionName: String? = null,
    @SerializedName("assessmentQuestionAnswer")
    val assessmentQuestionAnswer: List<String>? = null,
    @SerializedName("assessmentQuestionId")
    val assessmentQuestionId: String? = null
) : Parcelable
