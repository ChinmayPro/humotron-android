package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSelectAddressBinding
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.domain.modal.response.GetCartResponse.Address
import com.humotron.app.ui.shop.dialog.EnterAddressBottomSheet
import com.humotron.app.ui.shop.dialog.SelectAddressBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAddressFragment : BaseFragment(R.layout.fragment_select_address) {

    private lateinit var binding: FragmentSelectAddressBinding
    private val viewModel: ShopViewModel by viewModels()
    private var currentAddressId: String? = null

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
            
            binding.btnContinue.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + dpToPx(24)
            }
            
            insets
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSelectAddress_to_fragmentChooseDateTime)
        }

        binding.btnAddNewAddress.setOnClickListener {
            showEnterAddressBottomSheet()
        }

        binding.cvNoAddress.setOnClickListener {
            showEnterAddressBottomSheet()
        }

        binding.btnChangeAddress.setOnClickListener {
            showSelectAddressBottomSheet()
        }
    }

    private fun showSelectAddressBottomSheet() {
        SelectAddressBottomSheet.newInstance(currentAddressId) { selectedAddress ->
            currentAddressId = selectedAddress.id
            bindAddress(selectedAddress)
        }.show(childFragmentManager, SelectAddressBottomSheet::class.java.simpleName)
    }

    private fun showEnterAddressBottomSheet() {
        EnterAddressBottomSheet.newInstance().show(
            childFragmentManager,
            EnterAddressBottomSheet::class.java.simpleName
        )
    }

    private fun initObservers() {
        viewModel.getDefaultConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoader()
                    binding.clContent.visibility = View.VISIBLE
                    val address = resource.data?.address
                    if (address != null) {
                        binding.cvAddress.visibility = View.VISIBLE
                        binding.cvNoAddress.visibility = View.GONE
                        binding.btnContinue.isEnabled = true
                        currentAddressId = address.id
                        bindAddress(address)
                    } else {
                        binding.cvAddress.visibility = View.GONE
                        binding.cvNoAddress.visibility = View.VISIBLE
                        binding.btnContinue.isEnabled = false
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideLoader()
                    binding.clContent.visibility = View.GONE
                }
                Status.LOADING -> {
                    showLoader()
                    binding.clContent.visibility = View.GONE
                }
            }
        }
    }

    private fun bindAddress(address: Address) {
        binding.tvAddressName.text = getString(R.string.full_name_format, address.firstName ?: "", address.lastName ?: "").trim()
        binding.tvAddressPhone.text = address.contactNo ?: ""
        
        val addressParts = listOfNotNull(
            address.address1,
            address.address2,
            address.address3,
            address.city,
            address.country,
            address.postcode
        ).filter { it.isNotBlank() }
        
        binding.tvAddressDetails.text = addressParts.joinToString(", ")
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
