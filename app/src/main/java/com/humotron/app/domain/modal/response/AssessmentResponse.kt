package com.humotron.app.domain.modal.response

data class AssessmentResponse(
    val status: String,
    val message: String,
    val data: AssessmentData,
)

data class AssessmentData(
    val assessment: Assessment,
)

data class Assessment(
    val _id: String,
    val assessmentName: String,
    val assessmentIntro: String,
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
    val _id: String,
    val assessmentQuestionName: String,
    val assessmentQuestionNumber: Int,
    val assessmentQuestionPrompt: String,
    val assessmentAnswerType: String,  // ← ADD THIS
    val followUpQuestionId: String = "",
    val followUpToggle: String = "",
    val options: List<Option> = emptyList(),
    val assessmentQuestionAnswer: List<String> = emptyList(),
    val assessmentQuestionFollowUpAnswer: List<String> = emptyList(),
)

data class Option(
    val key: String,
    val value: String,
)
