package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.param.GetConversationThreadsParam
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.ConversationThreadsResponse
import com.humotron.app.domain.modal.response.FeltOffQuestionsResponse
import com.humotron.app.domain.modal.response.MetricTrackingResponse
import com.humotron.app.domain.modal.response.YetToTrackMetricResponse
import com.humotron.app.domain.modal.param.GetConversationsParam
import com.humotron.app.domain.modal.response.GetConversationsResponse
import com.humotron.app.domain.modal.param.PostFollowUpConversationParam
import com.humotron.app.domain.modal.param.StartNewChatParam
import com.humotron.app.domain.modal.response.PostFollowUpConversationResponse
import com.humotron.app.domain.modal.response.PromptContextResponse
import com.humotron.app.domain.modal.response.InsightMetricsOverviewResponse
import com.humotron.app.domain.modal.response.InsightTimelineResponse
import com.humotron.app.domain.modal.response.InsightSummaryResponse
import com.humotron.app.domain.modal.response.InsightDetailResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DecodeRepository @Inject
constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler,
) {

    fun getFeltOffQuestions(): Flow<Resource<FeltOffQuestionsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getFeltOffQuestions())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getNutritionIdeaQuestions(): Flow<Resource<FeltOffQuestionsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getNutritionIdeaQuestions())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllConversationThreads(param: GetConversationThreadsParam): Flow<Resource<ConversationThreadsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getAllConversationThreads(param))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun deleteAllConversationThreads(): Flow<Resource<CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.deleteAllConversationThreads())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun deleteConversationThread(threadId: String): Flow<Resource<CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.deleteConversationThread(threadId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getHealthMetricTrackingByUserId(): Flow<Resource<MetricTrackingResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getHealthMetricTrackingByUserId())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getYetToTrackMetricByUserId(): Flow<Resource<YetToTrackMetricResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getYetToTrackMetricByUserId())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getConversationsByUserId(param: GetConversationsParam): Flow<Resource<GetConversationsResponse>> = flow {
        emit(Resource.loading<GetConversationsResponse>())
        try {
            val response =
                responseHandler.handleResponse(api.getConversationsByUserId(param))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException<GetConversationsResponse>(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException<GetConversationsResponse>(ValidationException(it.message)))
    }

    fun postFollowUpConversation(param: PostFollowUpConversationParam): Flow<Resource<PostFollowUpConversationResponse>> = flow {
        emit(Resource.loading<PostFollowUpConversationResponse>())
        try {
            val response =
                responseHandler.handleResponse(api.postFollowUpConversation(param))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException<PostFollowUpConversationResponse>(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException<PostFollowUpConversationResponse>(ValidationException(it.message)))
    }

    fun startNewChat(param: StartNewChatParam): Flow<Resource<PostFollowUpConversationResponse>> = flow {
        emit(Resource.loading<PostFollowUpConversationResponse>())
        try {
            val response =
                responseHandler.handleResponse(api.startNewChat(param))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException<PostFollowUpConversationResponse>(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException<PostFollowUpConversationResponse>(ValidationException(it.message)))
    }

    fun getPromptContextByConversationId(conversationId: String): Flow<Resource<PromptContextResponse>> = flow {
        emit(Resource.loading<PromptContextResponse>())
        try {
            val response =
                responseHandler.handleResponse(api.getPromptContextByConversationId(conversationId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException<PromptContextResponse>(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException<PromptContextResponse>(ValidationException(it.message)))
    }

    fun getInsightMetricsOverview(): Flow<Resource<InsightMetricsOverviewResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getInsightMetricsOverview())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getInsightTimeline(metricId: String): Flow<Resource<InsightTimelineResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getInsightTimeline(metricId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getInsightSummaryByMetricId(metricId: String): Flow<Resource<InsightSummaryResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getInsightSummaryByMetricId(metricId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getInsightById(insightId: String): Flow<Resource<InsightDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getInsightById(insightId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWorkDayStress(): Flow<Resource<com.humotron.app.domain.modal.response.WorkDayStressResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWorkDayStress())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWeatherResilienceOverview(): Flow<Resource<com.humotron.app.domain.modal.response.WeatherResilienceResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWeatherResilienceOverview())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWeatherOverview(): Flow<Resource<com.humotron.app.domain.modal.response.WeatherOverviewResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWeatherOverview())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWeatherResilienceReportDetail(reportId: String): Flow<Resource<com.humotron.app.domain.modal.response.WeatherDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWeatherResilienceReportDetail(reportId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
    fun getWorkDayStressReport(request: com.humotron.app.domain.modal.response.WorkDayStressReportRequest): Flow<Resource<com.humotron.app.domain.modal.response.WorkDayStressReportResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWorkDayStressReport(request))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWorkdayStressOverview(): Flow<Resource<com.humotron.app.domain.modal.response.WorkDayStressOverviewResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWorkdayStressOverview())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getWorkdayStressReportById(reportId: String): Flow<Resource<com.humotron.app.domain.modal.response.WorkdayStressReportDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getWorkdayStressReportById(reportId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun generateWorkdayStressReport(): Flow<Resource<com.humotron.app.domain.modal.response.WorkdayStressReportDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.generateWorkdayStressReport())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}

