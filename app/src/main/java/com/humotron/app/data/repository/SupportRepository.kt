package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.SupportHomeResponse
import com.humotron.app.domain.modal.response.MyTicketsResponse
import com.humotron.app.domain.modal.response.SearchTopicsResponse
import com.humotron.app.domain.modal.response.SupportTopicDetailResponse
import com.humotron.app.domain.modal.response.TopicsByCategoryResponse
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.SaveTicketResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class SupportRepository @Inject constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler
) {
    fun getSupportHome(): Flow<Resource<SupportHomeResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getSupportHome(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getMyTickets(): Flow<Resource<MyTicketsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getMyTickets(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun searchTopics(page: Int, query: String, limit: Int): Flow<Resource<SearchTopicsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.searchTopics(page, query, limit), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getTopicDetail(topicId: String): Flow<Resource<SupportTopicDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getTopicDetail(topicId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getTopicsByCategory(categoryKey: String): Flow<Resource<TopicsByCategoryResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getTopicsByCategory(categoryKey), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getSupportCategoryByKey(categoryKey: String): Flow<Resource<TopicsByCategoryResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getSupportCategoryByKey(categoryKey), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllTopics(limit: Int, page: Int): Flow<Resource<com.humotron.app.domain.modal.response.AllTopicsResponse>> = flow {
        emit(Resource.loading())
        try {
            val param = HashMap<String, Any>().apply {
                put("limit", limit)
                put("pageCount", page)
            }
            val response = responseHandler.handleResponse(api.getAllTopics(param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun saveTicket(
        category: String,
        subcategory: String,
        contactReasonCode: String,
        subject: String,
        description: String,
        currentScreen: String,
        source: String,
        osPlatform: String,
        appVersion: String,
        deviceType: String,
        region: String,
        ticketId: String,
        attachments: List<MultipartBody.Part>
    ): Flow<Resource<SaveTicketResponse>> = flow {
        emit(Resource.loading())
        try {
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
            val subcategoryBody = subcategory.toRequestBody("text/plain".toMediaTypeOrNull())
            val contactReasonCodeBody = contactReasonCode.toRequestBody("text/plain".toMediaTypeOrNull())
            val subjectBody = subject.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val currentScreenBody = currentScreen.toRequestBody("text/plain".toMediaTypeOrNull())
            val sourceBody = source.toRequestBody("text/plain".toMediaTypeOrNull())
            val osPlatformBody = osPlatform.toRequestBody("text/plain".toMediaTypeOrNull())
            val appVersionBody = if (appVersion.isNotBlank()) appVersion.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val deviceTypeBody = if (deviceType.isNotBlank()) deviceType.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val regionBody = if (region.isNotBlank()) region.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val ticketIdBody = if (ticketId.isNotBlank()) ticketId.toRequestBody("text/plain".toMediaTypeOrNull()) else null

            val response = api.saveTicket(
                categoryBody,
                subcategoryBody,
                contactReasonCodeBody,
                subjectBody,
                descriptionBody,
                currentScreenBody,
                sourceBody,
                osPlatformBody,
                appVersionBody,
                deviceTypeBody,
                regionBody,
                ticketIdBody,
                attachments
            )
            emit(responseHandler.handleResponse(response, false))
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getTicketDetail(ticketId: String): Flow<Resource<com.humotron.app.domain.modal.response.TicketDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getTicketDetail(ticketId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun replyTicket(
        ticketId: String,
        body: String,
        attachments: List<MultipartBody.Part>
    ): Flow<Resource<CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val messageBody = body.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = api.replyTicket(ticketId, messageBody, attachments)
            emit(responseHandler.handleResponse(response, false))
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}
