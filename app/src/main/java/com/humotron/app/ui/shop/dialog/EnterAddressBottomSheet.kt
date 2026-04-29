package com.humotron.app.ui.shop.dialog

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.humotron.app.R

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.DialogEnterAddressBinding
import com.humotron.app.ui.shop.ShopViewModel

class EnterAddressBottomSheet : BaseBottomSheetDialogFragment() {

    private var _binding: DialogEnterAddressBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel by activityViewModels<ShopViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEnterAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.ccp.registerCarrierNumberEditText(binding.etPhone)

        binding.btnSave.setOnClickListener {
            // TODO: Implement save logic or callback
            dismiss()
        }

        binding.etPostalCode.doAfterTextChanged { text ->
            val isNotEmpty = !text.isNullOrBlank()
            binding.btnSearch.isEnabled = isNotEmpty
            if (isNotEmpty) {
                binding.btnSearch.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            } else {
                binding.btnSearch.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray_400_ui))
            }
        }

        binding.btnSearch.setOnClickListener {
            val term = binding.etPostalCode.text.toString()
            if (term.isNotBlank()) {
                viewModel.fetchAddressAutocomplete(term)
            }
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.getAddressAutocompleteLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> showProgress()
                Status.SUCCESS -> {
                    hideProgress()
                    val suggestions = resource.data?.suggestions
                    if (!suggestions.isNullOrEmpty()) {
                        showAddressSelectionDialog(suggestions)
                    } else {
                        android.widget.Toast.makeText(requireContext(), "No addresses found", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    android.widget.Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Error fetching addresses", android.widget.Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.getFullAddressLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> showProgress()
                Status.SUCCESS -> {
                    hideProgress()
                    resource.data?.let { populateAddressFields(it) }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    android.widget.Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Error fetching full address", android.widget.Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun showAddressSelectionDialog(suggestions: List<com.humotron.app.domain.modal.response.AddressSuggestion>) {
        val addresses = suggestions.map { it.address ?: "" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Select Address")
            .setItems(addresses) { _, which ->
                val selectedId = suggestions[which].id
                if (selectedId != null) {
                    viewModel.fetchFullAddress(selectedId)
                }
            }
            .show()
    }

    private fun populateAddressFields(address: com.humotron.app.domain.modal.response.FullAddressResponse) {
        binding.llAddressDetails.visibility = View.VISIBLE
        binding.etAddress1.setText(address.line1 ?: "")
        binding.etAddress2.setText(address.line2 ?: "")
        binding.etAddress3.setText(address.line3 ?: "")
        binding.etCity.setText(address.townOrCity ?: address.locality ?: "")
        binding.etCountry.setText(address.country ?: "")
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

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): EnterAddressBottomSheet {
            return EnterAddressBottomSheet()
        }
    }
}
