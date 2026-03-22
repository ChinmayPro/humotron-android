package com.humotron.app.ui.assesment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.core.AssesmentTempId
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.AssessmentRepository
import com.humotron.app.domain.modal.response.AnswerItem
import com.humotron.app.domain.modal.response.Assessment
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


    fun loadAssessment() {
        viewModelScope.launch {
            repository.getAssessment(
                id = AssesmentTempId,
                token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmaXJzdE5hbWUiOiJDaGlubWF5IiwibGFzdE5hbWUiOiJCaGF0dCIsImVtYWlsIjoiY2hpbm1heS5iaGF0dEBnbWFpbC5jb20iLCJjb250YWN0Tm8iOiI5ODg5ODk4OTg5IiwidXNlclR5cGUiOiJVU0VSIiwidXNlcklkIjoiNjVjMTcwYzg3N2I5NWM3OGNkM2ZhMmJjIiwiaWF0IjoxNzcxODM5OTgyfQ.Uocg15IrdDB_Z1wz7VAkHhiAxJhmlTao6Gtgu03mtTc"
            ).collect { resource ->
                when (resource.status) {
                    Status.LOADING -> _isLoading.value = true

                    Status.SUCCESS -> {
                        _isLoading.value = false
                        val data = resource.data?.data?.Assessment
                        data?.let {
                            val sorted = it.assessmentQuestions
                                .sortedBy { q -> q.assessmentQuestionNumber }

                            _assessment.value = it.copy(assessmentQuestions = sorted)

                            // Map API questions → AssessmentQuestion list
                            val mappedQuestions = sorted.mapIndexed { index, q ->
                                q.toAssessmentQuestion(index + 1)
                            }
                            _questions.value = mappedQuestions

                            // ✅ Restore pre-existing answers from API
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

                            // Reset index & notify UI
                            _currentIndex.value = 0
                            _questionsReady.value = true
                        }
                    }

                    Status.ERROR     -> _isLoading.value = false
                    Status.EXCEPTION -> _isLoading.value = false
                }
            }
        }
    }
    fun submitAllAnswers() {
        val assessmentId = AssesmentTempId
        val followUpQuestionId = _assessment.value?.assessmentQuestions
            ?.find { it.followUpQuestionId.isNotEmpty() }
            ?.followUpQuestionId

        val answerItems = answers.mapNotNull { (questionId, answer) ->
            val question = _questions.value?.find { it.id == questionId }
                ?: return@mapNotNull null

            // Answer strings build karo
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
                // Follow-up question
                AnswerItem(
                    assessmentId = assessmentId,
                    assessmentQuestionFollowUpId = question.apiQuestionId,
                    assessmentQuestionFollowUpAnswer = answerStrings,
                    shouldSave = true
                )
            } else {
                // Normal question
                AnswerItem(
                    assessmentId = assessmentId,
                    assessmentQuestionId = question.apiQuestionId,
                    assessmentQuestionAnswer = answerStrings,
                    shouldSave = true
                )
            }
        }

        viewModelScope.launch {
            repository.submitAnswers(
                token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmaXJzdE5hbWUiOiJDaGlubWF5IiwibGFzdE5hbWUiOiJCaGF0dCIsImVtYWlsIjoiY2hpbm1heS5iaGF0dEBnbWFpbC5jb20iLCJjb250YWN0Tm8iOiI5ODg5ODk4OTg5IiwidXNlclR5cGUiOiJVU0VSIiwidXNlcklkIjoiNjVjMTcwYzg3N2I5NWM3OGNkM2ZhMmJjIiwiaWF0IjoxNzcxODM5OTgyfQ.Uocg15IrdDB_Z1wz7VAkHhiAxJhmlTao6Gtgu03mtTc",
                request = SubmitAnswerRequest(data = answerItems)
            ).collect { resource ->
                when (resource.status) {
                    Status.LOADING   -> _isLoading.value = true
                    Status.SUCCESS   -> {
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
    fun submitAllAnswers00() {
        val assessmentId = AssesmentTempId

        val answerItems = answers.mapNotNull { (questionId, answer) ->
            val question = _questions.value?.find { it.id == questionId } ?: return@mapNotNull null
            val answerStrings: List<String> = when {
                answer.selectedItems.isNotEmpty() -> answer.selectedItems

                answer.selectedIndex != null -> {
                    val options = when (val type = question.type) {
                        is QuestionType.RadioList -> type.options
                        is QuestionType.YesNo -> listOf("No", "Yes")
                        is QuestionType.MultiSelect -> type.options
                    }
                    listOf(options.getOrElse(answer.selectedIndex) { "" })
                }

                else -> return@mapNotNull null
            }
            AnswerItem(
                assessmentId = assessmentId,
                assessmentQuestionId = question.apiQuestionId,
                assessmentQuestionAnswer = answerStrings,
                shouldSave = true
            )
        }

        viewModelScope.launch {
            repository.submitAnswers(
                token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmaXJzdE5hbWUiOiJDaGlubWF5IiwibGFzdE5hbWUiOiJCaGF0dCIsImVtYWlsIjoiY2hpbm1heS5iaGF0dEBnbWFpbC5jb20iLCJjb250YWN0Tm8iOiI5ODg5ODk4OTg5IiwidXNlclR5cGUiOiJVU0VSIiwidXNlcklkIjoiNjVjMTcwYzg3N2I5NWM3OGNkM2ZhMmJjIiwiaWF0IjoxNzcxODM5OTgyfQ.Uocg15IrdDB_Z1wz7VAkHhiAxJhmlTao6Gtgu03mtTc",
                request = SubmitAnswerRequest(data = answerItems)
            ).collect { resource ->

                when (resource.status) {
                    Status.LOADING -> _isLoading.value = true

                    Status.SUCCESS -> {
                        _isLoading.value = false
                        _submitSuccess.value = true
                        _errorMessage.value = resource.data?.message

                    }

                    Status.ERROR -> {
                        _isLoading.value = false
                    }

                    Status.EXCEPTION -> {
                        _isLoading.value = false
                    }
                }
            }
        }
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

    fun getAllAnswers(): Map<Int, AssessmentAnswer> = answers.toMap()
}