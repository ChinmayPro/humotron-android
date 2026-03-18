package com.humotron.app.ui.assesment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AssessmentViewModel : ViewModel() {

    val questions: List<AssessmentQuestion> = AssessmentRepository.questions
    val totalQuestions get() = questions.size

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val answers = mutableMapOf<Int, AssessmentAnswer>()

    val currentQuestion get() = questions[_currentIndex.value ?: 0]

    fun getCurrentAnswer(): AssessmentAnswer? = answers[currentQuestion.id]

    fun saveAnswer(answer: AssessmentAnswer) {
        answers[answer.questionId] = answer
    }

    fun canGoNext(): Boolean {
        val answer = answers[currentQuestion.id] ?: return false
        return answer.selectedIndex != null || answer.selectedItems.isNotEmpty()
    }

    fun goNext(): Boolean {
        val idx = _currentIndex.value ?: return false
        return if (idx < questions.size - 1) {
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

    fun isLastQuestion() = (_currentIndex.value ?: 0) == questions.size - 1
    fun isFirstQuestion() = (_currentIndex.value ?: 0) == 0

    fun getAllAnswers(): Map<Int, AssessmentAnswer> = answers.toMap()
}