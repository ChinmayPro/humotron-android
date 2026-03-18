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

// ── Full question bank ──────────────────────────────────────────
object AssessmentRepository {

    val questions = listOf(
        AssessmentQuestion(
            id = 1,
            questionText = "How often do you engage in moderate to vigorous physical activity each week?",
            helperText = "Consider activities that increase your heart rate and make you breathe harder, such as brisk walking, cycling, swimming, or structured exercise. Estimate how often you participate in these activities per week.",
            type = QuestionType.RadioList(
                listOf(
                    "Daily (Almost every day)",
                    "Regularly (3-5 days per week)",
                    "Occasionally (1-2 days per week)",
                    "Rarely or never"
                )
            )
        ),
        AssessmentQuestion(
            id = 2,
            questionText = "Have you ever been diagnosed with any of the following cardiovascular conditions?",
            helperText = "Review any past medical diagnoses related to heart health as they play a crucial role in managing your overall cardiovascular risk.",
            type = QuestionType.YesNo(
                conditionalLabel = "If yes, select your heart conditions",
                conditionalOptions = listOf(
                    "Coronary Artery Disease (CAD)",
                    "Myocardial Infarction (Heart Attack)",
                    "Hypertension (High Blood Pressure)",
                    "Heart Failure",
                    "Atrial Fibrillation",
                    "Stroke or TIA"
                )
            )
        ),
        AssessmentQuestion(
            id = 3,
            questionText = "Do you eat meat?",
            helperText = "Think about your diet over the past few months. Do you include any meat in your meals?",
            type = QuestionType.YesNo(
                conditionalLabel = "Evaluate the frequency of sleep disturbances such as difficulty falling asleep, staying asleep, or waking up too early.",
                conditionalOptions = listOf(
                    "Rarely",
                    "Sometimes",
                    "Often",
                    "Almost every night"
                )
            )
        ),
        AssessmentQuestion(
            id = 4,
            questionText = "How many pints of alcohol do you consume on average per week?",
            helperText = "Think about your weekly alcohol intake. Measuring it in pints can help assess its impact on your heart health.",
            type = QuestionType.RadioList(
                listOf(
                    "0 pints (I do not drink)",
                    "1-3 pints (Light drinking)",
                    "4-7 pints (Moderate drinking)",
                    "More than 7 pints (Heavy drinking)"
                )
            )
        ),
        AssessmentQuestion(
            id = 5,
            questionText = "How frequently do you experience headaches or migraines?",
            helperText = "Track the occurrence of headaches or migraines, noting their severity and the circumstances under which they occur.",
            type = QuestionType.MultiSelect(
                listOf("Rarely", "Monthly", "Weekly", "Daily")
            )
        )
    )
}