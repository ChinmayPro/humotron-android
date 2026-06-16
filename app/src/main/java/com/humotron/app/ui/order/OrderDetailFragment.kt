package com.humotron.app.ui.order

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentOrderDetailBinding
import com.humotron.app.domain.modal.response.GetAllOrderResponse
import com.humotron.app.ui.order.adapter.OrderItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import android.view.ViewGroup
import com.humotron.app.ui.dialogs.DeleteConfirmationBottomSheet
import com.humotron.app.util.ToastUtils

@AndroidEntryPoint
class OrderDetailFragment : BaseFragment(R.layout.fragment_order_detail) {

    private lateinit var binding: FragmentOrderDetailBinding
    private val viewModel: OrderViewModel by viewModels()
    private var order: GetAllOrderResponse.Order? = null
    private val orderItemAdapter by lazy { OrderItemAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOrderDetailBinding.bind(view)
        
        order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("order", GetAllOrderResponse.Order::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("order")
        }

        initViews()
        setupInsets()
        setupObservers()
        
        order?.id?.let { orderId ->
            viewModel.fetchOrderDetail(orderId)
        }
    }

    private fun initViews() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.header.title.text = getString(R.string.my_order)
        binding.header.divider.visibility = View.GONE

        binding.rvOrderItems.adapter = orderItemAdapter

        binding.btnTrackOrder.setOnClickListener {
            val orderNumber = order?.orderNumber
            val orderId = order?.id
            
            if (orderNumber != null && orderId != null) {
                val bundle = Bundle().apply {
                    putString("orderNumber", orderNumber)
                    putString("orderId", orderId)
                }
                findNavController().navigate(R.id.action_fragmentOrderDetail_to_fragmentTrackOrder, bundle)
            }
        }

        binding.btnCancelOrder.setOnClickListener {
            showCancelOrderDialog()
        }

        binding.btnContactSupport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentOrderDetail_to_fragmentContactSupport)
        }
    }

    private fun setupObservers() {
        viewModel.getOrderDetailLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // Show loading if needed
                }
                Status.SUCCESS -> {
                    resource.data?.data?.let { details ->
                        // Set items to adapter
                        val promoCode = details.couponDetails?.promoCode
                        details.order?.let { orderItems ->
                            orderItemAdapter.setItems(orderItems, promoCode)
                        }

                        // Set coupon details
                        if (details.couponDetails != null) {
                            binding.clCoupon.visibility = View.VISIBLE
                            binding.tvCouponCode.text = details.couponDetails.promoCode
                            
                            val discountValue = details.couponDetails.discountValue ?: 0.0
                            val discountType = details.couponDetails.discountType // e.g. "flat", "percentage"
                            
                            val discountText = if (discountType == "percentage") {
                                "${discountValue}% Discount"
                            } else {
                                "${getString(R.string.currency_symbol)}${discountValue} Discount"
                            }
                            binding.tvCouponDiscount.text = discountText
                        } else {
                            binding.clCoupon.visibility = View.GONE
                        }

                        // Set shipping details
                        if (details.address != null) {
                            binding.clAddress.visibility = View.VISIBLE
                            val addr = details.address
                            binding.tvAddressName.text = getString(R.string.full_name_format, addr.firstName ?: "", addr.lastName ?: "")
                            binding.tvAddressPhone.text = addr.contactNo ?: ""
                            
                            val addressStr = listOfNotNull(
                                addr.address1, addr.address2, addr.address3,
                                addr.city, addr.country, addr.postcode
                            ).filter { it.isNotBlank() }.joinToString(", ")
                            binding.tvAddressDetails.text = addressStr
                        } else {
                            binding.clAddress.visibility = View.GONE
                        }

                        // Set delivery methods
                        if (details.deliveryMethods != null) {
                            binding.clDeliveryMethod.visibility = View.VISIBLE
                            val dm = details.deliveryMethods
                            val methodName = dm.methodName ?: ""
                            val minDays = dm.minDays
                            val maxDays = dm.maxDays
                            val daysInfo = if (minDays != null && maxDays != null) {
                                getString(R.string.days_format, minDays, maxDays)
                            } else {
                                ""
                            }
                            binding.tvDeliveryMethodName.text = if (daysInfo.isNotBlank()) {
                                "$methodName\n$daysInfo"
                            } else {
                                methodName
                            }
                            binding.tvExpectedDeliveryValue.text = dm.estimatedDelivery ?: ""
                        } else {
                            binding.clDeliveryMethod.visibility = View.GONE
                        }

                        // Set bill details
                        binding.clBillDetail.visibility = View.VISIBLE
                        val productTotal = details.productTotalAmount ?: 0.0
                        val deliveryPrice = details.deliveryMethods?.price ?: 0.0
                        val couponAmt = details.couponAmount?.toDoubleOrNull() ?: 0.0
                        val vatAmt = details.totalVAT ?: 0.0
                        val totalPayable = details.payableAmount ?: 0.0

                        val currency = getString(R.string.currency_symbol)
                        binding.tvTotalAmount.text = String.format("%s%.2f", currency, totalPayable)
                        binding.tvDetailedItemPrice.text = String.format("%s%.2f", currency, productTotal)
                        binding.tvDetailedDeliveryPrice.text = String.format("%s%.2f", currency, deliveryPrice)
                        binding.tvDetailedCouponDiscount.text = String.format("%s%.1f", currency, couponAmt)
                        binding.tvDetailedVatValue.text = String.format("%s%.2f", currency, vatAmt)

                        val methodName = details.deliveryMethods?.methodName ?: ""
                        if (methodName.isNotBlank()) {
                            binding.tvDetailedDeliveryLabel.text = getString(R.string.charges_format_label, methodName)
                        } else {
                            binding.tvDetailedDeliveryLabel.text = getString(R.string.delivery_charges)
                        }
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    // Handle error
                }
            }
        }

        viewModel.getCancelOrderLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    val msg = resource.data?.message ?: "Order cancelled successfully"
                    ToastUtils.showShort(requireContext(), msg)
                    findNavController().getBackStackEntry(R.id.fragmentOrder).savedStateHandle.set("refresh_orders", true)
                    findNavController().popBackStack(R.id.fragmentOrder, false)
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    ToastUtils.showShort(requireContext(), resource.error?.errorMessage ?: getString(R.string.error_occurred))
                }
            }
        }
    }

    private fun showCancelOrderDialog() {
        DeleteConfirmationBottomSheet.newInstance(
            title = getString(R.string.alert_title),
            message = getString(R.string.cancel_order_confirmation)
        ) {
            order?.id?.let { orderId ->
                viewModel.cancelOrder(orderId)
            }
        }.show(childFragmentManager, DeleteConfirmationBottomSheet.TAG)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.nsvContent.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
    }
}

