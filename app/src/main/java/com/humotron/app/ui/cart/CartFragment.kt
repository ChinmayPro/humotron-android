package com.humotron.app.ui.cart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.databinding.FragmentCartBinding
import com.humotron.app.ui.cart.adapter.CartAdapter
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.ui.dialogs.DeleteConfirmationBottomSheet
import com.humotron.app.ui.profile.dialog.EnterCodeBottomSheet
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.humotron.app.domain.modal.response.CreatePaymentIntentResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : BaseFragment(R.layout.fragment_cart) {

    private lateinit var binding: FragmentCartBinding
    private val viewModel: CartViewModel by viewModels()
    private val cartAdapter by lazy { CartAdapter() }
    private lateinit var paymentSheet: PaymentSheet
    
    private var itemIdBeingDeleted: String? = null
    private var selectedDeliveryMethod: GetCartResponse.DeliveryMethod? = null
    private var finalPayableAmount: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCartBinding.bind(view)

        setupAdapter()
        setupObservers()
        initViews()
        setupBottomBar()
        
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        binding.layoutLoader.tvLoadingMessage.text = getString(R.string.loading)
        viewModel.fetchCart()
    }

    private fun setupBottomBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.flCheckout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + dpToPx(20)
            }
            insets
        }
    }

    private fun setupAdapter() {
        binding.rvCartItems.adapter = cartAdapter
        
        cartAdapter.onQuantityChanged = { item, newQuantity, action ->
            item.id?.let { 
                cartAdapter.setLoading(it, true, action)
                viewModel.editCartQty(it, newQuantity) 
            }
        }

        cartAdapter.onDeleteClicked = { item ->
            showDeleteConfirmationDialog(item)
        }

        cartAdapter.onEditClicked = { item ->
            val bundle = Bundle().apply {
                putString("deviceId", item.productDetails?.productId)
                putString("cartItemId", item.id)
                putInt("quantity", item.quantity ?: 1)
                putString("variantId", item.variantDetails?.variantId)
            }
            findNavController().navigate(R.id.action_fragmentCart_to_fragmentShopBuyNow, bundle)
        }
    }

    private fun showDeleteConfirmationDialog(item: GetCartResponse.CartItem) {
        val bottomSheet = DeleteConfirmationBottomSheet.newInstance {
            itemIdBeingDeleted = item.id
            item.id?.let { viewModel.deleteCartItem(it) }
        }
        bottomSheet.show(childFragmentManager, DeleteConfirmationBottomSheet.TAG)
    }

    private fun setupObservers() {
        viewModel.getCartLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    val cartItems = resource.data?.data?.cart ?: emptyList()
                    
                    if (cartItems.isEmpty()) {
                        showEmptyState()
                    } else {
                        showCartItems(resource.data?.data)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideLoading()
                    showEmptyState()
                }
                Status.LOADING -> {
                    if (cartAdapter.itemCount == 0) {
                        showLoading()
                    }
                }
            }
        }

        viewModel.getDeleteCartItemLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    itemIdBeingDeleted?.let { id ->
                        cartAdapter.removeItem(id)
                        updateCheckoutButtonState()
                    }
                    itemIdBeingDeleted = null
                    
                    viewModel.fetchCart()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    itemIdBeingDeleted = null
                }
                Status.LOADING -> {
                }
            }
        }

        viewModel.getEditCartQtyLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    viewModel.fetchCart()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    cartAdapter.clearLoadingStates()
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.fetchCart()
                }
                Status.LOADING -> {
                }
            }
        }

        viewModel.getRemovePromoCodeLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    viewModel.fetchCart()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                }
            }
        }

        viewModel.getUpdateUserLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    viewModel.fetchCart()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                }
            }
        }

        viewModel.getPlaceOrderLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    // Handled internally in ViewModel
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideCheckoutLoading()
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    showCheckoutLoading()
                }
            }
        }

        viewModel.getCreatePaymentIntentLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.data?.let { data ->
                        presentPaymentSheet(data)
                        // Delay hiding loader to ensure payment sheet has time to appear
                        binding.btnCheckout.postDelayed({
                            hideCheckoutLoading()
                        }, 2000)
                    } ?: hideCheckoutLoading()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideCheckoutLoading()
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    showCheckoutLoading()
                }
            }
        }
    }

    private fun showCheckoutLoading() {
        binding.btnCheckout.text = ""
        binding.btnCheckout.isEnabled = false
        binding.pbCheckout.visibility = View.VISIBLE
    }

    private fun hideCheckoutLoading() {
        binding.pbCheckout.visibility = View.GONE
        binding.btnCheckout.text = getString(R.string.confirm_order)
        binding.btnCheckout.isEnabled = true
    }

    private fun getErrorMessage(error: com.humotron.app.data.network.error.Error?): String {
        if (error == null) return getString(R.string.something_went_wrong)

        if (!error.errorMessage.isNullOrEmpty()) return error.errorMessage

        val rawError = error.error
        if (!rawError.isNullOrEmpty()) {
            return try {
                val json = org.json.JSONObject(rawError)
                when {
                    json.has("message") -> json.getString("message")
                    json.has("error") -> json.getString("error")
                    else -> rawError
                }
            } catch (e: Exception) {
                rawError
            }
        }

        return getString(R.string.something_went_wrong)
    }

    private fun showLoading() {
        binding.layoutLoader.root.visibility = View.VISIBLE
        binding.tvOrderDetailsLabel.visibility = View.GONE
        binding.rvCartItems.visibility = View.GONE
        binding.clEmptyCart.visibility = View.GONE
        binding.flCheckout.visibility = View.GONE
        binding.bottomShadow.visibility = View.GONE
        binding.tvNoData.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.layoutLoader.root.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.tvNoData.visibility = View.GONE
        binding.clEmptyCart.visibility = View.VISIBLE
        
        binding.tvOrderDetailsLabel.visibility = View.VISIBLE
        binding.rvCartItems.visibility = View.GONE
        binding.flCheckout.visibility = View.VISIBLE
        binding.bottomShadow.visibility = View.VISIBLE
        binding.llCartSummary.visibility = View.VISIBLE
        
        // Reset totals to zero
        bindDetailedBill(null)
        bindTotal(null)
        bindCoupon(null)
        bindAddress(null)
        bindShippingMethod(null)
        binding.llAddressActions.visibility = View.GONE
        updateCheckoutButtonState()
    }

    private fun showCartItems(data: GetCartResponse.Data?) {
        val items = data?.cart ?: emptyList()
        binding.tvNoData.visibility = View.GONE
        binding.clEmptyCart.visibility = View.GONE
        binding.tvOrderDetailsLabel.visibility = View.VISIBLE
        binding.rvCartItems.visibility = View.VISIBLE
        binding.flCheckout.visibility = View.VISIBLE
        binding.bottomShadow.visibility = View.VISIBLE
        binding.llCartSummary.visibility = View.VISIBLE
        binding.llAddressActions.visibility = View.VISIBLE
        
        cartAdapter.setItems(items, data?.couponDetails?.promoCode)

        bindAddress(data?.address)
        bindShippingMethod(selectedDeliveryMethod)
        bindDetailedBill(data)
        bindTotal(data)
        bindCoupon(data)

        updateCheckoutButtonState()
    }

    private fun bindCoupon(data: GetCartResponse.Data?) {
        val coupon = data?.couponDetails
        if (coupon != null && !coupon.promoCode.isNullOrEmpty()) {
            binding.tvApplyCouponLabel.visibility = View.GONE
            binding.ivApplyCoupon.visibility = View.GONE
            binding.llCouponApplied.visibility = View.VISIBLE
            binding.ivRemoveCoupon.visibility = View.VISIBLE
            
            val discount = coupon.discountValue ?: 0.0
            binding.tvCouponDiscount.text = getString(R.string.discount_text_format, getString(R.string.currency_symbol), discount)
            binding.tvCouponCode.text = coupon.promoCode
        } else {
            binding.tvApplyCouponLabel.visibility = View.VISIBLE
            binding.ivApplyCoupon.visibility = View.VISIBLE
            binding.llCouponApplied.visibility = View.GONE
            binding.ivRemoveCoupon.visibility = View.GONE
        }
    }
    private fun calculateItemPrice(data: GetCartResponse.Data?): Double {
        var total = 0.0
        data?.cart?.forEach { item ->
            total += (item.totalAmount ?: 0.0)
        }
        return total
    }

    private fun bindTotal(data: GetCartResponse.Data?) {
        val itemPrice = if (data != null) calculateItemPrice(data) else 0.0
        val vat = data?.totalVAT ?: 0.0
        val deliveryPrice = selectedDeliveryMethod?.price ?: 0.0
        val couponDiscount = data?.couponDetails?.discountValue ?: 0.0
        
        val finalTotal = itemPrice + vat + deliveryPrice - couponDiscount
        finalPayableAmount = finalTotal
        
        binding.tvTotalAmount.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), finalTotal)
    }

    private fun bindDetailedBill(data: GetCartResponse.Data?) {
        val itemPrice = if (data != null) calculateItemPrice(data) else 0.0
        val vat = data?.totalVAT ?: 0.0
        val deliveryPrice = selectedDeliveryMethod?.price ?: 0.0
        val couponDiscount = data?.couponDetails?.discountValue ?: 0.0
        
        binding.tvDetailedItemPrice.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), itemPrice)
        
        if (selectedDeliveryMethod != null) {
            binding.tvDetailedDeliveryLabel.text = selectedDeliveryMethod?.methodName
            val price = selectedDeliveryMethod?.price ?: 0.0
            if (price == 0.0) {
                binding.tvDetailedDeliveryPrice.text = getString(R.string.free)
            } else {
                binding.tvDetailedDeliveryPrice.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), price)
            }
        } else {
            binding.tvDetailedDeliveryLabel.text = getString(R.string.delivery_charges)
            binding.tvDetailedDeliveryPrice.text = getString(R.string.free)
        }
        
        binding.tvDetailedCouponDiscount.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), couponDiscount)
        binding.tvDetailedVatValue.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), vat)
    }

    private fun bindAddress(address: GetCartResponse.Address?) {
        if (address != null) {
            binding.tvShippingAddressLabel.visibility = View.GONE
            binding.ivAddAddress.visibility = View.GONE
            binding.llAddressSelected.visibility = View.VISIBLE
            
            binding.tvAddressName.text = "${address.firstName} ${address.lastName}"
            binding.tvAddressPhone.text = address.contactNo
            
            val addressParts = mutableListOf<String>()
            address.address1?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.address2?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.address3?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.city?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.country?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.postcode?.let { if (it.isNotEmpty()) addressParts.add(it) }
            
            binding.tvAddressDetails.text = addressParts.joinToString(", ")
        } else {
            binding.tvShippingAddressLabel.visibility = View.VISIBLE
            binding.ivAddAddress.visibility = View.VISIBLE
            binding.llAddressSelected.visibility = View.GONE
        }
        updateCheckoutButtonState()
    }

    private fun bindShippingMethod(method: GetCartResponse.DeliveryMethod?) {
        selectedDeliveryMethod = method
        viewModel.setSelectedDeliveryMethod(method)
        if (method != null) {
            binding.tvShippingMethodLabel.visibility = View.GONE
            binding.ivAddShipping.visibility = View.GONE
            binding.llShippingApplied.visibility = View.VISIBLE
            
            val daysRange = if (method.minDays != null && method.maxDays != null) "(${method.minDays}-${method.maxDays} Days)" else ""
            binding.tvSelectedShippingName.text = "${method.methodName}\n$daysRange"
            
            // Format price: remove trailing .0 if present to match screenshot £5.9, or show Free if 0
            val price = method.price ?: 0.0
            if (price == 0.0) {
                binding.tvSelectedShippingPrice.text = getString(R.string.free)
            } else {
                val priceText = if (price % 1.0 == 0.0) price.toInt().toString() else price.toString()
                binding.tvSelectedShippingPrice.text = "${getString(R.string.currency_symbol)}$priceText"
            }
            
            binding.tvSelectedShippingDelivery.text = "${getString(R.string.expected_delivery_colon)}\n${method.estimatedDelivery ?: ""}"
        } else {
            binding.tvShippingMethodLabel.visibility = View.VISIBLE
            binding.ivAddShipping.visibility = View.VISIBLE
            binding.llShippingApplied.visibility = View.GONE
        }
        updateCheckoutButtonState()
    }

    private fun updateCheckoutButtonState() {
        val hasItems = cartAdapter.itemCount > 0
        val hasShippingMethod = selectedDeliveryMethod != null
        val hasAddress = binding.llAddressSelected.visibility == View.VISIBLE
        binding.btnCheckout.isEnabled = hasItems && hasShippingMethod && hasAddress
    }

    private fun initViews() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.header.title.text = getString(R.string.review_details)

        binding.ivApplyCoupon.setOnClickListener {
            val currentCoupon = viewModel.getCartLiveData().value?.data?.data?.couponDetails?.promoCode
            val bottomSheet = EnterCodeBottomSheet.forCoupon(
                currentCoupon = currentCoupon,
                onCouponApplied = {
                    com.humotron.app.ui.profile.dialog.CouponSuccessDialog.newInstance().show(childFragmentManager, com.humotron.app.ui.profile.dialog.CouponSuccessDialog.TAG)
                    viewModel.fetchCart()
                },
                onSave = {
                    // This is handled by onCouponApplied
                }
            )
            bottomSheet.show(childFragmentManager, EnterCodeBottomSheet.TAG)
        }

        binding.ivRemoveCoupon.setOnClickListener {
            val couponId = viewModel.getCartLiveData().value?.data?.data?.couponDetails?.id ?: ""
            viewModel.removePromoCode(couponId)
        }

        binding.ivAddAddress.setOnClickListener {
            val enterAddressBottomSheet = com.humotron.app.ui.shop.dialog.EnterAddressBottomSheet.newInstance()
            enterAddressBottomSheet.show(childFragmentManager, com.humotron.app.ui.shop.dialog.EnterAddressBottomSheet::class.java.simpleName)
        }

        binding.clAddress.setOnClickListener {
            if (binding.llAddressSelected.visibility == View.GONE) {
                binding.ivAddAddress.performClick()
            }
        }

        binding.btnAddNewAddress.setOnClickListener {
            binding.ivAddAddress.performClick()
        }

        binding.btnChangeAddress.setOnClickListener {
            val currentAddressId = viewModel.getCartLiveData().value?.data?.data?.address?.id
            val selectAddressBottomSheet = com.humotron.app.ui.shop.dialog.SelectAddressBottomSheet.newInstance(currentAddressId) { selectedAddress ->
                // Update UI locally first for instant feedback
                bindAddress(selectedAddress)
                
                val userId = prefUtils.getLoginResponse().id ?: ""
                val map = HashMap<String, Any>()
                map["addressId"] = selectedAddress.id ?: ""
                viewModel.updateUser(userId, map)
            }
            selectAddressBottomSheet.show(childFragmentManager, com.humotron.app.ui.shop.dialog.SelectAddressBottomSheet::class.java.simpleName)
        }

        binding.clShippingMethod.setOnClickListener {
            if (binding.llShippingApplied.visibility == View.GONE) {
                binding.ivAddShipping.performClick()
            }
        }

        binding.ivAddShipping.setOnClickListener {
            val deliveryMethods = viewModel.getCartLiveData().value?.data?.data?.deliveryMethods ?: emptyList()
            val currentMethodId = selectedDeliveryMethod?.id
            
            if (deliveryMethods.isNotEmpty()) {
                val bottomSheet = com.humotron.app.ui.profile.dialog.SelectDeliveryBottomSheet(
                    currentMethodId,
                    deliveryMethods
                ) { selectedMethod: com.humotron.app.domain.modal.response.GetCartResponse.DeliveryMethod ->
                    // Update UI locally
                    bindShippingMethod(selectedMethod)
                    
                    // Also update detailed bill and total locally
                    val currentData = viewModel.getCartLiveData().value?.data?.data
                    bindDetailedBill(currentData)
                    bindTotal(currentData)
                    
                    val userId = prefUtils.getLoginResponse().id ?: ""
                    val map = HashMap<String, Any>()
                    map["deliveryMethodId"] = selectedMethod.id ?: ""
                    viewModel.updateUser(userId, map)
                }
                bottomSheet.show(childFragmentManager, com.humotron.app.ui.profile.dialog.SelectDeliveryBottomSheet.TAG)
            }
        }

        binding.clCoupon.setOnClickListener {
            if (binding.llCouponApplied.visibility == View.GONE) {
                binding.ivApplyCoupon.performClick()
            }
        }

        binding.btnViewDetailedBill.setOnClickListener {
            if (binding.llDetailedBill.visibility == View.VISIBLE) {
                binding.llDetailedBill.visibility = View.GONE
                binding.ivViewDetailedBill.rotation = 0f
            } else {
                binding.llDetailedBill.visibility = View.VISIBLE
                binding.ivViewDetailedBill.rotation = 180f
            }
        }

        binding.btnCheckout.setOnClickListener {
            viewModel.startCheckout(finalPayableAmount)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun presentPaymentSheet(data: CreatePaymentIntentResponse.Data) {
        val publishableKey = data.publishableKey ?: run {
            hideCheckoutLoading()
            return
        }
        PaymentConfiguration.init(requireContext(), publishableKey)

        val customerConfig = PaymentSheet.CustomerConfiguration(
            id = data.customerId ?: "",
            ephemeralKeySecret = data.ephemeralKey ?: ""
        )

        paymentSheet.presentWithPaymentIntent(
            data.clientSecret ?: "",
            PaymentSheet.Configuration(
                merchantDisplayName = getString(R.string.app_name),
                customer = customerConfig,
                allowsDelayedPaymentMethods = true
            )
        )
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        hideCheckoutLoading()
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                android.widget.Toast.makeText(requireContext(), getString(R.string.payment_successful), android.widget.Toast.LENGTH_SHORT).show()
                
                // Navigate back to home or refresh cart
                findNavController().popBackStack()
            }
            is PaymentSheetResult.Canceled -> {
                android.widget.Toast.makeText(requireContext(), getString(R.string.payment_canceled), android.widget.Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                val error = paymentSheetResult.error.message ?: getString(R.string.something_went_wrong)
                android.widget.Toast.makeText(requireContext(), getString(R.string.payment_failed, error), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
