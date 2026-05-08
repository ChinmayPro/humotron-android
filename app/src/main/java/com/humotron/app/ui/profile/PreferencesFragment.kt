package com.humotron.app.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.ui.profile.dialog.SelectDeliveryBottomSheet
import com.humotron.app.ui.profile.dialog.EnterCodeBottomSheet
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentPreferencesBinding
import androidx.fragment.app.activityViewModels
import com.humotron.app.ui.shop.dialog.EnterAddressBottomSheet
import com.humotron.app.ui.shop.dialog.SelectAddressBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import com.humotron.app.domain.modal.response.GetDefaultConfigResponse
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.ui.profile.dialog.CouponSuccessDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.view.ViewGroup


@AndroidEntryPoint
class PreferencesFragment : BaseFragment(R.layout.fragment_preferences) {

    private lateinit var binding: FragmentPreferencesBinding
    private val viewModel: ProfileViewModel by activityViewModels()
    private var allDeliveryOptions: List<GetCartResponse.DeliveryMethod> = emptyList()
    private var currentConfig: GetDefaultConfigResponse? = null
    private var isDeletingCoupon = false
    private var isDeletingVat = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPreferencesBinding.bind(view)

        setupInsets()

        setupObservers()
        initViews()
    }

    private fun setupObservers() {
        viewModel.getDeliveryOptionsLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    allDeliveryOptions = resource.data?.data?.deliveryOptions ?: emptyList()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    android.widget.Toast.makeText(requireContext(), resource.error?.errorMessage ?: getString(R.string.error_fetching_delivery_options), android.widget.Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.getDefaultConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    currentConfig = resource.data
                    updateUI(resource.data)
                    viewModel.clearUpdateUserLiveData()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    android.widget.Toast.makeText(requireContext(), resource.error?.errorMessage ?: getString(R.string.error_fetching_default_config), android.widget.Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.getUpdateUserLiveData().observe(viewLifecycleOwner) { resource ->
            if (resource == null) {
                hideProgress()
                binding.pbDeleteCoupon.visibility = View.GONE
                binding.pbDeleteVat.visibility = View.GONE
                if (isDeletingCoupon) binding.ivDeleteCoupon.visibility = View.VISIBLE
                if (isDeletingVat) binding.ivDeleteVat.visibility = View.VISIBLE
                isDeletingCoupon = false
                isDeletingVat = false
                return@observe
            }
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    binding.pbDeleteCoupon.visibility = View.GONE
                    binding.pbDeleteVat.visibility = View.GONE
                    
                    if (isDeletingCoupon) {
                        isDeletingCoupon = false
                        currentConfig = currentConfig?.copy(coupon = null)
                        updateUI(currentConfig)
                        viewModel.clearUpdateUserLiveData()
                    } else if (isDeletingVat) {
                        isDeletingVat = false
                        currentConfig = currentConfig?.copy(vatNumber = "")
                        updateUI(currentConfig)
                        viewModel.clearUpdateUserLiveData()
                    } else {
                        viewModel.fetchDefaultConfiguration()
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    binding.pbDeleteCoupon.visibility = View.GONE
                    binding.pbDeleteVat.visibility = View.GONE
                    if (isDeletingCoupon) binding.ivDeleteCoupon.visibility = View.VISIBLE
                    if (isDeletingVat) binding.ivDeleteVat.visibility = View.VISIBLE
                    isDeletingCoupon = false
                    isDeletingVat = false
                    android.widget.Toast.makeText(requireContext(), resource.error?.errorMessage ?: getString(R.string.something_went_wrong), android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.fetchDefaultConfiguration()
                }
                Status.LOADING -> {
                    if (isDeletingCoupon) {
                        binding.ivDeleteCoupon.visibility = View.GONE
                        binding.pbDeleteCoupon.visibility = View.VISIBLE
                    } else if (isDeletingVat) {
                        binding.ivDeleteVat.visibility = View.GONE
                        binding.pbDeleteVat.visibility = View.VISIBLE
                    } else {
                        showProgress()
                    }
                }
            }
        }
    }

    private fun updateUI(config: GetDefaultConfigResponse?) {
        config?.let {
            // Address
            it.address?.let { address ->
                populateAddressDetails(address)
            }

            // Delivery Method
            it.deliveryMethod?.let { method ->
                updateDeliveryUI(method)
            }

            // Coupon
            if (it.coupon != null) {
                binding.tvCouponLabel.text = getString(R.string.coupon_applied)
                
                val discountValue = it.coupon.discountValue ?: 0.0
                val isPercentage = it.coupon.discountType?.lowercase() == "percentage"
                val symbol = if (isPercentage) "%" else getString(R.string.currency_symbol)
                
                val discountText = if (isPercentage) {
                    "$discountValue$symbol ${getString(R.string.discount_label)}"
                } else {
                    "$symbol$discountValue ${getString(R.string.discount_label)}"
                }
                
                val promoCode = it.coupon.promoCode ?: it.coupon.promoCodeName ?: ""
                
                binding.tvCouponValue.text = "$discountText\n$promoCode"
                binding.tvCouponValue.visibility = View.VISIBLE
                binding.ivAddCoupon.visibility = View.GONE
                binding.ivDeleteCoupon.visibility = View.VISIBLE
            } else {
                binding.tvCouponLabel.text = getString(R.string.apply_coupon)
                binding.tvCouponValue.visibility = View.GONE
                binding.ivAddCoupon.visibility = View.VISIBLE
                binding.ivDeleteCoupon.visibility = View.GONE
            }

            // VAT Number
            if (!it.vatNumber.isNullOrEmpty()) {
                binding.tvVatLabel.text = getString(R.string.vat_number)
                binding.tvVatValue.text = it.vatNumber
                binding.tvVatValue.visibility = View.VISIBLE
                binding.ivAddVat.visibility = View.GONE
                binding.ivDeleteVat.visibility = View.VISIBLE
            } else {
                binding.tvVatLabel.text = getString(R.string.add_vat_number)
                binding.tvVatValue.visibility = View.GONE
                binding.ivAddVat.visibility = View.VISIBLE
                binding.ivDeleteVat.visibility = View.GONE
            }

            // Email Invoice
            binding.switchEmailInvoices.isChecked = it.emailInvoice ?: false
        }
    }

    private fun populateAddressDetails(address: GetCartResponse.Address) {
        val addressStr = StringBuilder()
        addressStr.append("${address.firstName} ${address.lastName}\n")
        addressStr.append("${address.contactNo}\n")
        addressStr.append("${address.address1}, ${address.address2}, ${address.address3},\n")
        addressStr.append("${address.city}, ${address.country}, ${address.postcode}")
        binding.tvAddressDetails.text = addressStr.toString()
    }

    private fun updateDeliveryUI(method: GetCartResponse.DeliveryMethod) {
        val methodInfo = "${method.methodName}\n(${method.minDays}-${method.maxDays} Days)"
        binding.tvDeliveryMethodName.text = methodInfo
        binding.tvDeliveryPrice.text = getString(R.string.currency_symbol) + (method.price ?: 0.0).toString()
        binding.tvExpectedDeliveryValue.text = method.estimatedDelivery ?: ""
    }

    private fun initViews() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clDeliveryOption.setOnClickListener {
            if (allDeliveryOptions.isNotEmpty()) {
                val bottomSheet = SelectDeliveryBottomSheet(
                    currentConfig?.deliveryMethod?.id,
                    allDeliveryOptions
                ) { selectedMethod ->
                    updateDeliveryUI(selectedMethod)
                    currentConfig = currentConfig?.copy(deliveryMethod = selectedMethod)
                    
                    val userId = prefUtils.getLoginResponse().id ?: ""
                    val map = HashMap<String, Any>()
                    map["deliveryMethodId"] = selectedMethod.id ?: ""
                    viewModel.updateUserById(userId, map)
                }
                bottomSheet.show(childFragmentManager, SelectDeliveryBottomSheet.TAG)
            } else {
                viewModel.fetchAllDeliveryOptions()
                android.widget.Toast.makeText(requireContext(), getString(R.string.loading_delivery_options), android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvAddNewAddress.setOnClickListener {
            val enterAddressBottomSheet = EnterAddressBottomSheet.newInstance()
            enterAddressBottomSheet.show(childFragmentManager, EnterAddressBottomSheet::class.java.simpleName)
        }

        binding.tvChangeAddress.setOnClickListener {
            val selectAddressBottomSheet = SelectAddressBottomSheet.newInstance(currentConfig?.address?.id) { selectedAddress ->
                populateAddressDetails(selectedAddress)
                currentConfig = currentConfig?.copy(address = selectedAddress)

//                val userId = prefUtils.getLoginResponse().id ?: ""
//                val map = HashMap<String, Any>()
//                map["addressId"] = selectedAddress.id ?: ""
//                viewModel.updateUserById(userId, map)
            }
            selectAddressBottomSheet.show(childFragmentManager, SelectAddressBottomSheet::class.java.simpleName)
        }

        binding.ivAddVat.setOnClickListener {
            EnterCodeBottomSheet.forVat(currentConfig?.vatNumber) { enteredVat ->
                // Handled via ViewModel observer in the bottom sheet
            }.show(childFragmentManager, EnterCodeBottomSheet.TAG)
        }

        binding.ivDeleteVat.setOnClickListener {
            val userId = prefUtils.getLoginResponse().id ?: ""
            val map = HashMap<String, Any>()
            map["vatNumber"] = ""
            isDeletingVat = true
            viewModel.updateUserById(userId, map)
        }

        binding.ivAddCoupon.setOnClickListener {
            val currentCoupon = currentConfig?.coupon?.promoCode ?: currentConfig?.coupon?.promoCodeName
            EnterCodeBottomSheet.forCoupon(currentCoupon, onCouponApplied = {
                CouponSuccessDialog.newInstance().show(childFragmentManager, CouponSuccessDialog.TAG)
            }) { enteredCoupon ->
                // Local update handled via observer
            }.show(childFragmentManager, EnterCodeBottomSheet.TAG)
        }

        binding.ivDeleteCoupon.setOnClickListener {
            val couponId = currentConfig?.coupon?.id ?: ""
            if (couponId.isNotEmpty()) {
                isDeletingCoupon = true
                viewModel.removePromoCodeByUser(couponId)
            } else {
                android.widget.Toast.makeText(requireContext(), "Coupon ID not found", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchEmailInvoices.setOnCheckedChangeListener { _, isChecked ->
            // Only trigger if the value actually changed from the current config
            if (currentConfig?.emailInvoice != isChecked) {
                val userId = prefUtils.getLoginResponse().id ?: ""
                val map = HashMap<String, Any>()
                map["emailInvoice"] = isChecked
                viewModel.updateUserById(userId, map)
            }
        }

        viewModel.fetchAllDeliveryOptions()
        viewModel.fetchDefaultConfiguration()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.nsvContent.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }
}
