package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.Status
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.param.CreateNuggetPrefParam
import com.humotron.app.domain.modal.param.NuggetsInteraction
import com.humotron.app.domain.modal.response.AddToCartResponse
import com.humotron.app.domain.modal.response.BioHackProgressResponse
import com.humotron.app.domain.modal.response.BookDetailResponse
import com.humotron.app.domain.modal.response.BookLikeResponse
import com.humotron.app.domain.modal.response.BookPreferenceResponse
import com.humotron.app.domain.modal.response.NuggetDetailResponse
import com.humotron.app.domain.modal.response.NuggetPreference
import com.humotron.app.domain.modal.response.NuggetsReactionResponse
import com.humotron.app.domain.modal.response.NuggetsTypeAndLevelResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BioHackRepository @Inject
constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler,
) {

    fun getNuggetsPreference(): Flow<Resource<NuggetPreference>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getNuggetsPreference())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getNuggetsTypeAndLevel(): Flow<Resource<NuggetsTypeAndLevelResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getNuggetsTypeAndLevel())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun nuggetsInteraction(param: NuggetsInteraction): Flow<Resource<NuggetsReactionResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.nuggetsInteraction(
                        param.nuggetId,
                        param.anecdoteId,
                        param
                    )
                )

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun createPreference(param: CreateNuggetPrefParam): Flow<Resource<Any>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.createNuggetPreference(param))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getNuggetDetails(id: String): Flow<Resource<NuggetDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getNuggetDetails(id))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun likeBook(id: String): Flow<Resource<BookLikeResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.likeBook(id))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun addToCart(param: AddToCartParam): Flow<Resource<AddToCartResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.addToCart(param))
            if (response.status == Status.SUCCESS) {
                response.data?.data?.id = param.productId
            }

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }


    fun getBookDetail(): Flow<Resource<BookPreferenceResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getBookDetail())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getBioHackProgress(): Flow<Resource<BioHackProgressResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getBioHackProgress())

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getBookSummary(bookId: String): Flow<Resource<BookDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(api.getBookSummary(bookId))

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}