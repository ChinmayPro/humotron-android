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
import com.humotron.app.domain.modal.response.InsightMetricsOverviewResponse
import com.humotron.app.domain.modal.response.InsightTimelineResponse
import com.humotron.app.domain.modal.response.InsightSummaryResponse
import com.humotron.app.domain.modal.response.InsightDetailResponse
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
    var navigatedToBoosterDetails: Boolean = false

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

    fun getHealthMetricTrackingByUserId() {
        viewModelScope.launch {
            repository.getHealthMetricTrackingByUserId().onEach { state ->
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

    private val insightMetricsOverviewLiveData: SingleLiveEvent<Resource<InsightMetricsOverviewResponse>> =
        SingleLiveEvent()

    fun insightMetricsOverviewData(): SingleLiveEvent<Resource<InsightMetricsOverviewResponse>> {
        return insightMetricsOverviewLiveData
    }

    fun getInsightMetricsOverview() {
        viewModelScope.launch {
            repository.getInsightMetricsOverview().onEach { state ->
                insightMetricsOverviewLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val insightTimelineLiveData: SingleLiveEvent<Resource<InsightTimelineResponse>> =
        SingleLiveEvent()

    fun insightTimelineData(): SingleLiveEvent<Resource<InsightTimelineResponse>> {
        return insightTimelineLiveData
    }

    fun getInsightTimeline(metricId: String) {
        viewModelScope.launch {
            repository.getInsightTimeline(metricId).onEach { state ->
                insightTimelineLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val insightSummaryLiveData: SingleLiveEvent<Resource<InsightSummaryResponse>> =
        SingleLiveEvent()

    fun insightSummaryData(): SingleLiveEvent<Resource<InsightSummaryResponse>> {
        return insightSummaryLiveData
    }

    fun getInsightSummaryByMetricId(metricId: String) {
        viewModelScope.launch {
            repository.getInsightSummaryByMetricId(metricId).onEach { state ->
                insightSummaryLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val insightDetailLiveData: SingleLiveEvent<Resource<InsightDetailResponse>> =
        SingleLiveEvent()

    fun insightDetailData(): SingleLiveEvent<Resource<InsightDetailResponse>> {
        return insightDetailLiveData
    }

    fun getInsightById(insightId: String) {
        viewModelScope.launch {
            repository.getInsightById(insightId).onEach { state ->
                insightDetailLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val workDayStressLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressResponse>> =
        SingleLiveEvent()

    fun workDayStressData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressResponse>> {
        return workDayStressLiveData
    }

    fun getWorkDayStress() {
        viewModelScope.launch {
            repository.getWorkDayStress().onEach { state ->
                workDayStressLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val weatherResilienceLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WeatherResilienceResponse>> =
        SingleLiveEvent()

    fun weatherResilienceData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WeatherResilienceResponse>> {
        return weatherResilienceLiveData
    }

    fun getWeatherResilienceOverview() {
        viewModelScope.launch {
            repository.getWeatherResilienceOverview().onEach { state ->
                weatherResilienceLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val weatherOverviewLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WeatherOverviewResponse>> =
        SingleLiveEvent()

    fun weatherOverviewData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WeatherOverviewResponse>> {
        return weatherOverviewLiveData
    }

    fun getWeatherOverview() {
        viewModelScope.launch {
            repository.getWeatherOverview().onEach { state ->
                weatherOverviewLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val weatherDetailLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WeatherDetailResponse>> =
        SingleLiveEvent()

    fun weatherDetailData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WeatherDetailResponse>> {
        return weatherDetailLiveData
    }

    fun getWeatherResilienceReportDetail(reportId: String) {
        viewModelScope.launch {
            repository.getWeatherResilienceReportDetail(reportId).onEach { state ->
                weatherDetailLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val workDayStressReportDayLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressReportResponse>> =
        SingleLiveEvent()

    fun workDayStressReportDayData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressReportResponse>> {
        return workDayStressReportDayLiveData
    }

    fun getWorkDayStressReportDay(startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.getWorkDayStressReport(
                com.humotron.app.domain.modal.response.WorkDayStressReportRequest(startDate, endDate, "DAY")
            ).onEach { state ->
                workDayStressReportDayLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val workDayStressReportMonthLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressReportResponse>> =
        SingleLiveEvent()

    fun workDayStressReportMonthData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressReportResponse>> {
        return workDayStressReportMonthLiveData
    }

    fun getWorkDayStressReportMonth(startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.getWorkDayStressReport(
                com.humotron.app.domain.modal.response.WorkDayStressReportRequest(startDate, endDate, "MONTH")
            ).onEach { state ->
                workDayStressReportMonthLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val workdayStressOverviewLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressOverviewResponse>> =
        SingleLiveEvent()

    fun workdayStressOverviewData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkDayStressOverviewResponse>> {
        return workdayStressOverviewLiveData
    }

    fun getWorkdayStressOverview() {
        viewModelScope.launch {
            repository.getWorkdayStressOverview().onEach { state ->
                workdayStressOverviewLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private val workdayStressReportDetailLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkdayStressReportDetailResponse>> =
        SingleLiveEvent()

    fun workdayStressReportDetailData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.WorkdayStressReportDetailResponse>> {
        return workdayStressReportDetailLiveData
    }

    fun getWorkdayStressReportById(reportId: String) {
        viewModelScope.launch {
            repository.getWorkdayStressReportById(reportId).onEach { state ->
                workdayStressReportDetailLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    fun generateWorkdayStressReport() {
        viewModelScope.launch {
            repository.generateWorkdayStressReport().onEach { state ->
                workdayStressReportDetailLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }
}

