package com.humotron.app.ui.shop

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.ShopRepository
import com.humotron.app.domain.modal.response.ProductVariantResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ShopBuyNowViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    private val productVariantLiveData: MutableLiveData<Resource<ProductVariantResponse>> = MutableLiveData()
    private val likeDislikeDeviceLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    private val addToCartLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.ShopAddToCartResponse>> = SingleLiveEvent()

    fun getProductVariantLiveData(): MutableLiveData<Resource<ProductVariantResponse>> = productVariantLiveData
    fun getLikeDislikeDeviceLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = likeDislikeDeviceLiveData
    fun getAddToCartLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.ShopAddToCartResponse>> = addToCartLiveData

    fun getProductVariantById(id: String) {
        repository.getProductVariantById(id).onEach { state ->
            productVariantLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun likeDislikeDevice(id: String) {
        repository.likeDislikeDevice(id).onEach { state ->
            likeDislikeDeviceLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun addToCart(param: com.humotron.app.domain.modal.param.AddToCartParam) {
        repository.addToCart(param).onEach { state ->
            addToCartLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
