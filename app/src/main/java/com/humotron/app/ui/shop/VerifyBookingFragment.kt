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
        val date = viewModel.getSelectedDate()
        val time = viewModel.getSelectedTime()

        if (type == null || address == null || date == null || time == null) {
            android.widget.Toast.makeText(requireContext(), "Please select all details", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Format Date: yyyy-MM-dd
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = sdfDate.format(date.time)

        // Format Time: hh:mm a
        val sdfTime24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfTime12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = try {
            val dateObj = sdfTime24.parse(time)
            sdfTime12.format(dateObj!!).uppercase()
        } catch (e: Exception) {
            time
        }

        val bookingDetails = BookingDetails(
            date = formattedDate,
            time = formattedTime,
            addressId = address.id
        )

        val param = AddToCartParam(
            cartItemId = "",
            productId = type.id ?: "69cca8017deeffebfbacc07d",
            productType = "expert_review",
            quantity = 1,
            variantId = "",
            bookingDetails = bookingDetails
        )

        viewModel.createBookCart(param)
    }

    private fun displayBookingData() {
        // Display Test Details
        viewModel.getSelectedBookingType()?.let { type: BookingTypeResponse.BookingType ->
            binding.tvBookingTypeTitle.text = type.title
            binding.tvBookingTypeDesc.text = type.description
            binding.tvPrice.text = getString(R.string.price_format, type.currency ?: "$", type.price)
        }

        // Display Date & Time
        val date = viewModel.getSelectedDate()
        val time = viewModel.getSelectedTime()
        if (date != null && time != null) {
            val sdf = SimpleDateFormat("EEEE, MMM dd' 'yyyy", Locale.getDefault())
            val dateStr = sdf.format(date.time)
            val timeStr = formatTime(time)
            binding.tvSelectedDateTime.text = "$dateStr | $timeStr"
        }

        // Display Address
        viewModel.getSelectedAddress()?.let { address: GetCartResponse.Address ->
            bindAddress(address)
        }
    }

    private fun bindAddress(address: GetCartResponse.Address) {
        val addressParts = mutableListOf<String>()
        
        val streetAddress = listOfNotNull(
            address.address1,
            address.address2,
            address.address3
        ).filter { it.isNotBlank() }.joinToString(", ")
        
        if (streetAddress.isNotBlank()) addressParts.add(streetAddress)
        address.postcode?.let { if (it.isNotBlank()) addressParts.add(it) }
        address.city?.let { if (it.isNotBlank()) addressParts.add(it) }
        address.country?.let { if (it.isNotBlank()) addressParts.add(it) }
        
        binding.tvAddressDetails.text = addressParts.joinToString("\n")
    }

    private fun formatTime(time: String): String {
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf24.parse(time)
            sdf12.format(date!!).uppercase()
        } catch (e: Exception) {
            time
        }
    }
}
