package com.humotron.app.ui.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.OrderRepository
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.domain.modal.response.GetAllOrderResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val orderListLiveData: SingleLiveEvent<Resource<GetAllOrderResponse>> = SingleLiveEvent()
    fun getOrderListLiveData(): SingleLiveEvent<Resource<GetAllOrderResponse>> = orderListLiveData

    private val orderDetailLiveData: SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetOrderDetailResponse>> = SingleLiveEvent()
    fun getOrderDetailLiveData(): SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.GetOrderDetailResponse>> = orderDetailLiveData

    private val cancelOrderLiveData: SingleLiveEvent<Resource<CommonResponse>> = SingleLiveEvent()
    fun getCancelOrderLiveData(): SingleLiveEvent<Resource<CommonResponse>> = cancelOrderLiveData

    private var currentPage = 1
    private var isLastPage = false
    private val limit = 10

    fun fetchOrderList(isFirstPage: Boolean = false) {
        if (isFirstPage) {
            currentPage = 1
            isLastPage = false
        }

        if (isLastPage) return

        repository.getAllOrderListByUser(currentPage, limit).onEach { state ->
            orderListLiveData.value = state
            if (state.status == Status.SUCCESS) {
                // Since the provided JSON doesn't show pagination metadata, 
                // we'll assume it's a single page for now or until the API is updated.
                isLastPage = true 
            }
        }.launchIn(viewModelScope)
    }

    fun fetchOrderDetail(orderId: String) {
        repository.getOrderDetailById(orderId).onEach { state ->
            orderDetailLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun cancelOrder(orderId: String) {
        repository.cancelOrder(orderId).onEach { state ->
            cancelOrderLiveData.value = state
        }.launchIn(viewModelScope)
    }
}
