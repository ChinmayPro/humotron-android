package com.humotron.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.CartRepository
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository
) : ViewModel() {

    private val cartLiveData: SingleLiveEvent<Resource<GetCartResponse>> = SingleLiveEvent()
    fun getCartLiveData(): SingleLiveEvent<Resource<GetCartResponse>> = cartLiveData

    private val deleteCartItemLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getDeleteCartItemLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = deleteCartItemLiveData

    private val editCartQtyLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getEditCartQtyLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = editCartQtyLiveData

    private val removePromoCodeLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getRemovePromoCodeLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = removePromoCodeLiveData

    private val updateUserLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = SingleLiveEvent()
    fun getUpdateUserLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.CommonResponse>> = updateUserLiveData

    fun fetchCart() {
        repository.getCartByUserId().onEach { state ->
            cartLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun deleteCartItem(itemId: String) {
        repository.deleteCartItemById(itemId).onEach { state ->
            deleteCartItemLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun editCartQty(itemId: String, quantity: Int) {
        repository.editCartQtyByItemId(itemId, quantity).onEach { state ->
            editCartQtyLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun removePromoCode(userId: String) {
        repository.removePromoCodeByUser(userId).onEach { state ->
            removePromoCodeLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun updateUser(userId: String, data: HashMap<String, Any>) {
        repository.updateUserById(userId, data).onEach { state ->
            updateUserLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
