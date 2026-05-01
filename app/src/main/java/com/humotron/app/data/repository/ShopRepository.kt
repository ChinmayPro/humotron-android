package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.response.BookAddToCartResponse
import com.humotron.app.domain.modal.response.BookLikeResponse
import com.humotron.app.domain.modal.response.BookPreferenceResponse
import com.humotron.app.domain.modal.response.DeviceDetailResponse
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.domain.modal.response.GetOptimizedRecipeWithMetricsResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.domain.modal.response.ProductDetailResponse
import com.humotron.app.domain.modal.response.ProductVariantResponse
import com.humotron.app.domain.modal.response.BookingTypeResponse
import com.humotron.app.domain.modal.param.UpdateAddressRequest
import com.humotron.app.domain.modal.response.UpdateAddressResponse
import com.humotron.app.domain.modal.response.GetAllAddressResponse
import com.humotron.app.domain.modal.response.AddressAutocompleteResponse
import com.humotron.app.domain.modal.response.FullAddressResponse
import com.humotron.app.domain.modal.response.GetAllLabResponse
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

    fun productLikeDislike(id: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.productLikeDislike(id), false)
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

    fun addToCart(param: com.humotron.app.domain.modal.param.AddToCartParam): Flow<Resource<com.humotron.app.domain.modal.response.ShopAddToCartResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.addToCartDevice(param), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun createBookCart(param: com.humotron.app.domain.modal.param.AddToCartParam): Flow<Resource<com.humotron.app.domain.modal.response.BookAddToCartResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.createBookCart(param), false)
            if (response.status == com.humotron.app.data.network.Status.SUCCESS) {
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

    fun deleteCartItem(itemId: String): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
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

    fun getCartByUserId(): Flow<Resource<com.humotron.app.domain.modal.response.GetCartResponse>> = flow {
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

    fun getOptimizedRecipeWithMetrics(): Flow<Resource<GetOptimizedRecipeWithMetricsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getOptimizedRecipeWithMetrics(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getBookByUserPreference(): Flow<Resource<BookPreferenceResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getBookDetail(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun likeBook(bookId: String): Flow<Resource<com.humotron.app.domain.modal.response.BookLikeResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.likeBook(bookId), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllTestBookingsType(): Flow<Resource<BookingTypeResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllTestBookingsType(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getDefaultConfiguration(request: com.humotron.app.domain.modal.param.DefaultConfigRequest): Flow<Resource<com.humotron.app.domain.modal.response.GetDefaultConfigResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse<com.humotron.app.domain.modal.response.GetDefaultConfigResponse>(api.getDefaultConfiguration(request), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun getAllAddressByUserId(): Flow<Resource<GetAllAddressResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllAddressByUserId(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun updateAddressById(addressId: String, request: UpdateAddressRequest): Flow<Resource<UpdateAddressResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.updateAddressById(addressId, request), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun getAddressAutocomplete(term: String): Flow<Resource<AddressAutocompleteResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAddressAutocomplete(term), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun getFullAddress(id: String): Flow<Resource<FullAddressResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getFullAddress(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun getProductDetail(id: String): Flow<Resource<ProductDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getProductById(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllLabName(postcode: String): Flow<Resource<GetAllLabResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllLabName(postcode), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}
