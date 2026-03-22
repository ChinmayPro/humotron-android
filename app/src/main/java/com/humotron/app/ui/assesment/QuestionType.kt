package com.humotron.app.ui.assesment

// ── Question types ──────────────────────────────────────────────
sealed class QuestionType {

    /** Single-choice radio list */
    data class RadioList(val options: List<String>) : QuestionType()

    /** Yes / No toggle buttons.
     *  If [conditionalOptions] is non-null, a radio list appears when user picks "Yes". */
    data class YesNo(
        val conditionalOptions: List<String>? = null,
        val conditionalLabel: String? = null
    ) : QuestionType()

    /** Multi-select via bottom-sheet picker */
    data class MultiSelect(val options: List<String>) : QuestionType()
}

// ── Single question model ───────────────────────────────────────
data class AssessmentQuestion(
    val id: Int,
    val apiQuestionId: String,   // ✅ Yeh add karo
    val questionText: String,
    val helperText: String,
    val type: QuestionType
)

// ── Answer state ────────────────────────────────────────────────
data class AssessmentAnswer(
    val questionId: Int,
    val selectedIndex: Int? = null,       // for RadioList / YesNo
    val selectedItems: List<String> = emptyList() // for MultiSelect
)
