package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MergedAssessment(
    @SerializedName("assessmentId")
    val assessmentId: String,
    @SerializedName("assessmentName")
    val assessmentName: String,
    @SerializedName("totalQuestions")
    val totalQuestions: Int,
    @SerializedName("answeredCount")
    val answeredCount: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("action")
    val action: String,
    @SerializedName("assessmentDetails")
    val assessmentDetails: AssessmentDetails?,
    @SerializedName("hasUnansweredFollowUps")
    val hasUnansweredFollowUps: Boolean,
    @SerializedName("pendingFollowUps")
    val pendingFollowUps: Boolean
) : Parcelable

@Parcelize
data class AssessmentDetails(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("assessmentName")
    val assessmentName: String?,
    @SerializedName("alertCriteria")
    val alertCriteria: String?,
    @SerializedName("assessmentIntro")
    val assessmentIntro: String?,
    @SerializedName("assessmentWhat")
    val assessmentWhat: String?,
    @SerializedName("assessmentWhy")
    val assessmentWhy: String?,
    @SerializedName("assessmentNextSteps")
    val assessmentNextSteps: String?,
    @SerializedName("assessmentDuration")
    val assessmentDuration: String?,
    @SerializedName("assessmentNoQuestions")
    val assessmentNoQuestions: String?,
    @SerializedName("assessmentQuestionId")
    val assessmentQuestionId: List<String>?,
    @SerializedName("isDeleted")
    val isDeleted: Boolean?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
) : Parcelable