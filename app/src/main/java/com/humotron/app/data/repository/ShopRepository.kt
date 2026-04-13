package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.DeviceDetailResponse
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.domain.modal.response.ProductVariantResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler
) {
    fun getShopDevices(): Flow<Resource<GetShopDevicesResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllDeviceWithMetrics(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getDeviceDetail(id: String): Flow<Resource<DeviceDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getDeviceDetailsById(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getDeviceFaqs(id: String): Flow<Resource<DeviceFaqResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getDeviceFaqs(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun likeDislikeDevice(id: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.deviceLikeDislike(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getProductVariantById(id: String): Flow<Resource<ProductVariantResponse>> = flow {
        emit(Resource.loading<ProductVariantResponse>())
        try {
            val response = responseHandler.handleResponse<ProductVariantResponse>(api.getProductVariantById(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException<ProductVariantResponse>(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException<ProductVariantResponse>(ValidationException(it.message)))
    }
}
