package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentVerifyBookingBinding
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.param.BookingDetails
import com.humotron.app.domain.modal.response.BookingTypeResponse
import com.humotron.app.domain.modal.response.GetCartResponse
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class VerifyBookingFragment : BaseFragment(R.layout.fragment_verify_booking) {

    private lateinit var binding: FragmentVerifyBookingBinding
    private val viewModel: ShopViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVerifyBookingBinding.bind(view)

        setupInsets()
        initViews()
        initObservers()
        displayBookingData()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            binding.btnConfirmBooking.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + (24 * resources.displayMetrics.density).toInt()
            }
            
            insets
        }
    }

    private fun initViews() {
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivEditTime.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentChooseDateTime, false)
        }

        binding.ivEditAddress.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentSelectAddress, false)
        }

        binding.btnConfirmBooking.setOnClickListener {
            confirmBooking()
        }
    }

    private fun initObservers() {
        viewModel.getCreateBookCartLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.layoutLoader.root.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    val navOptions = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.fragmentShop, true)
                        .build()
                    findNavController().navigate(R.id.fragmentCart, null, navOptions)
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmBooking() {
        val type = viewModel.getSelectedBookingType()
        val address = viewModel.getSelectedAddress()
        val lab = viewModel.getSelectedLab()
        val date = viewModel.getSelectedDate()
        val time = viewModel.getSelectedTime()

        val (productType, bookingType) = when (type?.title) {
            "At-Home Service" -> "blood_home" to "homeVisit"
            "Self- Collection kit" -> "blood_self" to "selfCollection"
            "Lab visit" -> "blood_lab" to "labVisit"
            else -> "blood_home" to "homeVisit"
        }

        if (isBookingDataInvalid(type, date, time, bookingType, address, lab)) {
            android.widget.Toast.makeText(requireContext(), getString(R.string.please_select_details), android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date!!.time)
        val formattedTime = formatTime(time ?: "")

        val bookingDetails = when (bookingType) {
            "selfCollection" -> BookingDetails(date = null, time = null, addressId = address?.id)
            "labVisit" -> BookingDetails(date = formattedDate, time = formattedTime, labId = lab?.id)
            else -> BookingDetails(date = formattedDate, time = formattedTime, addressId = address?.id)
        }

        val param = AddToCartParam(
            productId = type!!.id,
            productType = productType,
            bookingType = bookingType,
            quantity = 1,
            bookingDetails = bookingDetails
        )

        viewModel.createBookCart(param)
    }

    private fun isBookingDataInvalid(
        type: BookingTypeResponse.BookingType?,
        date: java.util.Calendar?,
        time: String?,
        bookingType: String,
        address: GetCartResponse.Address?,
        lab: com.humotron.app.domain.modal.response.GetAllLabResponse.Lab?
    ): Boolean {
        return type == null || date == null || time == null || 
               (bookingType != "labVisit" && address == null) || 
               (bookingType == "labVisit" && lab == null)
    }

    private fun displayBookingData() {
        val type = viewModel.getSelectedBookingType()
        val date = viewModel.getSelectedDate()
        val time = viewModel.getSelectedTime()

        // Display Test Details
        type?.let {
            binding.tvBookingTypeTitle.text = it.title
            binding.tvBookingTypeDesc.text = it.description
            binding.tvPrice.text = getString(R.string.price_format, it.currency ?: "$", it.price)
            binding.tvAddressLabel.text = if (it.title == "Lab visit") getString(R.string.selected_lab) else getString(R.string.address_label)
        }

        // Display Date & Time
        if (date != null && time != null) {
            val sdf = SimpleDateFormat("EEEE, MMM dd' 'yyyy", Locale.getDefault())
            binding.tvSelectedDateTime.text = "${sdf.format(date.time)} | ${formatTime(time)}"
        }

        // Display Address or Lab
        if (type?.title == "Lab visit") {
            // Style to match Lab List Item design
            val padding = (16 * resources.displayMetrics.density).toInt()
            binding.cvAddress.setContentPadding(padding, padding, padding, padding)
            binding.cvAddress.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.surface_dark))
            binding.cvAddress.strokeColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.stroke_card)
            binding.cvAddress.strokeWidth = (1 * resources.displayMetrics.density).toInt()
            binding.cvAddress.cardElevation = (0 * resources.displayMetrics.density)
            binding.ivEditAddress.visibility = android.view.View.GONE
            
            viewModel.getSelectedLab()?.let { lab ->
                val labAddress = lab.address
                val addressParts = listOfNotNull(
                    lab.labName,
                    labAddress?.line1,
                    labAddress?.line2,
                    labAddress?.city,
                    labAddress?.postcode,
                    labAddress?.country
                ).filter { it.isNullOrBlank().not() }
                binding.tvAddressDetails.text = addressParts.joinToString("\n")
            }
        } else {
            // Style as Plain View for others (Home visit, etc.)
            binding.cvAddress.setContentPadding(0, 0, 0, 0)
            binding.cvAddress.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.cvAddress.strokeColor = android.graphics.Color.TRANSPARENT
            binding.cvAddress.strokeWidth = 0
            binding.cvAddress.cardElevation = 0f
            binding.ivEditAddress.visibility = android.view.View.VISIBLE
            
            viewModel.getSelectedAddress()?.let { address ->
                bindAddress(address)
            }
        }
    }

    private fun bindAddress(address: GetCartResponse.Address) {
        val name = getString(R.string.full_name_format, address.firstName ?: "", address.lastName ?: "").trim()
        val phone = address.contactNo ?: ""
        
        val streetAddress = listOfNotNull(address.address1, address.address2, address.address3)
            .filter { it.isNullOrBlank().not() }.joinToString(", ")
        
        val addressParts = listOfNotNull(
            name,
            if (phone.isNotBlank()) phone else null,
            if (streetAddress.isNotBlank()) streetAddress else null,
            address.postcode,
            address.city,
            address.country
        ).filter { it.isNullOrBlank().not() }
        
        binding.tvAddressDetails.text = addressParts.joinToString("\n")
    }

    private fun formatTime(time: String): String {
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12 = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = sdf24.parse(time)
            sdf12.format(date!!).lowercase()
        } catch (e: Exception) {
            time
        }
    }
}
