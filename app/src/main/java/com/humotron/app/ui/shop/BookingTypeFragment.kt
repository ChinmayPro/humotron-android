package com.humotron.app.ui.shop

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBookingTypeBinding
import com.humotron.app.domain.modal.response.BookingTypeResponse.BookingType
import com.humotron.app.ui.shop.adapter.BookingTypeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingTypeFragment : BaseFragment(R.layout.fragment_booking_type) {

    private lateinit var binding: FragmentBookingTypeBinding
    private val viewModel: ShopViewModel by activityViewModels()
    private lateinit var adapter: BookingTypeAdapter
    private var selectedType: BookingType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookingTypeBinding.bind(view)

        setupInsets()
        initViews()
        initObservers()
        viewModel.fetchBookingTypes()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            binding.layoutFooter.updatePadding(bottom = systemBars.bottom + (20 * density).toInt())
            insets
        }
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Initialize default progress bar state (3 steps, step 1)
        updateStepProgressBar(totalSteps = 3, currentStepIndex = 0, stepName = "COLLECTION METHOD")

        adapter = BookingTypeAdapter { selectedItem ->
            selectedType = selectedItem
            val isSelected = selectedItem != null
            binding.btnContinue.isEnabled = isSelected
            binding.btnContinue.alpha = if (isSelected) 1.0f else 0.5f

            val titleLower = selectedItem?.title?.lowercase() ?: ""
            when {
                titleLower.contains("self") -> {
                    updateStepProgressBar(totalSteps = 3, currentStepIndex = 0, stepName = "COLLECTION METHOD")
                }
                titleLower.contains("home") -> {
                    updateStepProgressBar(totalSteps = 5, currentStepIndex = 0, stepName = "COLLECTION METHOD")
                }
                titleLower.contains("lab") -> {
                    updateStepProgressBar(totalSteps = 6, currentStepIndex = 0, stepName = "COLLECTION METHOD")
                }
                else -> {
                    updateStepProgressBar(totalSteps = 3, currentStepIndex = 0, stepName = "COLLECTION METHOD")
                }
            }
        }
        binding.rvBookingTypes.adapter = adapter

        binding.btnContinue.setOnClickListener {
            viewModel.setSelectedBookingType(selectedType)
            val titleLower = selectedType?.title?.lowercase() ?: ""
            when {
                titleLower.contains("lab") -> {
                    findNavController().navigate(R.id.action_fragmentBookingType_to_enterPincodeFragment)
                }
                titleLower.contains("home") -> {
                    findNavController().navigate(R.id.action_fragmentBookingType_to_fragmentChooseDateTime)
                }
                else -> {
                    findNavController().navigate(R.id.action_fragmentBookingType_to_fragmentSelectAddress)
                }
            }
        }
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

    private fun initObservers() {
        viewModel.getBookingTypeLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoader()
                    val data = resource.data?.data
                    val mockData = listOf(
                        BookingType(id = "self", title = "Self-collection kit", description = "Use our easy kit and drop it off when you are ready.", price = "£19.99"),
                        BookingType(id = "home", title = "At-home service", description = "A professional visits your home to collect the sample.", price = "£29.99"),
                        BookingType(id = "lab", title = "Lab visit", description = "Visit a partner lab for a quick, professional blood draw.", price = "£29.99")
                    )
                    adapter.setData(if (!data.isNullOrEmpty()) data else mockData)
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideLoader()
                    val mockData = listOf(
                        BookingType(id = "self", title = "Self-collection kit", description = "Use our easy kit and drop it off when you are ready.", price = "£19.99"),
                        BookingType(id = "home", title = "At-home service", description = "A professional visits your home to collect the sample.", price = "£29.99"),
                        BookingType(id = "lab", title = "Lab visit", description = "Visit a partner lab for a quick, professional blood draw.", price = "£29.99")
                    )
                    adapter.setData(mockData)
                }
                Status.LOADING -> {
                    showLoader()
                }
            }
        }
    }

    private fun showLoader() {
        binding.layoutLoader.root.visibility = View.VISIBLE
        binding.layoutLoader.lottieLoader.playAnimation()
    }

    private fun hideLoader() {
        binding.layoutLoader.root.visibility = View.GONE
    }
}
