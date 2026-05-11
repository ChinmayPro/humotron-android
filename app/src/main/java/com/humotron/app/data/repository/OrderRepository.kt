package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.GetAllOrderResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler
) {
    fun getAllOrderListByUser(page: Int, limit: Int): Flow<Resource<GetAllOrderResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllOrderListByUser(page, limit), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getOrderDetailById(orderId: String): Flow<Resource<com.humotron.app.domain.modal.response.GetOrderDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getOrderDetailById(orderId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun cancelOrder(orderId: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.cancelOrder(orderId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun getOrderTrackingDetails(orderNumber: String): Flow<Resource<com.humotron.app.domain.modal.response.GetOrderTrackingResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getOrderTrackingDetails(orderNumber), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun getBloodTestOrders(): Flow<Resource<com.humotron.app.domain.modal.response.GetBloodTestOrderResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getBloodTestOrders(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }
}
