package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.param.RemovePdfParam
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.ExtractMetricsResponse
import com.humotron.app.domain.modal.response.GenerateMetricResponse
import com.humotron.app.domain.modal.response.MedicalPdfResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicalRepository @Inject constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler
) {

    fun createClinicalDocuments(
        isCreateNugget: Boolean,
        uploadType: String,
        file: File
    ): Flow<Resource<ExtractMetricsResponse>> = flow {
        emit(Resource.loading())
        
        val uploadTypeBody = uploadType.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
        val multipartBodyPart = MultipartBody.Part.createFormData("clinicalDocumentsFiles", file.name, requestFile)

        try {
            val response = api.createClinicalDocuments(isCreateNugget, uploadTypeBody, multipartBodyPart)
            emit(responseHandler.handleResponse(response, true))
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
        }
    }.catch {
        emit(Resource.error(com.humotron.app.data.network.error.Error(errorMessage = it.message ?: "Unknown error")))
    }

    fun getAllPdfList(): Flow<Resource<MedicalPdfResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = api.getAllPdfList()
            emit(responseHandler.handleResponse(response, false))
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun generateMetricByPdfId(pdfId: String): Flow<Resource<GenerateMetricResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = api.generateMetricByPdfId(com.humotron.app.domain.modal.param.GenerateMetricParam(pdfId))
            emit(responseHandler.handleResponse(response, false))
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
        }
    }.catch {
        emit(Resource.error(com.humotron.app.data.network.error.Error(errorMessage = it.message ?: "Unknown error")))
    }

    fun removePdfByPdfId(pdfId: String): Flow<Resource<CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = api.removePdfByPdfId(RemovePdfParam(pdfId))
            emit(responseHandler.handleResponse(response, false))
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
        }
    }.catch {
        emit(Resource.error(com.humotron.app.data.network.error.Error(errorMessage = it.message ?: "Unknown error")))
    }
}
