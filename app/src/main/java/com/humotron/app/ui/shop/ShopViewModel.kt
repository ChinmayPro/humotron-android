package com.humotron.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ShopRepository
import com.humotron.app.domain.modal.response.DeviceDetailResponse
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.domain.modal.response.GetOptimizedRecipeWithMetricsResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    private val devicesLiveData: SingleLiveEvent<Resource<GetShopDevicesResponse>> = SingleLiveEvent()
    private val deviceDetailLiveData: SingleLiveEvent<Resource<DeviceDetailResponse>> = SingleLiveEvent()
    private val deviceFaqsLiveData: SingleLiveEvent<Resource<DeviceFaqResponse>> = SingleLiveEvent()
    private val optimizedRecipeLiveData: SingleLiveEvent<Resource<GetOptimizedRecipeWithMetricsResponse>> = SingleLiveEvent()
    private val bookPreferenceLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.BookPreferenceResponse>> = SingleLiveEvent()

    fun getDevicesLiveData(): SingleLiveEvent<Resource<GetShopDevicesResponse>> = devicesLiveData
    fun getDeviceDetailLiveData(): SingleLiveEvent<Resource<DeviceDetailResponse>> = deviceDetailLiveData
    fun getDeviceFaqsLiveData(): SingleLiveEvent<Resource<DeviceFaqResponse>> = deviceFaqsLiveData
    fun getOptimizedRecipeLiveData(): SingleLiveEvent<Resource<GetOptimizedRecipeWithMetricsResponse>> = optimizedRecipeLiveData
    fun getBookPreferenceLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.BookPreferenceResponse>> = bookPreferenceLiveData

    fun fetchShopDevices() {
        repository.getShopDevices().onEach { state ->
            devicesLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchDeviceDetail(id: String) {
        repository.getDeviceDetail(id).onEach { state ->
            deviceDetailLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchDeviceFaqs(id: String) {
        repository.getDeviceFaqs(id).onEach { state ->
            deviceFaqsLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val likeDislikeDeviceLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getLikeDislikeDeviceLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = likeDislikeDeviceLiveData

    fun likeDislikeDevice(id: String) {
        repository.likeDislikeDevice(id).onEach { state ->
            likeDislikeDeviceLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchOptimizedRecipe() {
        repository.getOptimizedRecipeWithMetrics().onEach { state ->
            optimizedRecipeLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchBookPreference() {
        repository.getBookByUserPreference().onEach { state ->
            bookPreferenceLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val likeBookLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.BookLikeResponse>> = SingleLiveEvent()
    fun getLikeBookLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.BookLikeResponse>> = likeBookLiveData

    private val addToCartLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.ShopAddToCartResponse>> = SingleLiveEvent()
    fun getAddToCartLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.ShopAddToCartResponse>> = addToCartLiveData

    private val createBookCartLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.BookAddToCartResponse>> = SingleLiveEvent()
    fun getCreateBookCartLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.BookAddToCartResponse>> = createBookCartLiveData

    private val deleteCartItemLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getDeleteCartItemLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = deleteCartItemLiveData

    private val cartLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetCartResponse>> = SingleLiveEvent()
    fun getCartLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetCartResponse>> = cartLiveData

    fun likeBook(bookId: String) {
        repository.likeBook(bookId).onEach { state ->
            likeBookLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun addToCart(param: com.humotron.app.domain.modal.param.AddToCartParam) {
        repository.addToCart(param).onEach { state ->
            addToCartLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun createBookCart(param: com.humotron.app.domain.modal.param.AddToCartParam) {
        repository.createBookCart(param).onEach { state ->
            createBookCartLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun deleteCartItem(itemId: String) {
        repository.deleteCartItem(itemId).onEach { state ->
            deleteCartItemLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchCart() {
        repository.getCartByUserId().onEach { state ->
            cartLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
