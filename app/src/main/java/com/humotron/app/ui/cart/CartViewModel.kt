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

    fun fetchCart() {
        repository.getCartByUserId().onEach { state ->
            cartLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
