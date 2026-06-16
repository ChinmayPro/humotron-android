package com.humotron.app.ui.order

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentTrackOrderBinding
import com.humotron.app.domain.modal.response.GetOrderTrackingResponse
import com.humotron.app.ui.dialogs.DeleteConfirmationBottomSheet
import com.humotron.app.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@AndroidEntryPoint
class TrackOrderFragment : BaseFragment(R.layout.fragment_track_order) {

    private lateinit var binding: FragmentTrackOrderBinding
    private val viewModel: OrderViewModel by viewModels()
    private var orderNumber: String? = null
    private var orderInternalId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrackOrderBinding.bind(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        
        orderNumber = arguments?.getString("orderNumber")
        orderInternalId = arguments?.getString("orderId")

        initViews()
        initObservers()

        orderNumber?.let {
            binding.tvOrderNumber.text = "#$it"
            viewModel.fetchOrderTrackingDetails(it)
        }
    }

    private fun initViews() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.header.title.text = getString(R.string.order_timeline)
        binding.header.divider.visibility = View.GONE

        binding.btnContactSupport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentTrackOrder_to_fragmentContactSupport)
        }

        binding.tvCancelOrder.setOnClickListener {
            showCancelOrderDialog()
        }
    }

    private fun showCancelOrderDialog() {
        DeleteConfirmationBottomSheet.newInstance(
            title = getString(R.string.alert_title),
            message = getString(R.string.cancel_order_confirmation)
        ) {
            orderInternalId?.let { id ->
                viewModel.cancelOrder(id)
            }
        }.show(childFragmentManager, DeleteConfirmationBottomSheet.TAG)
    }

    private fun initObservers() {
        viewModel.getOrderTrackingLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    resource.data?.data?.let { data ->
                        if (orderInternalId.isNullOrBlank() && !data.orderId.isNullOrBlank()) {
                            orderInternalId = data.orderId // Fallback to API ID only if not passed from previous screen
                        }
                        // binding.tvOrderNumber.text is already set to the display orderNumber initially
                        binding.tvCurrentStatus.text = data.orderStatusName
                        
                        // Set background color for current status badge
                        val statusColor = parseColor(data.statusColorCode, ContextCompat.getColor(requireContext(), R.color.lime_green))
                        binding.tvCurrentStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(statusColor)
                        binding.tvCurrentStatus.setTextColor(if (data.orderStatus == "order_placed") ContextCompat.getColor(requireContext(), R.color.white) else ContextCompat.getColor(requireContext(), R.color.black))
                        
                        data.history?.let { history ->
                            updateHistoryUI(history)
                        }
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val errorMessage = resource.error?.errorMessage ?: getString(R.string.something_went_wrong)
                    ToastUtils.showShort(requireContext(), errorMessage)
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

    private fun updateHistoryUI(history: List<GetOrderTrackingResponse.HistoryEvent>) {
        // Step 1: Placed
        updateStep(
            history.find { it.status == "order_placed" },
            history.find { it.status == "payment_success" } != null,
            binding.ivStatusPlaced, null, binding.vLineBottomPlaced, binding.tvStatusDatePlaced
        )

        // Step 2: Payment
        updateStep(
            history.find { it.status == "payment_success" },
            history.find { it.status == "order_confirmed" } != null,
            binding.ivStatusPayment, binding.vLineTopPayment, binding.vLineBottomPayment, binding.tvStatusDatePayment
        )

        // Step 3: Confirmed
        updateStep(
            history.find { it.status == "order_confirmed" },
            history.find { it.status == "order_packed" } != null,
            binding.ivStatusConfirmed, binding.vLineTopConfirmed, binding.vLineBottomConfirmed, binding.tvStatusDateConfirmed
        )

        // Step 4: Packed
        updateStep(
            history.find { it.status == "order_packed" },
            history.find { it.status == "ready_for_pickup" } != null,
            binding.ivStatusPacked, binding.vLineTopPacked, binding.vLineBottomPacked, binding.tvStatusDatePacked
        )

        // Step 5: Pickup
        updateStep(
            history.find { it.status == "ready_for_pickup" },
            history.find { it.status == "courier_picked_up" } != null,
            binding.ivStatusPickup, binding.vLineTopPickup, binding.vLineBottomPickup, binding.tvStatusDatePickup
        )

        // Step 6: Courier
        updateStep(
            history.find { it.status == "courier_picked_up" },
            history.find { it.status == "out_for_delivery" } != null,
            binding.ivStatusCourier, binding.vLineTopCourier, binding.vLineBottomCourier, binding.tvStatusDateCourier
        )

        // Step 7: Delivery
        updateStep(
            history.find { it.status == "out_for_delivery" },
            history.find { it.status == "delivered" } != null,
            binding.ivStatusDelivery, binding.vLineTopDelivery, binding.vLineBottomDelivery, binding.tvStatusDateDelivery
        )

        // Step 8: Delivered
        updateStep(
            history.find { it.status == "delivered" },
            false,
            binding.ivStatusDelivered, binding.vLineTopDelivered, null, binding.tvStatusDateDelivered
        )
    }

    private fun updateStep(
        event: GetOrderTrackingResponse.HistoryEvent?,
        nextCompleted: Boolean,
        ivStatus: ImageView,
        vLineTop: View?,
        vLineBottom: View?,
        tvDate: TextView
    ) {
        val grey = ContextCompat.getColor(requireContext(), R.color.white20)
        val black = ContextCompat.getColor(requireContext(), R.color.black)
        val white = ContextCompat.getColor(requireContext(), R.color.white)

        val isCompleted = event?.isCompleted == true

        if (isCompleted) {
            ivStatus.setImageResource(R.drawable.ic_checked)
            vLineTop?.setBackgroundColor(grey)
            event?.timestamp?.let { ts ->
                tvDate.visibility = View.VISIBLE
                tvDate.text = formatTimestamp(ts)
                
                // Styling the date bubble based on status
                val bubbleColor = parseColor(event.statusColorCode, ContextCompat.getColor(requireContext(), R.color.lime_green))
                tvDate.backgroundTintList = android.content.res.ColorStateList.valueOf(bubbleColor)

            } ?: run {
                tvDate.visibility = View.INVISIBLE
            }
        } else {
            ivStatus.setImageResource(R.drawable.ic_dot_pending)
            vLineTop?.setBackgroundColor(grey)
            tvDate.visibility = View.INVISIBLE
        }

        vLineBottom?.setBackgroundColor(grey)
    }

    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("EEE, dd MMM, h:mm a", Locale.getDefault())
            val netDate = Date(timestamp * 1000L)
            sdf.format(netDate)
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseColor(colorHex: String?, defaultColor: Int): Int {
        return try {
            if (colorHex.isNullOrBlank()) defaultColor
            else android.graphics.Color.parseColor(colorHex)
        } catch (e: Exception) {
            defaultColor
        }
    }
}
