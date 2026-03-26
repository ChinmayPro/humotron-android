package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.AssessmentResponse
import com.humotron.app.domain.modal.response.SubmitAnswerRequest
import com.humotron.app.domain.modal.response.SubmitAnswerResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AssessmentRepository @Inject
constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler,
) {
   suspend fun getAssessment(id: String, token: String): Flow<Resource<AssessmentResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.getAssessment(id, token), false
                )

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun submitAnswers(
        token: String,
        request: SubmitAnswerRequest
    ): Flow<Resource<SubmitAnswerResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = api.submitAssessmentAnswers(
                token = token,
                request = request
            )
            if (response.isSuccessful) {
                emit(Resource.success(response.body()))
            } else {
//                emit(Resource.error(response.message()))
            }
        } catch (e: Exception) {
//            emit(Resource.exception(e))
        }
    }
}