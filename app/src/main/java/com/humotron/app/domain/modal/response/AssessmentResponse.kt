package com.humotron.app.domain.modal.response

import com.google.gson.annotations.SerializedName

data class AssessmentResponse(
    val status: String,
    val message: String,
    val data: AssessmentData,
)

data class AssessmentData(
    @SerializedName("Assessment")
    val assessment: Assessment,
)

data class Assessment(
    val _id: String,
    val assessmentName: String,
    val alertCriteria: String? = null,
    val assessmentIntro: String,
    val assessmentWhat: String? = null,
    val assessmentWhy: String? = null,
    val assessmentNextSteps: String? = null,
    val assessmentDuration: String,
    val assessmentNoQuestions: String,
    val assessmentQuestions: List<Question>,
)

/*data class Question(
    val _id: String,
    val assessmentQuestionName: String,
    val assessmentQuestionNumber: Int,
    val assessmentQuestionPrompt: String,
    val options: List<Option>,
    val assessmentQuestionAnswer: List<String>
)*/
data class Question(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("assessmentQuestionName")
    val assessmentQuestionName: String,
    @SerializedName("assessmentQuestionNumber")
    val assessmentQuestionNumber: Int,
    @SerializedName("assessmentQuestionPrompt")
    val assessmentQuestionPrompt: String,
    @SerializedName("assessmentAnswerType")
    val assessmentAnswerType: String,
    @SerializedName("followUpQuestionId")
    val followUpQuestionId: String = "",
    @SerializedName("followUpToggle")
    val followUpToggle: String = "",
    @SerializedName("options")
    val options: List<Option> = emptyList(),
    @SerializedName("assessmentQuestionAnswer")
    val assessmentQuestionAnswer: List<String> = emptyList(),
    @SerializedName("assessmentQuestionFollowUpAnswer")
    val assessmentQuestionFollowUpAnswer: List<String> = emptyList(),
)

data class Option(
    val key: String,
    val value: String,
)
