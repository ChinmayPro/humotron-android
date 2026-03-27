package com.humotron.app.ui.decode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.DecodeRepository
import com.humotron.app.domain.modal.param.GetConversationThreadsParam
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.ConversationThreadsResponse
import com.humotron.app.domain.modal.response.FeltOffQuestionsResponse
import com.humotron.app.domain.modal.response.MetricTrackingResponse
import com.humotron.app.domain.modal.response.YetToTrackMetricResponse
import com.humotron.app.domain.modal.param.GetConversationsParam
import com.humotron.app.domain.modal.param.StartNewChatParam
import com.humotron.app.domain.modal.response.GetConversationsResponse
import com.humotron.app.domain.modal.param.PostFollowUpConversationParam
import com.humotron.app.domain.modal.response.PostFollowUpConversationResponse
import com.humotron.app.domain.modal.response.PromptContextResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecodeViewModel @Inject constructor(
    private val repository: DecodeRepository,
) : ViewModel() {

    private var currentConversationThreadId: String? = null
    private var currentPage: Int = 1
    private var isLastPage: Boolean = false

    fun getThreadId(): String? = currentConversationThreadId

    fun resetThreadId() {
        currentConversationThreadId = null
        currentPage = 1
        isLastPage = false
    }

    private val feltOffQuestionsLiveData: SingleLiveEvent<Resource<FeltOffQuestionsResponse>> =
        SingleLiveEvent()

    fun feltOffQuestionsData(): SingleLiveEvent<Resource<FeltOffQuestionsResponse>> {
        return feltOffQuestionsLiveData
    }

    fun getFeltOffQuestions() {
        viewModelScope.launch {
            repository.getFeltOffQuestions().onEach { state ->
                feltOffQuestionsLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    fun getNutritionIdeaQuestions() {
        viewModelScope.launch {
            repository.getNutritionIdeaQuestions().onEach { state ->
                feltOffQuestionsLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val conversationThreadsLiveData: SingleLiveEvent<Resource<ConversationThreadsResponse>> =
        SingleLiveEvent()

    fun conversationThreadsData(): SingleLiveEvent<Resource<ConversationThreadsResponse>> {
        return conversationThreadsLiveData
    }

    fun getAllConversationThreads(sortingOrder: String = "desc") {
        viewModelScope.launch {
            repository.getAllConversationThreads(GetConversationThreadsParam(sortingOrder)).onEach { state ->
                conversationThreadsLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val deleteThreadLiveData: SingleLiveEvent<Resource<CommonResponse>> =
        SingleLiveEvent()

    fun deleteThreadData(): SingleLiveEvent<Resource<CommonResponse>> {
        return deleteThreadLiveData
    }

    fun deleteConversationThread(threadId: String) {
        viewModelScope.launch {
            repository.deleteConversationThread(threadId).onEach { state ->
                deleteThreadLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val deleteAllThreadsLiveData: SingleLiveEvent<Resource<CommonResponse>> =
        SingleLiveEvent()

    fun deleteAllThreadsData(): SingleLiveEvent<Resource<CommonResponse>> {
        return deleteAllThreadsLiveData
    }

    fun deleteAllConversationThreads() {
        viewModelScope.launch {
            repository.deleteAllConversationThreads().onEach { state ->
                deleteAllThreadsLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val metricTrackingLiveData: SingleLiveEvent<Resource<MetricTrackingResponse>> =
        SingleLiveEvent()

    fun metricTrackingData(): SingleLiveEvent<Resource<MetricTrackingResponse>> {
        return metricTrackingLiveData
    }

    fun getMetricTrackingByUserId() {
        viewModelScope.launch {
            repository.getMetricTrackingByUserId().onEach { state ->
                metricTrackingLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val yetToTrackMetricsLiveData: SingleLiveEvent<Resource<YetToTrackMetricResponse>> =
        SingleLiveEvent()

    fun yetToTrackMetricsData(): SingleLiveEvent<Resource<YetToTrackMetricResponse>> {
        return yetToTrackMetricsLiveData
    }

    fun getYetToTrackMetricByUserId() {
        viewModelScope.launch {
            repository.getYetToTrackMetricByUserId().onEach { state ->
                yetToTrackMetricsLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val conversationsLiveData: SingleLiveEvent<Resource<GetConversationsResponse>> =
        SingleLiveEvent()

    fun conversationsData(): SingleLiveEvent<Resource<GetConversationsResponse>> {
        return conversationsLiveData
    }

    fun getConversationsByUserId(
        promptId: String? = null,
        conversationThreadId: String? = null,
        metricName: String? = null,
        isLoadMore: Boolean = false
    ) {
        if (isLoadMore && isLastPage) return

        if (!isLoadMore) {
            currentPage = 1
            isLastPage = false
        }

        viewModelScope.launch {
            repository.getConversationsByUserId(
                GetConversationsParam(
                    pageCount = if (isLoadMore) currentPage + 1 else 1,
                    promptId = promptId,
                    conversationThreadId = conversationThreadId,
                    limit = 10
                )
            ).onEach { state ->
                when (state.status) {
                    Status.SUCCESS -> {
                        if (state.data?.data.isNullOrEmpty()) {
                            if (!isLoadMore) {
                                promptId?.let {
                                    startNewChat(it, metricName ?: "")
                                }
                            } else {
                                isLastPage = true
                            }
                        } else {
                            if (isLoadMore) {
                                currentPage++
                            }
                            if ((state.data?.data?.size ?: 0) < 10) {
                                isLastPage = true
                            }
                            currentConversationThreadId = state.data?.data?.firstOrNull()?.conversationThreadId
                            conversationsLiveData.value = state
                        }
                    }
                    else -> {
                        conversationsLiveData.value = state
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun startNewChat(promptId: String, metricName: String) {
        viewModelScope.launch {
            repository.startNewChat(
                StartNewChatParam(
                    type = "metric_tracking",
                    promptId = promptId,
                    metricName = metricName
                )
            ).onEach { state ->
                when (state.status) {
                    Status.SUCCESS -> {
                        val chatData = state.data
                        currentConversationThreadId = chatData?.data?.conversationThreadId
                        
                        val response = GetConversationsResponse(
                            status = chatData?.status,
                            message = chatData?.message,
                            data = chatData?.data?.let { listOf(it) } ?: emptyList(),
                            totalRecords = 1
                        )
                        conversationsLiveData.value = Resource.success(response)
                    }
                    Status.ERROR -> {
                        conversationsLiveData.value = Resource.error(state.error!!)
                    }
                    Status.EXCEPTION -> {
                        conversationsLiveData.value = Resource.exception(state.error!!)
                    }
                    else -> {
                        conversationsLiveData.value = Resource(state.status, null, state.error)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private val followUpLiveData: SingleLiveEvent<Resource<PostFollowUpConversationResponse>> =
        SingleLiveEvent()

    fun followUpData(): SingleLiveEvent<Resource<PostFollowUpConversationResponse>> {
        return followUpLiveData
    }

    fun postFollowUpConversation(question: String) {
        val threadId = currentConversationThreadId ?: return
        viewModelScope.launch {
            repository.postFollowUpConversation(
                PostFollowUpConversationParam(
                    conversationThreadId = threadId,
                    followUpQuestion = question
                )
            ).onEach { state ->
                if (state.status == Status.SUCCESS) {
                    currentConversationThreadId = state.data?.data?.conversationThreadId ?: currentConversationThreadId
                }


                followUpLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val promptContextLiveData: SingleLiveEvent<Resource<PromptContextResponse>> =
        SingleLiveEvent()

    fun promptContextData(): SingleLiveEvent<Resource<PromptContextResponse>> {
        return promptContextLiveData
    }

    fun getPromptContextByConversationId(conversationId: String) {
        viewModelScope.launch {
            repository.getPromptContextByConversationId(conversationId).onEach { state ->
                promptContextLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }
}

