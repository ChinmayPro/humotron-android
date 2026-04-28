package com.humotron.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ShopRepository
import com.humotron.app.domain.modal.response.DeviceDetailResponse
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.domain.modal.response.GetOptimizedRecipeWithMetricsResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.domain.modal.response.ProductDetailResponse
import com.humotron.app.domain.modal.response.BookingTypeResponse
import com.humotron.app.domain.modal.param.UpdateAddressRequest
import com.humotron.app.domain.modal.response.UpdateAddressResponse
import com.humotron.app.domain.modal.response.GetAllAddressResponse
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.domain.modal.response.AddressAutocompleteResponse
import com.humotron.app.domain.modal.response.FullAddressResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    var lastSelectedTabId: Int = com.humotron.app.R.id.fragmentShopDevices
    
    // Booking Flow Data
    private var selectedBookingType: BookingTypeResponse.BookingType? = null
    private var selectedAddress: GetCartResponse.Address? = null
    private var selectedDate: java.util.Calendar? = null
    private var selectedTime: String? = null

    fun setSelectedBookingType(type: BookingTypeResponse.BookingType?) { selectedBookingType = type }
    fun getSelectedBookingType() = selectedBookingType

    fun setSelectedAddress(address: GetCartResponse.Address?) { selectedAddress = address }
    fun getSelectedAddress() = selectedAddress

    fun setSelectedDateTime(date: java.util.Calendar?, time: String?) {
        selectedDate = date
        selectedTime = time
    }
    fun getSelectedDate() = selectedDate
    fun getSelectedTime() = selectedTime

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
    
    private val addressAutocompleteLiveData: SingleLiveEvent<Resource<AddressAutocompleteResponse>> = SingleLiveEvent()
    fun getAddressAutocompleteLiveData(): SingleLiveEvent<Resource<AddressAutocompleteResponse>> = addressAutocompleteLiveData

    private val fullAddressLiveData: SingleLiveEvent<Resource<FullAddressResponse>> = SingleLiveEvent()
    fun getFullAddressLiveData(): SingleLiveEvent<Resource<FullAddressResponse>> = fullAddressLiveData

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

    private val productLikeDislikeLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getProductLikeDislikeLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = productLikeDislikeLiveData

    fun productLikeDislike(id: String) {
        repository.productLikeDislike(id).onEach { state ->
            productLikeDislikeLiveData.value = state
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

    private val bookingTypeLiveData: SingleLiveEvent<Resource<BookingTypeResponse>> = SingleLiveEvent()
    fun getBookingTypeLiveData(): SingleLiveEvent<Resource<BookingTypeResponse>> = bookingTypeLiveData

    fun fetchBookingTypes() {
        repository.getAllTestBookingsType().onEach { state ->
            bookingTypeLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val defaultConfigLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetDefaultConfigResponse>> = SingleLiveEvent()
    fun getDefaultConfigLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetDefaultConfigResponse>> = defaultConfigLiveData

    fun fetchDefaultConfig(payload: String, iv: String) {
        val request = com.humotron.app.domain.modal.param.DefaultConfigRequest(payload, iv)
        repository.getDefaultConfiguration(request).onEach { state ->
            defaultConfigLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val allAddressLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetAllAddressResponse>> = SingleLiveEvent()
    fun getAllAddressLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetAllAddressResponse>> = allAddressLiveData

    fun fetchAllAddress() {
        repository.getAllAddressByUserId().onEach { state ->
            allAddressLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val updateAddressLiveData: SingleLiveEvent<Resource<UpdateAddressResponse>> = SingleLiveEvent()
    fun getUpdateAddressLiveData(): SingleLiveEvent<Resource<UpdateAddressResponse>> = updateAddressLiveData

    fun updateAddress(addressId: String, request: UpdateAddressRequest) {
        repository.updateAddressById(addressId, request).onEach { state ->
            updateAddressLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchAddressAutocomplete(term: String) {
        repository.getAddressAutocomplete(term).onEach { state ->
            addressAutocompleteLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun fetchFullAddress(id: String) {
        repository.getFullAddress(id).onEach { state ->
            fullAddressLiveData.value = state
        }.launchIn(viewModelScope)
    }

    private val productDetailLiveData: SingleLiveEvent<Resource<ProductDetailResponse>> = SingleLiveEvent()
    fun getProductDetailLiveData(): SingleLiveEvent<Resource<ProductDetailResponse>> = productDetailLiveData

    fun fetchProductDetail(id: String) {
        repository.getProductDetail(id).onEach { state ->
            productDetailLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
