package com.humotron.app.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class BillingPurchaseResult {
    data class Success(val purchase: Purchase) : BillingPurchaseResult()
    data class Error(val responseCode: Int, val debugMessage: String) : BillingPurchaseResult()
    object UserCanceled : BillingPurchaseResult()
}

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val externalScope: CoroutineScope
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private var reconnectDelay = 1000L
    private val maxReconnectDelay = 64000L

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<BillingPurchaseResult>()
    val purchaseEvents: SharedFlow<BillingPurchaseResult> = _purchaseEvents.asSharedFlow()

    private val _activePurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val activePurchases: StateFlow<List<Purchase>> = _activePurchases.asStateFlow()

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        startConnection()
    }

    fun startConnection() {
        val client = billingClient ?: return
        if (client.isReady) {
            _isReady.value = true
            return
        }

        Timber.d("Starting Google Play Billing connection...")
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.i("Google Play Billing successfully connected.")
                    _isReady.value = true
                    reconnectDelay = 1000L
                    queryActivePurchases()
                } else {
                    Timber.w("Billing setup failed: %s", billingResult.debugMessage)
                    _isReady.value = false
                    retryConnection()
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing service disconnected. Attempting to reconnect...")
                _isReady.value = false
                retryConnection()
            }
        })
    }

    private fun retryConnection() {
        externalScope.launch(Dispatchers.IO) {
            delay(reconnectDelay)
            reconnectDelay = (reconnectDelay * 2).coerceAtMost(maxReconnectDelay)
            Timber.d("Retrying Play Store connection after %d ms", reconnectDelay)
            startConnection()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.i("User canceled purchase process.")
                externalScope.launch {
                    _purchaseEvents.emit(BillingPurchaseResult.UserCanceled)
                }
            }
            else -> {
                Timber.e("Purchase failed: %s (%d)", billingResult.debugMessage, billingResult.responseCode)
                externalScope.launch {
                    _purchaseEvents.emit(
                        BillingPurchaseResult.Error(
                            billingResult.responseCode,
                            billingResult.debugMessage
                        )
                    )
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(acknowledgeParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.i("Purchase successfully acknowledged: %s", purchase.products.joinToString())
                        queryActivePurchases()
                        externalScope.launch {
                            _purchaseEvents.emit(BillingPurchaseResult.Success(purchase))
                        }
                    } else {
                        Timber.e("Failed to acknowledge purchase: %s", billingResult.debugMessage)
                        externalScope.launch {
                            _purchaseEvents.emit(
                                BillingPurchaseResult.Error(
                                    billingResult.responseCode,
                                    billingResult.debugMessage
                                )
                            )
                        }
                    }
                }
            } else {
                Timber.d("Purchase already acknowledged: %s", purchase.products.joinToString())
                externalScope.launch {
                    _purchaseEvents.emit(BillingPurchaseResult.Success(purchase))
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Timber.i("Purchase is pending: %s", purchase.products.joinToString())
        }
    }

    fun queryProductDetails(
        productIds: List<String>,
        productType: String,
        onResult: (BillingResult, List<ProductDetails>) -> Unit
    ) {
        val client = billingClient
        if (client == null || !client.isReady) {
            Timber.w("Billing client is not ready.")
            onResult(
                BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                    .setDebugMessage("Billing client is not ready.")
                    .build(),
                emptyList()
            )
            return
        }

        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            onResult(billingResult, productDetailsList)
        }
    }

    fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String? = null
    ): BillingResult {
        val client = billingClient
        if (client == null || !client.isReady) {
            Timber.w("Billing client is not ready to launch flow.")
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                .setDebugMessage("Billing client is not ready.")
                .build()
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        return client.launchBillingFlow(activity, billingFlowParams)
    }

    fun queryActivePurchases() {
        val client = billingClient
        if (client == null || !client.isReady) {
            return
        }

        val allPurchases = mutableListOf<Purchase>()

        fun queryPurchasesOfType(type: String, onFinished: () -> Unit) {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(type)
                .build()

            client.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    allPurchases.addAll(purchases)
                }
                onFinished()
            }
        }

        queryPurchasesOfType(BillingClient.ProductType.SUBS) {
            queryPurchasesOfType(BillingClient.ProductType.INAPP) {
                _activePurchases.value = allPurchases
                Timber.d("Active purchases updated: %d items", allPurchases.size)
            }
        }
    }
}
