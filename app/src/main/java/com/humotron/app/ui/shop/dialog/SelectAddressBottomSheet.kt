package com.humotron.app.ui.shop.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.DialogSelectAddressBinding
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.ui.shop.ShopViewModel
import com.humotron.app.ui.shop.adapter.AddressSelectAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAddressBottomSheet : BaseBottomSheetDialogFragment() {

    private var _binding: DialogSelectAddressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ShopViewModel by viewModels()
    
    private var onAddressSelected: ((GetCartResponse.Address) -> Unit)? = null
    private var selectedAddressId: String? = null

    private lateinit var adapter: AddressSelectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
        
        viewModel.fetchAllAddress()
    }

    private fun initViews() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        adapter = AddressSelectAdapter(
            onEditClick = { address ->
                EditAddressBottomSheet.newInstance(address) { _ ->
                    viewModel.fetchAllAddress()
                }.show(childFragmentManager, EditAddressBottomSheet::class.java.simpleName)
            },
            onAddressClick = { address ->
                onAddressSelected?.invoke(address)
                dismiss()
            }
        )

        binding.rvAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAddresses.adapter = adapter
    }

    private fun initObservers() {
        viewModel.getAllAddressLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.data?.let { list ->
                        // If we don't have a selection, or after an edit, 
                        // find the default address and select it
                        val defaultAddress = list.find { it.isDefault == true }
                        if (defaultAddress != null) {
                            selectedAddressId = defaultAddress.id
                        }
                        adapter.submitList(list, selectedAddressId)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    // Handle error
                }
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
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
        fun newInstance(selectedId: String?, onSelected: (GetCartResponse.Address) -> Unit): SelectAddressBottomSheet {
            val fragment = SelectAddressBottomSheet()
            fragment.selectedAddressId = selectedId
            fragment.onAddressSelected = onSelected
            return fragment
        }
    }
}
