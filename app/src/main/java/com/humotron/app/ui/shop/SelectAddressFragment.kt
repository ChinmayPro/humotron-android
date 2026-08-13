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
import com.humotron.app.databinding.FragmentSelectAddressBinding
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.domain.modal.response.GetCartResponse.Address
import com.humotron.app.ui.shop.adapter.SelectAddressAdapter
import com.humotron.app.ui.shop.dialog.EditAddressBottomSheet
import com.humotron.app.ui.shop.dialog.EnterAddressBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAddressFragment : BaseFragment(R.layout.fragment_select_address) {

    private lateinit var binding: FragmentSelectAddressBinding
    private val viewModel: ShopViewModel by activityViewModels()
    private lateinit var adapter: SelectAddressAdapter
    private var selectedAddress: GetCartResponse.Address? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectAddressBinding.bind(view)

        setupInsets()
        initViews()
        initObservers()
        viewModel.fetchDefaultConfig(
            payload = "BU69YsgWhF9NOGAKzexgvQ==",
            iv = "SZcndf9QS08vbx9UYPeK4A=="
        )
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

        setupFlowStepHeader()

        adapter = SelectAddressAdapter(
            onAddressSelected = { address ->
                selectedAddress = address
                binding.btnContinue.isEnabled = true
                binding.btnContinue.alpha = 1.0f
            },
            onEditAddress = { _ ->
                showSelectAddressBottomSheet()
            }
        )
        binding.rvAddresses.adapter = adapter

        binding.btnContinue.setOnClickListener {
            viewModel.setSelectedAddress(selectedAddress)
            findNavController().navigate(R.id.action_fragmentSelectAddress_to_fragmentVerifyBooking)
        }

        binding.llAddNewAddress.setOnClickListener {
            showEnterAddressBottomSheet()
        }
    }

    private fun setupFlowStepHeader() {
        val titleLower = viewModel.getSelectedBookingType()?.title?.lowercase() ?: ""
        if (titleLower.contains("home")) {
            updateStepProgressBar(totalSteps = 5, currentStepIndex = 3, stepName = "LOCATION")
        } else {
            updateStepProgressBar(totalSteps = 3, currentStepIndex = 1, stepName = "LOCATION")
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

    private fun showSelectAddressBottomSheet() {
        val bottomSheet = com.humotron.app.ui.shop.dialog.SelectAddressBottomSheet.newInstance(selectedAddress?.id) { selected ->
            selectedAddress = selected
            adapter.setData(listOf(selected))
            binding.btnContinue.isEnabled = true
            binding.btnContinue.alpha = 1.0f
        }
        bottomSheet.show(childFragmentManager, com.humotron.app.ui.shop.dialog.SelectAddressBottomSheet::class.java.simpleName)
    }

    private fun showEnterAddressBottomSheet() {
        val bottomSheet = EnterAddressBottomSheet.newInstance()
        bottomSheet.onAddressSaved = {
            viewModel.fetchDefaultConfig(
                payload = "BU69YsgWhF9NOGAKzexgvQ==",
                iv = "SZcndf9QS08vbx9UYPeK4A=="
            )
        }
        bottomSheet.show(childFragmentManager, EnterAddressBottomSheet::class.java.simpleName)
    }

    private fun initObservers() {
        viewModel.getDefaultConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoader()
                    val apiAddress = resource.data?.address
                    val addresses = createMockAddressList(apiAddress)
                    val defaultAddress = addresses.find { it.isDefault == true } ?: addresses.firstOrNull()
                    if (defaultAddress != null) {
                        selectedAddress = defaultAddress
                        adapter.setData(listOf(defaultAddress))
                        binding.btnContinue.isEnabled = true
                        binding.btnContinue.alpha = 1.0f
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideLoader()
                    val addresses = createMockAddressList(null)
                    val defaultAddress = addresses.find { it.isDefault == true } ?: addresses.firstOrNull()
                    if (defaultAddress != null) {
                        selectedAddress = defaultAddress
                        adapter.setData(listOf(defaultAddress))
                        binding.btnContinue.isEnabled = true
                        binding.btnContinue.alpha = 1.0f
                    }
                }
                Status.LOADING -> {
                    showLoader()
                }
            }
        }
    }

    private fun createMockAddressList(apiAddress: Address?): List<Address> {
        val list = mutableListOf<Address>()
        if (apiAddress != null) {
            list.add(apiAddress)
        } else {
            list.add(
                Address(
                    id = "a1",
                    firstName = "Chinmay",
                    lastName = "Bhatt",
                    address1 = "113 Masthead House",
                    address2 = "14 Rope Terrace",
                    address3 = "",
                    city = "London",
                    postcode = "E16 2PH",
                    country = "England",
                    contactNo = "+44 7417 519358",
                    isDefault = true
                )
            )
        }

        list.add(
            Address(
                id = "a2",
                firstName = "Ravi",
                lastName = "Patel",
                address1 = "Flat 6",
                address2 = "Arden Court, 22 Wharf Road",
                address3 = "",
                city = "London",
                postcode = "N1 7GR",
                country = "England",
                contactNo = "+44 7822 118842",
                isDefault = false
            )
        )

        return list
    }

    private fun showLoader() {
        binding.layoutLoader.root.visibility = View.VISIBLE
        binding.layoutLoader.lottieLoader.playAnimation()
    }

    private fun hideLoader() {
        binding.layoutLoader.root.visibility = View.GONE
        binding.layoutLoader.lottieLoader.cancelAnimation()
    }
}
