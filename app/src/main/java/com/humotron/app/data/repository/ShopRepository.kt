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
import com.humotron.app.domain.modal.response.GetOptimizedRecommendationsWithMetricsResponse
import com.humotron.app.domain.modal.response.GetOptimizedRecommendationDetailResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.domain.modal.response.ProductDetailResponse
import com.humotron.app.domain.modal.response.ProductVariantResponse
import com.humotron.app.domain.modal.response.BookingTypeResponse
import com.humotron.app.domain.modal.param.CreateAddressRequest
import com.humotron.app.domain.modal.param.UpdateAddressRequest
import com.humotron.app.domain.modal.response.CreateAddressResponse
import com.humotron.app.domain.modal.response.UpdateAddressResponse
import com.humotron.app.domain.modal.response.GetAllAddressResponse
import com.humotron.app.domain.modal.response.AddressAutocompleteResponse
import com.humotron.app.domain.modal.response.AddressSuggestion
import com.humotron.app.domain.modal.response.FullAddressResponse
import com.humotron.app.domain.modal.response.IdealPostcodeResult
import java.util.concurrent.ConcurrentHashMap
import com.humotron.app.domain.modal.response.GetAllLabResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val api: AppApi,
    private val responseHandler: ResponseHandler
) {
    private val cachedIdealPostcodeResults = ConcurrentHashMap<String, IdealPostcodeResult>()
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

    fun placeOrder(request: HashMap<String, Any>): Flow<Resource<com.humotron.app.domain.modal.response.PlaceOrderResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.placeOrder(request), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun createPaymentIntent(request: HashMap<String, Any>): Flow<Resource<com.humotron.app.domain.modal.response.CreatePaymentIntentResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.createPaymentIntent(request), false)
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

    fun getOptimizedRecommendationsWithMetrics(): Flow<Resource<GetOptimizedRecommendationsWithMetricsResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getOptimizedRecommendationsWithMetrics(), false)
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

    fun createAddress(request: CreateAddressRequest): Flow<Resource<CreateAddressResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.createAddress(request), false)
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
            val cleanTerm = term.trim().replace(" ", "").uppercase()
            val response = api.getIdealPostcode(cleanTerm)
            if (response.isSuccessful && response.body()?.result != null) {
                val results = response.body()?.result ?: emptyList()
                val suggestions = results.mapIndexed { index, item ->
                    val id = item.id ?: "ideal_$index"
                    cachedIdealPostcodeResults[id] = item
                    val line1 = item.line1.orEmpty().ifBlank { item.premise.orEmpty() }
                    val line2AndTown = listOfNotNull(
                        item.line2.takeIf { !it.isNullOrBlank() },
                        item.postTown.takeIf { !it.isNullOrBlank() }
                    ).joinToString(", ")
                    val formattedAddress = if (line2AndTown.isNotBlank()) {
                        "$line1\n$line2AndTown"
                    } else {
                        line1
                    }
                    AddressSuggestion(
                        address = formattedAddress,
                        url = null,
                        id = id
                    )
                }
                emit(Resource.success(AddressAutocompleteResponse(suggestions = suggestions)))
            } else {
                val fallbackResponse = responseHandler.handleResponse(api.getAddressAutocomplete(term), false)
                emit(fallbackResponse)
            }
        } catch (e: Exception) {
            try {
                val fallbackResponse = responseHandler.handleResponse(api.getAddressAutocomplete(term), false)
                emit(fallbackResponse)
            } catch (ex: Exception) {
                emit(responseHandler.handleException(e))
                e.printStackTrace()
            }
        }
    }.catch {
        emit(responseHandler.handleException(com.humotron.app.data.network.exceptions.ValidationException(it.message)))
    }

    fun getFullAddress(id: String): Flow<Resource<FullAddressResponse>> = flow {
        emit(Resource.loading())
        try {
            val cachedItem = cachedIdealPostcodeResults[id]
            if (cachedItem != null) {
                val fullAddress = FullAddressResponse(
                    postcode = cachedItem.postcode,
                    line1 = cachedItem.line1,
                    line2 = cachedItem.line2,
                    line3 = cachedItem.line3,
                    line4 = null,
                    locality = cachedItem.postTown,
                    townOrCity = cachedItem.postTown ?: cachedItem.county,
                    county = cachedItem.postalCounty ?: cachedItem.county,
                    country = cachedItem.country
                )
                emit(Resource.success(fullAddress))
            } else {
                val response = responseHandler.handleResponse(api.getFullAddress(id), false)
                emit(response)
            }
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

    fun getOptimizedRecommendationDetail(id: String, type: String): Flow<Resource<GetOptimizedRecommendationDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val body = mapOf("type" to type)
            val response = responseHandler.handleResponse(api.getOptimizedRecommendationDetail(id, body), false)
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
            val res = api.getAllLabName(postcode)
            if (res.isSuccessful && res.body()?.data?.labList?.isNotEmpty() == true) {
                emit(responseHandler.handleResponse(res, false))
            } else {
                // If lab not found for current postcode, fallback to default UK lab postcode
                val fallbackRes = api.getAllLabName("E14 4QT")
                if (fallbackRes.isSuccessful) {
                    emit(responseHandler.handleResponse(fallbackRes, false))
                } else {
                    emit(responseHandler.handleResponse(res, false))
                }
            }
        } catch (e: Exception) {
            try {
                val fallbackRes = api.getAllLabName("E14 4QT")
                if (fallbackRes.isSuccessful) {
                    emit(responseHandler.handleResponse(fallbackRes, false))
                } else {
                    emit(responseHandler.handleException(e))
                }
            } catch (ex: Exception) {
                emit(responseHandler.handleException(e))
            }
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllBooster(): Flow<Resource<com.humotron.app.domain.modal.response.BoosterResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllBooster(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getAllPlan(): Flow<Resource<com.humotron.app.domain.modal.response.PlanResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getAllPlan(), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getBoosterById(id: String): Flow<Resource<com.humotron.app.domain.modal.response.BoosterDetailResponse>> = flow {
        emit(Resource.loading())
        try {
            val response = responseHandler.handleResponse(api.getBoosterById(id), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun createDigitalProductOrder(
        boosterId: String = "",
        planId: String = "",
        productType: String = "booster"
    ): Flow<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = flow {
        emit(Resource.loading())
        try {
            val requestMap = hashMapOf<String, Any>(
                "boosterId" to boosterId,
                "planId" to planId,
                "productType" to productType
            )
            val response = responseHandler.handleResponse(api.createDigitalProductOrder(requestMap), false)
            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }
}
