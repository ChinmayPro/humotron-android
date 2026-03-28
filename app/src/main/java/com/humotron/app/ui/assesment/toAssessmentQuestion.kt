import com.humotron.app.domain.modal.response.Question
import com.humotron.app.ui.assesment.AssessmentQuestion
import com.humotron.app.ui.assesment.QuestionType

/*
package com.humotron.app.ui.assesment

import com.humotron.app.domain.modal.response.Question

fun Question.toAssessmentQuestion(uiId: Int): AssessmentQuestion {
    val optionLabels = options.map { it.value }

    val type: QuestionType = when {
        options.size == 2 &&
                options.any { it.key.lowercase() == "yes" } &&
                options.any { it.key.lowercase() == "no" } -> {
            QuestionType.YesNo(
                conditionalOptions = null,
                conditionalLabel = null
            )
        }
        assessmentQuestionAnswer.size > 1 -> {
            QuestionType.MultiSelect(options = optionLabels)
        }
        else -> QuestionType.RadioList(options = optionLabels)
    }

    return AssessmentQuestion(
        id = uiId,
        apiQuestionId = _id,         // ✅ API ka _id yahan set hoga
        questionText = assessmentQuestionName,
        helperText = assessmentQuestionPrompt,
        type = type
    )
}


*/

fun Question.toAssessmentQuestion(displayIndex: Int): AssessmentQuestion {
    val questionType: QuestionType = when (assessmentAnswerType.uppercase()) {

        "YESNO" -> {
            // If there's a followUp, fetch its options from the parent assessment
            // For now we pass null — wire up conditional options if needed
            QuestionType.YesNo(
                conditionalOptions = null,
                conditionalLabel = null
            )
        }

        "DROPDOWN" -> {
            // Filter out PLACEHOLDER option
            val filteredOptions = options
                .filter { it.key != "PLACEHOLDER" }
                .map { it.value }
            QuestionType.MultiSelect(options = filteredOptions)
        }

        "MULTIOPTIONS" -> {
            QuestionType.RadioList(options = options.map { it.value })
        }

        else -> QuestionType.RadioList(options = options.map { it.value })
    }

    return AssessmentQuestion(
        id = displayIndex,
        apiQuestionId = _id,
        questionText = assessmentQuestionName,
        helperText = assessmentQuestionPrompt,
        type = questionType
    )
}