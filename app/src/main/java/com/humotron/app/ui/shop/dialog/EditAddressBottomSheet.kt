package com.humotron.app.ui.shop.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.databinding.DialogEditAddressBinding
import com.humotron.app.domain.modal.response.GetCartResponse
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import com.humotron.app.ui.shop.ShopViewModel
import com.humotron.app.domain.modal.param.UpdateAddressRequest
import com.humotron.app.data.network.Status
import android.widget.Toast
import com.humotron.app.R


@AndroidEntryPoint
class EditAddressBottomSheet : BaseBottomSheetDialogFragment() {

    private var _binding: DialogEditAddressBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopViewModel by viewModels()
    
    private var address: GetCartResponse.Address? = null
    private var onSave: ((GetCartResponse.Address) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.humotron.app.R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        prefillData()
        observeViewModel()
    }

    private fun initViews() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveAddress()
        }
    }

    private fun saveAddress() {
        val addressId = address?.id ?: return
        
        val request = UpdateAddressRequest(
            firstName = binding.etFirstName.text.toString().trim(),
            lastName = binding.etLastName.text.toString().trim(),
            contactNo = binding.etPhone.text.toString().trim(),
            address1 = binding.etAddress1.text.toString().trim(),
            address2 = binding.etAddress2.text.toString().trim(),
            address3 = binding.etAddress3.text.toString().trim(),
            city = binding.etCity.text.toString().trim(),
            country = binding.etCountry.text.toString().trim(),
            postcode = binding.etPincode.text.toString().trim(),
            isDefault = binding.cbDefault.isChecked
        )
        
        viewModel.updateAddress(addressId, request)
    }

    private fun observeViewModel() {
        viewModel.getUpdateAddressLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    onSave?.invoke(address!!) // Callback to refresh parent list
                    dismiss()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    hideProgress()
                }
            }
        }
    }

    private fun prefillData() {
        address?.let { addr ->
            binding.etFirstName.setText(addr.firstName)
            binding.etLastName.setText(addr.lastName)
            binding.etPhone.setText(addr.contactNo)
            binding.etAddress1.setText(addr.address1)
            binding.etAddress2.setText(addr.address2)
            binding.etAddress3.setText(addr.address3)
            binding.etCity.setText(addr.city)
            binding.etCountry.setText(addr.country)
            binding.etPincode.setText(addr.postcode)
            binding.cbDefault.isChecked = addr.isDefault == true
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as com.google.android.material.bottomsheet.BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(address: GetCartResponse.Address? = null, onSave: (GetCartResponse.Address) -> Unit): EditAddressBottomSheet {
            val fragment = EditAddressBottomSheet()
            fragment.address = address
            fragment.onSave = onSave
            return fragment
        }
    }
}
