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

    fun deleteUserHardwareById(id: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.deleteUserHardwareById(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun deleteUserById(id: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val emptyBody = okhttp3.RequestBody.create(null, "")
            val response = responseHandler.handleResponse(api.deleteUserById(id, emptyBody), false)
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

    fun getHealthProfileConfigAndPreferences(): Flow<Resource<com.humotron.app.domain.modal.response.HealthProfileConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getHealthProfileConfigAndPreferences(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun saveHealthProfileConfigAndPreferences(param: com.humotron.app.domain.modal.param.HealthProfileConfigRequest): Flow<Resource<com.humotron.app.domain.modal.response.HealthProfileConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.saveHealthProfileConfigAndPreferences(param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getInsightConfigAndPreferences(): Flow<Resource<com.humotron.app.domain.modal.response.InsightConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getInsightConfigAndPreferences(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun saveInsightConfigAndPreferences(param: com.humotron.app.domain.modal.param.InsightConfigRequest): Flow<Resource<com.humotron.app.domain.modal.response.InsightConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.saveInsightConfigAndPreferences(param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getChatConfigAndPreferences(): Flow<Resource<com.humotron.app.domain.modal.response.ChatConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getChatConfigAndPreferences(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun saveChatConfigAndPreferences(param: com.humotron.app.domain.modal.param.ChatConfigRequest): Flow<Resource<com.humotron.app.domain.modal.response.ChatConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.saveChatConfigAndPreferences(param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getRecipeConfigAndPreferences(): Flow<Resource<com.humotron.app.domain.modal.response.RecipeConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getRecipeConfigAndPreferences(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun saveRecipeConfigAndPreferences(param: com.humotron.app.domain.modal.param.RecipeConfigRequest): Flow<Resource<com.humotron.app.domain.modal.response.RecipeConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.saveRecipeConfigAndPreferences(param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllDeviceData(): Flow<Resource<com.humotron.app.domain.modal.response.GetAllDeviceResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllDeviceData(), false)
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

    fun getDeviceConfiguration(id: String): Flow<Resource<com.humotron.app.domain.modal.response.GetDeviceConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getDeviceConfiguration(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun addDeviceMetaData(data: com.humotron.app.domain.modal.param.DeviceMetaDataParam): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.addDeviceMetaData(data), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getDataSources(): Flow<Resource<com.humotron.app.domain.modal.response.DataSourcesResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getDataSources(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getDataSourceDetail(sourceKey: String): Flow<Resource<com.humotron.app.domain.modal.response.DataSourceDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getDataSourceDetail(sourceKey), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun updateDataSourceUsage(
        sourceKey: String,
        param: com.humotron.app.domain.modal.param.UpdateDataSourceUsageParam
    ): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.updateDataSourceUsage(sourceKey, param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun pauseDataSource(
        sourceKey: String,
        param: com.humotron.app.domain.modal.param.PauseDataSourceParam
    ): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.pauseDataSource(sourceKey, param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun updateDataSourceTopics(
        sourceKey: String,
        body: Map<String, Boolean>
    ): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.updateDataSourceTopics(sourceKey, body), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun deleteDataSourceData(
        sourceKey: String
    ): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.deleteDataSourceData(sourceKey), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getRecipesByMetricReading(
        metricId: String
    ): Flow<Resource<com.humotron.app.domain.modal.response.GetRecipesByMetricReadingResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getRecipesByMetricReading(metricId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}
