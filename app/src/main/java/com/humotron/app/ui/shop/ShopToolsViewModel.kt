package com.humotron.app.ui.shop

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.error.Error
import com.humotron.app.data.repository.ShopRepository
import com.humotron.app.domain.modal.response.BoosterResponse
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.util.BillingManager
import com.humotron.app.util.BillingPurchaseResult
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopToolsViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _boostersLiveData = MutableLiveData<Resource<List<BoosterResponse.Booster>>>()
    val boostersLiveData: LiveData<Resource<List<BoosterResponse.Booster>>> = _boostersLiveData

    private val _activePurchasesLiveData = MutableLiveData<List<Purchase>>()
    val activePurchasesLiveData: LiveData<List<Purchase>> = _activePurchasesLiveData

    private val _playStoreProductsLiveData = MutableLiveData<List<ProductDetails>>()
    val playStoreProductsLiveData: LiveData<List<ProductDetails>> = _playStoreProductsLiveData

    private val _orderResultLiveData = SingleLiveEvent<Resource<CommonResponse>>()
    val orderResultLiveData: LiveData<Resource<CommonResponse>> = _orderResultLiveData

    private val _boosterDetailLiveData = SingleLiveEvent<Resource<com.humotron.app.domain.modal.response.BoosterDetailResponse>>()
    val boosterDetailLiveData: LiveData<Resource<com.humotron.app.domain.modal.response.BoosterDetailResponse>> = _boosterDetailLiveData

    val purchaseSuccessEvent = SingleLiveEvent<Purchase>()
    val purchaseErrorEvent = SingleLiveEvent<String>()
    val purchaseCancelEvent = SingleLiveEvent<Unit>()

    private var playStoreProducts = listOf<ProductDetails>()
    private var pendingPurchaseBooster: BoosterResponse.Booster? = null

    private var cachedBoosterProductIds = listOf<String>()

    init {
        observeBillingReady()
        observeActivePurchases()
        observePurchaseEvents()
    }

    private fun observeBillingReady() {
        billingManager.isReady.onEach { isReady ->
            if (isReady) {
                fetchProductList()
            }
        }.launchIn(viewModelScope)
    }

    private fun observeActivePurchases() {
        billingManager.activePurchases.onEach { purchases ->
            _activePurchasesLiveData.value = purchases
        }.launchIn(viewModelScope)
    }

    private fun observePurchaseEvents() {
        viewModelScope.launch {
            billingManager.purchaseEvents.collect { result ->
                when (result) {
                    is BillingPurchaseResult.Success -> {
                        val booster = pendingPurchaseBooster
                        if (booster != null && booster.id != null) {
                            createDigitalProductOrder(booster.id)
                        } else {
                            pendingPurchaseBooster = null
                        }
                        purchaseSuccessEvent.value = result.purchase
                    }
                    is BillingPurchaseResult.UserCanceled -> {
                        purchaseCancelEvent.value = Unit
                        pendingPurchaseBooster = null
                    }
                    is BillingPurchaseResult.Error -> {
                        purchaseErrorEvent.value = result.debugMessage
                        pendingPurchaseBooster = null
                    }
                }
            }
        }
    }

    private fun fetchProductList() {
        val ids = cachedBoosterProductIds
        if (ids.isEmpty()) return

        val combinedDetails = mutableListOf<ProductDetails>()

        // Query SUBS first, then INAPP — combine results
        billingManager.queryProductDetails(
            productIds = ids,
            productType = BillingClient.ProductType.SUBS
        ) { billingResultSubs, detailsSubs ->
            if (billingResultSubs.responseCode == BillingClient.BillingResponseCode.OK) {
                combinedDetails.addAll(detailsSubs)
            }

            billingManager.queryProductDetails(
                productIds = ids,
                productType = BillingClient.ProductType.INAPP
            ) { billingResultInApp, detailsInApp ->
                if (billingResultInApp.responseCode == BillingClient.BillingResponseCode.OK) {
                    combinedDetails.addAll(detailsInApp)
                }

                playStoreProducts = combinedDetails
                _playStoreProductsLiveData.postValue(combinedDetails)
            }
        }
    }

    fun fetchBoosters() {
        viewModelScope.launch {
            shopRepository.getAllBooster().collect { resource ->
                if (resource.status == com.humotron.app.data.network.Status.SUCCESS) {
                    val boosters = resource.data?.data?.booster ?: emptyList()
                    _boostersLiveData.value = Resource.success(boosters)

                    // Extract product IDs dynamically from API and query Play Store
                    val ids = boosters.mapNotNull { it.playStoreProductId.ifEmpty { null } }
                    if (ids.isNotEmpty() && ids != cachedBoosterProductIds) {
                        cachedBoosterProductIds = ids
                        if (billingManager.isReady.value) {
                            fetchProductList()
                        }
                    }
                } else if (resource.status == com.humotron.app.data.network.Status.ERROR || resource.status == com.humotron.app.data.network.Status.EXCEPTION) {
                    val err = resource.error ?: com.humotron.app.data.network.error.Error(errorMessage = "Unknown error occurred")
                    _boostersLiveData.value = if (resource.status == com.humotron.app.data.network.Status.EXCEPTION) {
                        Resource.exception(err)
                    } else {
                        Resource.error(err)
                    }
                } else {
                    _boostersLiveData.value = Resource.loading()
                }
            }
        }
    }

    fun fetchBoosterById(id: String) {
        viewModelScope.launch {
            shopRepository.getBoosterById(id).collect { resource ->
                _boosterDetailLiveData.value = resource
            }
        }
    }

    fun createDigitalProductOrder(boosterId: String) {
        viewModelScope.launch {
            shopRepository.createDigitalProductOrder(boosterId).collect { resource ->
                _orderResultLiveData.value = resource
                // Handle states upon final confirmation from the backend API
                if (resource.status == com.humotron.app.data.network.Status.SUCCESS) {
                    pendingPurchaseBooster = null
                    fetchBoosters()
                } else if (resource.status == com.humotron.app.data.network.Status.ERROR || resource.status == com.humotron.app.data.network.Status.EXCEPTION) {
                    pendingPurchaseBooster = null
                    fetchBoosters()
                }
            }
        }
    }

    var isBillingFlowActive: Boolean = false
        private set

    fun clearBillingFlowActive() {
        isBillingFlowActive = false
    }

    fun isPurchaseInProgress(): Boolean {
        return pendingPurchaseBooster != null
    }

    fun getProductDetailsForId(productId: String): ProductDetails? {
        return playStoreProducts.find { it.productId == productId }
    }

    fun refreshPurchases() {
        billingManager.queryActivePurchases()
    }

    fun launchBillingFlow(activity: Activity, booster: BoosterResponse.Booster, productDetails: ProductDetails) {
        pendingPurchaseBooster = booster
        isBillingFlowActive = true
        billingManager.launchBillingFlow(activity, productDetails)
    }
}
