package com.humotron.app.domain.modal.response


data class SubmitAnswerRequest(
    val data: List<AnswerItem>
)

/*
data class AnswerItem(
    val assessmentQuestionAnswer: List<String>,
    val shouldSave: Boolean = true,
    val assessmentId: String,
    val assessmentQuestionId: String
)
*/

data class AnswerItem(
    val assessmentId: String,

    // Normal question
    val assessmentQuestionId: String? = null,
    val assessmentQuestionAnswer: List<String>? = null,

    // Follow-up question
    val assessmentQuestionFollowUpId: String? = null,
    val assessmentQuestionFollowUpAnswer: List<String>? = null,

    val shouldSave: Boolean = true
)

data class SubmitAnswerResponse(
    val status: String,
    val message: String
)