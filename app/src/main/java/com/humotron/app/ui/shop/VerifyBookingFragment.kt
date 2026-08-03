package com.humotron.app.ui.shop

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentVerifyBookingBinding
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.param.BookingDetails
import com.humotron.app.domain.modal.response.BookingTypeResponse
import com.humotron.app.domain.modal.response.GetAllLabResponse
import com.humotron.app.domain.modal.response.GetCartResponse
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.humotron.app.domain.modal.response.CreatePaymentIntentResponse
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class VerifyBookingFragment : BaseFragment(R.layout.fragment_verify_booking) {

    private lateinit var binding: FragmentVerifyBookingBinding
    private val viewModel: ShopViewModel by activityViewModels()
    private lateinit var paymentSheet: PaymentSheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVerifyBookingBinding.bind(view)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        setupInsets()
        initViews()
        setupFlowStepHeader()
        displayBookingData()
        observeViewModel()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            binding.layoutFooter.updatePadding(bottom = systemBars.bottom + (16 * density).toInt())
            insets
        }
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentBookingType, false)
        }

        binding.tvEditMethod.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentBookingType, false)
        }

        binding.ivEditAddress.setOnClickListener {
            val titleLower = viewModel.getSelectedBookingType()?.title?.lowercase() ?: ""
            if (titleLower.contains("lab")) {
                findNavController().popBackStack(R.id.fragmentSelectLab, false)
            } else {
                findNavController().popBackStack(R.id.fragmentSelectAddress, false)
            }
        }

        binding.ivEditTime.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentChooseDateTime, false)
        }

        binding.btnConfirmBooking.setOnClickListener {
            confirmBooking()
        }
    }

    private fun setupFlowStepHeader() {
        val titleLower = viewModel.getSelectedBookingType()?.title?.lowercase() ?: ""
        val (totalSteps, stepIndex) = when {
            titleLower.contains("lab") -> 6 to 5
            titleLower.contains("home") -> 5 to 4
            else -> 3 to 2
        }
        updateStepProgressBar(totalSteps, stepIndex, "REVIEW & PAY")
    }

    private fun updateStepProgressBar(totalSteps: Int, currentStepIndex: Int, stepName: String) {
        binding.llProgressBar.removeAllViews()
        val density = resources.displayMetrics.density
        for (i in 0 until totalSteps) {
            val segment = View(requireContext())
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            if (i < totalSteps - 1) {
                params.marginEnd = (6 * density).toInt()
            }
            segment.layoutParams = params

            val bgDrawable = GradientDrawable().apply {
                cornerRadius = 2 * density
                if (i <= currentStepIndex) {
                    setColor(Color.parseColor("#5FB7C4"))
                } else {
                    setColor(Color.parseColor("#1AFFFFFF"))
                }
            }
            segment.background = bgDrawable
            binding.llProgressBar.addView(segment)
        }

        val stepNumber = currentStepIndex + 1
        binding.tvStepEyebrow.text = "STEP $stepNumber OF $totalSteps · $stepName"
    }

    private fun observeViewModel() {
        viewModel.getCreatePaymentIntentLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showLoadingState(true)
                }
                Status.SUCCESS -> {
                    showLoadingState(false)
                    resource.data?.data?.let { data ->
                        presentPaymentSheet(data)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    showLoadingState(false)
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.btnConfirmBooking.isEnabled = !isLoading
        binding.btnConfirmBooking.text = if (isLoading) "Processing..." else "Proceed to payment"
    }

    private fun confirmBooking() {
        val type = viewModel.getSelectedBookingType()
        val address = viewModel.getSelectedAddress() ?: createMockAddress()
        val lab = viewModel.getSelectedLab()
        val date = viewModel.getSelectedDate()
        val time = viewModel.getSelectedTime()

        val titleLower = type?.title?.lowercase() ?: ""
        val (productType, bookingType) = when {
            titleLower.contains("home") -> "blood_home" to "homeVisit"
            titleLower.contains("self") -> "blood_self" to "selfCollection"
            titleLower.contains("lab") -> "blood_lab" to "labVisit"
            else -> "blood_self" to "selfCollection"
        }

        if (isBookingDataInvalid(type, date, time, bookingType, address, lab)) {
            Toast.makeText(requireContext(), getString(R.string.please_select_details), Toast.LENGTH_SHORT).show()
            return
        }

        val formattedDate = date?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time) }
        val formattedTime = time?.let { formatTime(it) }

        val bookingDetails = when (bookingType) {
            "selfCollection" -> BookingDetails(date = null, time = null, addressId = address.id)
            "labVisit" -> BookingDetails(date = formattedDate, time = formattedTime, labId = lab?.id)
            else -> BookingDetails(date = formattedDate, time = formattedTime, addressId = address.id)
        }

        val param = AddToCartParam(
            productId = type?.id ?: "",
            productType = productType,
            bookingType = bookingType,
            quantity = 1,
            bookingDetails = bookingDetails
        )

        val rawPrice = type?.price ?: "29.99"
        val clean = rawPrice.replace("£", "").replace("$", "").trim()
        val basePrice = clean.toDoubleOrNull() ?: 29.99

        viewModel.startBookingCheckout(param, address.id ?: "", basePrice)
    }

    private fun presentPaymentSheet(data: CreatePaymentIntentResponse.Data) {
        val publishableKey = data.publishableKey ?: return
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
        showLoadingState(false)
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(requireContext(), "Payment successful!", Toast.LENGTH_SHORT).show()
                val orderId = viewModel.getCurrentOrderId() ?: ""
                val bundle = Bundle().apply {
                    putString("orderId", orderId)
                }
                try {
                    findNavController().navigate(R.id.fragmentOrderSuccess, bundle)
                } catch (e: Exception) {
                    val navOptions = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.fragmentShop, true)
                        .build()
                    findNavController().navigate(R.id.fragmentShop, null, navOptions)
                }
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(requireContext(), getString(R.string.payment_canceled), Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                val error = paymentSheetResult.error.message ?: getString(R.string.something_went_wrong)
                Toast.makeText(requireContext(), getString(R.string.payment_failed, error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isBookingDataInvalid(
        type: BookingTypeResponse.BookingType?,
        date: java.util.Calendar?,
        time: String?,
        bookingType: String,
        address: GetCartResponse.Address?,
        lab: GetAllLabResponse.Lab?
    ): Boolean {
        return when (bookingType) {
            "selfCollection" -> false
            "labVisit" -> date == null || time == null
            else -> date == null || time == null
        }
    }

    private fun displayBookingData() {
        val type = viewModel.getSelectedBookingType()
        val date = viewModel.getSelectedDate()
        val time = viewModel.getSelectedTime()
        val address = viewModel.getSelectedAddress() ?: createMockAddress()
        val lab = viewModel.getSelectedLab()

        val titleLower = type?.title?.lowercase() ?: ""

        val rawPrice = type?.price ?: "29.99"
        val formattedPrice = formatCurrencyPrice(rawPrice)

        binding.tvBookingTypeTitle.text = type?.title ?: "At-home service"
        binding.tvBookingTypeDesc.text = type?.description ?: "A professional visits your home to collect the sample."
        binding.tvPrice.text = formattedPrice
        binding.tvTotalPrice.text = formattedPrice
        binding.tvPriceItemName.text = type?.title ?: "At-home service"

        when {
            titleLower.contains("home") -> {
                binding.tvAddressLabel.text = "AT HOME"
                binding.ivLocationIcon.setImageResource(R.drawable.ic_opt_heart)
                bindAddress(address)
            }
            titleLower.contains("lab") -> {
                binding.tvAddressLabel.text = "LAB"
                binding.ivLocationIcon.setImageResource(R.drawable.ic_opt_flask)
                if (lab != null) {
                    binding.tvAddressName.text = lab.labName ?: "Canary Wharf Lab"
                    val labAddress = lab.address
                    val parts = listOfNotNull(labAddress?.line1, labAddress?.line2, labAddress?.city, labAddress?.postcode).filter { it.isNotBlank() }
                    binding.tvAddressDetails.text = if (parts.isNotEmpty()) parts.joinToString(", ") else "Canary Wharf, London"
                } else {
                    binding.tvAddressName.text = "Canary Wharf Partner Lab"
                    binding.tvAddressDetails.text = "12 Cabot Square, London E16 2PH"
                }
            }
            else -> {
                binding.tvAddressLabel.text = "KIT POSTED TO"
                binding.ivLocationIcon.setImageResource(R.drawable.ic_opt_document)
                bindAddress(address)
            }
        }

        // Date & Time visibility
        if (date != null || !time.isNullOrEmpty()) {
            binding.llDateTimeContainer.visibility = View.VISIBLE
            if (titleLower.contains("home")) {
                binding.tvLabelDate.text = "DATE"
            } else {
                binding.tvLabelDate.text = "DATE & TIME"
            }

            if (date != null) {
                val sdf = SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH)
                binding.tvSelectedDateTime.text = sdf.format(date.time)
            } else {
                binding.tvSelectedDateTime.text = "Wed, 18 Jun"
            }

            if (!time.isNullOrEmpty()) {
                binding.tvSelectedDateTimeExtra.text = time
                binding.tvSelectedDateTimeExtra.visibility = View.VISIBLE
            } else {
                binding.tvSelectedDateTimeExtra.visibility = View.GONE
            }
        } else {
            binding.llDateTimeContainer.visibility = View.GONE
        }
    }

    private fun createMockAddress(): GetCartResponse.Address {
        return GetCartResponse.Address(
            id = "a1",
            firstName = "Chinmay",
            lastName = "Bhatt",
            address1 = "113, Masthead House",
            address2 = "14 Rope Terrace",
            address3 = "",
            city = "London",
            postcode = "E16 2PH",
            country = "England",
            contactNo = "+44 7417 519358",
            isDefault = true
        )
    }

    private fun bindAddress(address: GetCartResponse.Address) {
        val fullName = getString(R.string.full_name_format, address.firstName ?: "", address.lastName ?: "").trim()
        binding.tvAddressName.text = if (fullName.isNotBlank()) fullName else "Chinmay Bhatt"

        val parts = listOfNotNull(
            address.address1,
            address.address2,
            address.address3,
            address.city,
            address.country,
            address.postcode
        ).filter { it.isNotBlank() }

        binding.tvAddressDetails.text = if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            "113, Masthead House, 14 Rope Terrace, London, England, E16 2PH"
        }
    }

    private fun formatCurrencyPrice(rawPrice: String): String {
        val clean = rawPrice.replace("£", "").replace("$", "").trim()
        val doubleVal = clean.toDoubleOrNull() ?: 29.99
        return "£%.2f".format(Locale.ENGLISH, doubleVal)
    }

    private fun formatTime(timeStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timeStr)
            date?.let { outputFormat.format(it) } ?: timeStr
        } catch (e: Exception) {
            timeStr
        }
    }
}
