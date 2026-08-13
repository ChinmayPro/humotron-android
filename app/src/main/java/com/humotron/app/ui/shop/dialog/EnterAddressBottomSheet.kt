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

    var onAddressSaved: (() -> Unit)? = null

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
        binding.ccp.setHintExampleNumberEnabled(false)
        binding.etPhone.hint = getString(R.string.phone_hint)

        binding.btnSave.setOnClickListener {
            saveAddress()
        }

        binding.etPostalCode.filters = arrayOf(android.text.InputFilter.AllCaps())

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

        viewModel.getCreateAddressLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> showProgress()
                Status.SUCCESS -> {
                    hideProgress()
                    android.widget.Toast.makeText(requireContext(), resource.data?.message ?: "Address created successfully", android.widget.Toast.LENGTH_SHORT).show()
                    onAddressSaved?.invoke()
                    dismiss()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    android.widget.Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Error creating address", android.widget.Toast.LENGTH_SHORT).show()
                }
                else -> hideProgress()
            }
        }
    }

    private fun saveAddress() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val contactNo = binding.etPhone.text.toString().trim()
        val address1 = binding.etAddress1.text.toString().trim()
        val address2 = binding.etAddress2.text.toString().trim()
        val address3 = binding.etAddress3.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val country = binding.etCountry.text.toString().trim()
        val postcode = binding.etPostalCode.text.toString().trim()
        val isDefault = binding.cbMakeDefault.isChecked

        if (firstName.isBlank()) {
            android.widget.Toast.makeText(requireContext(), "Please enter first name", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (lastName.isBlank()) {
            android.widget.Toast.makeText(requireContext(), "Please enter last name", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (contactNo.isBlank() || contactNo.length < 7) {
            android.widget.Toast.makeText(requireContext(), "Please enter valid contact number", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (postcode.isBlank()) {
            android.widget.Toast.makeText(requireContext(), "Please enter postal code", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (address1.isBlank()) {
            android.widget.Toast.makeText(requireContext(), "Please enter address line 1", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (city.isBlank()) {
            android.widget.Toast.makeText(requireContext(), "Please enter city", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (country.isBlank()) {
            android.widget.Toast.makeText(requireContext(), "Please enter country", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val request = com.humotron.app.domain.modal.param.CreateAddressRequest(
            firstName = firstName,
            lastName = lastName,
            contactNo = contactNo,
            address1 = address1,
            address2 = address2,
            address3 = address3,
            city = city,
            country = country,
            postcode = postcode,
            isDefault = isDefault
        )

        viewModel.createAddress(request)
    }

    private fun showAddressSelectionDialog(suggestions: List<com.humotron.app.domain.modal.response.AddressSuggestion>) {
        val context = requireContext()
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(context, R.style.TransparentBottomSheetDialogTheme)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_address_suggestions_bottomsheet, null)
        val tvSubtitle = dialogView.findViewById<android.widget.TextView>(R.id.tvSubtitle)
        val ivClose = dialogView.findViewById<android.widget.ImageView>(R.id.ivClose)
        val rvAddresses = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAddresses)

        val postcode = binding.etPostalCode.text.toString().trim().uppercase()
        tvSubtitle.text = "Showing ${suggestions.size} addresses for $postcode"

        ivClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        rvAddresses.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rvAddresses.adapter = AddressSuggestionCardAdapter(suggestions) { item ->
            bottomSheetDialog.dismiss()
            item.id?.let { id ->
                viewModel.fetchFullAddress(id)
            }
        }

        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets -> insets }
            }
        }

        bottomSheetDialog.window?.let { window ->
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }

        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }

    private class AddressSuggestionCardAdapter(
        private val items: List<com.humotron.app.domain.modal.response.AddressSuggestion>,
        private val onItemClick: (com.humotron.app.domain.modal.response.AddressSuggestion) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<AddressSuggestionCardAdapter.ViewHolder>() {

        class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val tvLine1: android.widget.TextView = view.findViewById(R.id.tvLine1)
            val tvLine2: android.widget.TextView = view.findViewById(R.id.tvLine2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address_card, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val fullText = item.address ?: ""
            if (fullText.contains("\n")) {
                val parts = fullText.split("\n", limit = 2)
                holder.tvLine1.text = parts[0]
                holder.tvLine2.text = parts[1]
                holder.tvLine2.visibility = View.VISIBLE
            } else {
                holder.tvLine1.text = fullText
                holder.tvLine2.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount(): Int = items.size
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
                it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                    val imeHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
                    v.setPadding(0, 0, 0, imeHeight)
                    insets
                }
            }
        }
        dialog.window?.let { window ->
            window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.statusBarColor = android.graphics.Color.TRANSPARENT
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
            it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val imeHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
                v.setPadding(0, 0, 0, imeHeight)
                insets
            }
        }
        dialog?.window?.let { window ->
            window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.statusBarColor = android.graphics.Color.TRANSPARENT
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
