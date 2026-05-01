package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.DeliveryOptionResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler
) {
    fun getAllDeliveryOptionByLimit(): Flow<Resource<DeliveryOptionResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllDeliveryOptionByLimit(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getDefaultConfiguration(): Flow<Resource<com.humotron.app.domain.modal.response.GetDefaultConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getDefaultConfigurationNoBody(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
    fun updateUserById(userId: String, data: HashMap<String, Any>): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.updateUserById(userId, data), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getPromoCodeDetailsByPromoCode(promoCode: String): Flow<Resource<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>> = flow {
        emit(Resource.loading<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>())
        try {
            val response = api.getPromoCodeDetailsByPromoCode(promoCode)
            if (response.isSuccessful) {
                emit(Resource.success(response.body()))
            } else {
                // Parse error body to get the message from API
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    com.google.gson.Gson().fromJson(errorBody, com.humotron.app.domain.modal.response.PromoCodeDetailsResponse::class.java)
                } catch (e: Exception) { null }

                if (errorResponse != null) {
                    // Emit as success so UI can read status/message fields
                    emit(Resource.success(errorResponse))
                } else {
                    emit(responseHandler.handleException<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>(Exception(errorBody ?: "Something went wrong")))
                }
            }
        } catch (e: Exception) {
            emit(responseHandler.handleException<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>(ValidationException(it.message)))
    }

    fun removePromoCodeByUser(userId: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = api.removePromoCodeByUser(userId)
            if (response.isSuccessful) {
                emit(Resource.success(response.body()))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = try {
                    val json = JSONObject(errorBody ?: "")
                    json.optString("message", json.optString("error", "Something went wrong"))
                } catch (e: Exception) {
                    "Something went wrong"
                }
                emit(Resource.error(com.humotron.app.data.network.error.Error(errorMessage = errorMsg)))
            }
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}
