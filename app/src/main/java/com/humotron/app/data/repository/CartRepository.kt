package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.GetCartResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler
) {
    fun getCartByUserId(): Flow<Resource<GetCartResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getCartByUserId(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun deleteCartItemById(itemId: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.deleteCartItemById(itemId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}
