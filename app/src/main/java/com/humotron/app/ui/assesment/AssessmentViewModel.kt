
package com.humotron.app.ui.assesment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.AssessmentRepository
import com.humotron.app.domain.modal.response.AnswerItem
import com.humotron.app.domain.modal.response.Assessment
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.domain.modal.response.SubmitAnswerRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import toAssessmentQuestion
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val repository: AssessmentRepository,
) : ViewModel() {

    private val _questions = MutableLiveData<List<AssessmentQuestion>>(emptyList())
    private val _questionsReady = MutableLiveData(false)
    val questionsReady: LiveData<Boolean> = _questionsReady

    val currentQuestion get() = _questions.value?.getOrNull(_currentIndex.value ?: 0)
    val totalQuestions get() = _questions.value?.size ?: 0

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val answers = mutableMapOf<Int, AssessmentAnswer>()

    private val _assessment = MutableLiveData<Assessment?>()
    val assessment: LiveData<Assessment?> = _assessment

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _submitSuccess = MutableLiveData(false)
    val submitSuccess: LiveData<Boolean> = _submitSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentAssessmentId: String = ""
    private var currentAuthToken: String = ""

    fun loadAssessment(assessmentId: String, authToken: String) {
        currentAssessmentId = assessmentId
        currentAuthToken = authToken

        viewModelScope.launch {
            repository.getAssessment(
                id = assessmentId,
                token = "Bearer $authToken"
            ).collect { resource ->
                when (resource.status) {
                    Status.LOADING -> _isLoading.value = true

                    Status.SUCCESS -> {
                        _isLoading.value = false
                        val data = resource.data?.data?.assessment
                        data?.let {
                            val sorted = it.assessmentQuestions
                                .sortedBy { q -> q.assessmentQuestionNumber }

                            _assessment.value = it.copy(assessmentQuestions = sorted)

                            val mappedQuestions = sorted.mapIndexed { index, q ->
                                q.toAssessmentQuestion(index + 1)
                            }
                            _questions.value = mappedQuestions

                            // API se existing answers restore karo
                            sorted.forEachIndexed { index, q ->
                                val questionId = index + 1

                                when (q.assessmentAnswerType.uppercase()) {

                                    "MULTIOPTIONS" -> {
                                        val existingAnswer = q.assessmentQuestionAnswer.firstOrNull()
                                            ?: return@forEachIndexed
                                        val options = q.options.map { it.value }
                                        val selectedIndex = options.indexOf(existingAnswer)
                                        if (selectedIndex >= 0) {
                                            answers[questionId] = AssessmentAnswer(
                                                questionId = questionId,
                                                selectedIndex = selectedIndex
                                            )
                                        }
                                    }

                                    "YESNO" -> {
                                        val existingAnswer = q.assessmentQuestionAnswer.firstOrNull()
                                            ?: return@forEachIndexed
                                        val selectedIndex = when (existingAnswer.lowercase()) {
                                            "yes" -> 1
                                            "no"  -> 0
                                            else  -> return@forEachIndexed
                                        }
                                        answers[questionId] = AssessmentAnswer(
                                            questionId = questionId,
                                            selectedIndex = selectedIndex
                                        )
                                    }

                                    "DROPDOWN" -> {
                                        val existingAnswers = q.assessmentQuestionAnswer
                                        if (existingAnswers.isEmpty()) return@forEachIndexed
                                        answers[questionId] = AssessmentAnswer(
                                            questionId = questionId,
                                            selectedItems = existingAnswers
                                        )
                                    }
                                }
                            }

                            val startIndex = getFirstUnansweredIndex()
                            _currentIndex.value = startIndex
                            _questionsReady.value = true
                        }
                    }

                    Status.ERROR     -> _isLoading.value = false
                    Status.EXCEPTION -> _isLoading.value = false
                }
            }
        }
    }

    // ✅ Single answer silently API ko bhejo — Next/Save click pe call hoga
    fun saveAnswerToApi(answer: AssessmentAnswer, mergedAssessment: MergedAssessment?) {
        val question = _questions.value?.find { it.id == answer.questionId } ?: return

        val followUpQuestionId = _assessment.value?.assessmentQuestions
            ?.find { it.followUpQuestionId.isNotEmpty() }
            ?.followUpQuestionId

        val answerStrings: List<String> = when {
            answer.selectedItems.isNotEmpty() -> answer.selectedItems

            answer.selectedIndex != null -> {
                when (val type = question.type) {
                    is QuestionType.YesNo ->
                        if (answer.selectedIndex == 1) listOf("Yes") else listOf("No")
                    is QuestionType.RadioList ->
                        listOf(type.options.getOrElse(answer.selectedIndex) { "" })
                    is QuestionType.MultiSelect ->
                        listOf(type.options.getOrElse(answer.selectedIndex) { "" })
                }
            }

            else -> return
        }

        val isFollowUp = followUpQuestionId != null &&
                question.apiQuestionId == followUpQuestionId

        val answerItem = if (isFollowUp) {
            AnswerItem(
                assessmentId = mergedAssessment?.assessmentId ?: "",
                assessmentQuestionFollowUpId = question.apiQuestionId,
                assessmentQuestionFollowUpAnswer = answerStrings,
                shouldSave = true
            )
        } else {
            AnswerItem(
                assessmentId = mergedAssessment?.assessmentId ?: "",
                assessmentQuestionId = question.apiQuestionId,
                assessmentQuestionAnswer = answerStrings,
                shouldSave = true
            )
        }

        // Silent fire-and-forget — failure pe kuch nahi karega
        viewModelScope.launch {
            try {
                repository.submitAnswers(
                    token = "Bearer $currentAuthToken",
                    request = SubmitAnswerRequest(data = listOf(answerItem))
                ).collect { resource ->
                    Log.d("TAG", "saveAnswerToApi: ${resource.status} for questionId=${answer.questionId}")
                }
            } catch (e: Exception) {
                Log.e("TAG", "saveAnswerToApi: silently failed", e)
            }
        }
    }

    // ✅ Final Save button pe — sabhi answers ek saath bhejo (safety net)
    fun submitAllAnswers(mergedAssessment: MergedAssessment?, authToken: String?) {
        val followUpQuestionId = _assessment.value?.assessmentQuestions
            ?.find { it.followUpQuestionId.isNotEmpty() }
            ?.followUpQuestionId

        val answerItems = answers.mapNotNull { (questionId, answer) ->
            val question = _questions.value?.find { it.id == questionId }
                ?: return@mapNotNull null

            val answerStrings: List<String> = when {
                answer.selectedItems.isNotEmpty() -> answer.selectedItems

                answer.selectedIndex != null -> {
                    when (val type = question.type) {
                        is QuestionType.YesNo ->
                            if (answer.selectedIndex == 1) listOf("Yes") else listOf("No")
                        is QuestionType.RadioList ->
                            listOf(type.options.getOrElse(answer.selectedIndex) { "" })
                        is QuestionType.MultiSelect ->
                            listOf(type.options.getOrElse(answer.selectedIndex) { "" })
                    }
                }

                else -> return@mapNotNull null
            }

            val isFollowUp = followUpQuestionId != null &&
                    question.apiQuestionId == followUpQuestionId

            if (isFollowUp) {
                AnswerItem(
                    assessmentId = mergedAssessment?.assessmentId ?: "",
                    assessmentQuestionFollowUpId = question.apiQuestionId,
                    assessmentQuestionFollowUpAnswer = answerStrings,
                    shouldSave = true
                )
            } else {
                AnswerItem(
                    assessmentId = mergedAssessment?.assessmentId ?: "",
                    assessmentQuestionId = question.apiQuestionId,
                    assessmentQuestionAnswer = answerStrings,
                    shouldSave = true
                )
            }
        }

        viewModelScope.launch {
            Log.e("TAG", "submitAllAnswers: $answerItems")
            repository.submitAnswers(
                token = "Bearer $authToken",
                request = SubmitAnswerRequest(data = answerItems)
            ).collect { resource ->
                when (resource.status) {
                    Status.LOADING -> _isLoading.value = true
                    Status.SUCCESS -> {
                        _isLoading.value = false
                        _submitSuccess.value = true
                        _errorMessage.value = resource.data?.message
                    }
                    Status.ERROR     -> _isLoading.value = false
                    Status.EXCEPTION -> _isLoading.value = false
                }
            }
        }
    }

    fun getFirstUnansweredIndex(): Int {
        val questions = _questions.value ?: return 0
        if (questions.isEmpty()) return 0
        val firstUnanswered = questions.indexOfFirst { q -> answers[q.id] == null }
        return when {
            firstUnanswered >= 0 -> firstUnanswered
            else -> questions.size - 1
        }
    }

    fun jumpToIndex(index: Int) {
        val safeIndex = index.coerceIn(0, (totalQuestions - 1).coerceAtLeast(0))
        _currentIndex.value = safeIndex
    }

    fun getCurrentAnswer(): AssessmentAnswer? = answers[currentQuestion!!.id]

    fun saveAnswer(answer: AssessmentAnswer) {
        answers[answer.questionId] = answer
    }

    fun canGoNext(): Boolean {
        val answer = answers[currentQuestion!!.id] ?: return false
        return answer.selectedIndex != null || answer.selectedItems.isNotEmpty()
    }

    fun goNext(): Boolean {
        val idx = _currentIndex.value ?: return false
        return if (idx < totalQuestions - 1) {
            _currentIndex.value = idx + 1
            true
        } else false
    }

    fun goPrevious(): Boolean {
        val idx = _currentIndex.value ?: return false
        return if (idx > 0) {
            _currentIndex.value = idx - 1
            true
        } else false
    }

    fun isLastQuestion() = (_currentIndex.value ?: 0) == totalQuestions - 1
    fun isFirstQuestion() = (_currentIndex.value ?: 0) == 0
}