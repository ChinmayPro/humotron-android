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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : BaseFragment(R.layout.fragment_cart) {

    private lateinit var binding: FragmentCartBinding
    private val viewModel: CartViewModel by viewModels()
    private val cartAdapter by lazy { CartAdapter() }
    
    // Track the item being deleted for smooth removal
    private var itemIdBeingDeleted: String? = null
    private var selectedDeliveryMethod: com.humotron.app.domain.modal.response.GetCartResponse.DeliveryMethod? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCartBinding.bind(view)

        setupAdapter()
        setupObservers()
        initViews()
        setupBottomBar()

        binding.layoutLoader.tvLoadingMessage.text = getString(R.string.loading)
        viewModel.fetchCart()
    }

    private fun setupBottomBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnCheckout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // Base margin (20dp) + dynamic navigation bar height
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
                    // Smoothly remove the item from the adapter first
                    itemIdBeingDeleted?.let { id ->
                        cartAdapter.removeItem(id)
                    }
                    itemIdBeingDeleted = null
                    
                    // Refresh cart in background to update totals without blocking the UI
                    viewModel.fetchCart()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    itemIdBeingDeleted = null
                    // Optionally show error message
                }
                Status.LOADING -> {
                    // Optional: show loading overlay during delete
                }
            }
        }

        viewModel.getEditCartQtyLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    // Item loading state will be cleared by fetchCart -> showCartItems -> setItems
                    // But for immediate feedback, we can clear it here if we want
                    viewModel.fetchCart()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    cartAdapter.clearLoadingStates()
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.fetchCart() // Refresh to revert UI to server state
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
                    // Don't call fetchCart here, update is already done locally in callback
                }
                Status.ERROR, Status.EXCEPTION -> {
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                }
            }
        }
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
        binding.rvCartItems.visibility = View.GONE
        binding.tvOrderDetailsLabel.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE
        binding.bottomShadow.visibility = View.GONE
        binding.tvNoData.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.layoutLoader.root.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.tvOrderDetailsLabel.visibility = View.GONE
        binding.rvCartItems.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE
        binding.bottomShadow.visibility = View.GONE
        binding.llCartSummary.visibility = View.GONE
        binding.tvNoData.visibility = View.VISIBLE
    }

    private fun showCartItems(data: com.humotron.app.domain.modal.response.GetCartResponse.Data?) {
        val items = data?.cart ?: emptyList()
        binding.tvNoData.visibility = View.GONE
        binding.tvOrderDetailsLabel.visibility = View.VISIBLE
        binding.rvCartItems.visibility = View.VISIBLE
        binding.btnCheckout.visibility = View.VISIBLE
        binding.bottomShadow.visibility = View.VISIBLE
        binding.llCartSummary.visibility = View.VISIBLE
        
        // Pass the updated list to adapter
        cartAdapter.setItems(items, data?.couponDetails?.promoCode)

        // Bind Address
        bindAddress(data?.address)

        // Bind Shipping Method
        // Note: selectedDeliveryMethod is tracked locally
        bindShippingMethod(selectedDeliveryMethod)

        // Bind Detailed Bill
        bindDetailedBill(data)

        // Bind Total
        bindTotal(data)

        // Bind Coupon
        bindCoupon(data)
    }

    private fun bindCoupon(data: com.humotron.app.domain.modal.response.GetCartResponse.Data?) {
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
    private fun calculateItemPrice(data: com.humotron.app.domain.modal.response.GetCartResponse.Data?): Double {
        var total = 0.0
        data?.cart?.forEach { item ->
            total += (item.totalAmount ?: 0.0)
        }
        return total
    }

    private fun bindTotal(data: com.humotron.app.domain.modal.response.GetCartResponse.Data?) {
        if (data == null) return
        
        val itemPrice = calculateItemPrice(data)
        val vat = data.totalVAT ?: 0.0
        val deliveryPrice = selectedDeliveryMethod?.price ?: 0.0
        val couponDiscount = data.couponDetails?.discountValue ?: 0.0
        
        val finalTotal = itemPrice + vat + deliveryPrice - couponDiscount
        
        binding.tvTotalAmount.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), finalTotal)
    }

    private fun bindDetailedBill(data: com.humotron.app.domain.modal.response.GetCartResponse.Data?) {
        if (data == null) return

        val itemPrice = calculateItemPrice(data)
        val vat = data.totalVAT ?: 0.0
        val deliveryPrice = selectedDeliveryMethod?.price ?: 0.0
        val couponDiscount = data.couponDetails?.discountValue ?: 0.0
        
        binding.tvDetailedItemPrice.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), itemPrice)
        
        if (selectedDeliveryMethod != null) {
            binding.tvDetailedDeliveryLabel.text = selectedDeliveryMethod?.methodName
            binding.tvDetailedDeliveryPrice.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), selectedDeliveryMethod?.price ?: 0.0)
        } else {
            binding.tvDetailedDeliveryLabel.text = getString(R.string.delivery_charges)
            binding.tvDetailedDeliveryPrice.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), 0.0)
        }
        
        binding.tvDetailedCouponDiscount.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), couponDiscount)
        binding.tvDetailedVatValue.text = getString(R.string.price_format_decimal, getString(R.string.currency_symbol), vat)
    }

    private fun bindAddress(address: com.humotron.app.domain.modal.response.GetCartResponse.Address?) {
        if (address != null) {
            binding.clAddress.visibility = View.VISIBLE
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
            binding.clAddress.visibility = View.GONE
        }
    }

    private fun bindShippingMethod(method: com.humotron.app.domain.modal.response.GetCartResponse.DeliveryMethod?) {
        selectedDeliveryMethod = method
        if (method != null) {
            binding.tvShippingMethodLabel.visibility = View.GONE
            binding.ivAddShipping.visibility = View.GONE
            binding.llShippingApplied.visibility = View.VISIBLE
            
            val daysRange = if (method.minDays != null && method.maxDays != null) "(${method.minDays}-${method.maxDays} Days)" else ""
            binding.tvSelectedShippingName.text = "${method.methodName}\n$daysRange"
            
            // Format price: remove trailing .0 if present to match screenshot £5.9
            val price = method.price ?: 0.0
            val priceText = if (price % 1.0 == 0.0) price.toInt().toString() else price.toString()
            binding.tvSelectedShippingPrice.text = "${getString(R.string.currency_symbol)}$priceText"
            
            binding.tvSelectedShippingDelivery.text = "${getString(R.string.expected_delivery_colon)}\n${method.estimatedDelivery ?: ""}"
        } else {
            binding.tvShippingMethodLabel.visibility = View.VISIBLE
            binding.ivAddShipping.visibility = View.VISIBLE
            binding.llShippingApplied.visibility = View.GONE
        }
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

        binding.btnAddNewAddress.setOnClickListener {
            val enterAddressBottomSheet = com.humotron.app.ui.shop.dialog.EnterAddressBottomSheet.newInstance()
            enterAddressBottomSheet.show(childFragmentManager, com.humotron.app.ui.shop.dialog.EnterAddressBottomSheet::class.java.simpleName)
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

        binding.btnViewDetailedBill.setOnClickListener {
            if (binding.llDetailedBill.visibility == View.VISIBLE) {
                binding.llDetailedBill.visibility = View.GONE
                binding.ivViewDetailedBill.rotation = 0f
            } else {
                binding.llDetailedBill.visibility = View.VISIBLE
                binding.ivViewDetailedBill.rotation = 180f
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
