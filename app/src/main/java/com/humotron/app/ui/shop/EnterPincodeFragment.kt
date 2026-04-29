package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentEnterPincodeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterPincodeFragment : BaseFragment(R.layout.fragment_enter_pincode) {

    private lateinit var binding: FragmentEnterPincodeBinding
    private val viewModel: ShopViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEnterPincodeBinding.bind(view)

        // Set Status Bar Color to Black
        activity?.window?.statusBarColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.humotron.app.R.color.black)

        initViews()
    }

    private fun initViews() {
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAccessLocation.setOnClickListener {
            // Logic for location access can be added here
            // For now, just a placeholder
        }

        binding.etPincode.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                binding.etPincode.clearFocus()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.btnContinue.setOnClickListener {
            val pincode = binding.etPincode.text.toString()
            if (pincode.isNotEmpty()) {
                // Navigate to Select Lab screen
                val bundle = androidx.core.os.bundleOf("postcode" to pincode)
                findNavController().navigate(R.id.action_enterPincodeFragment_to_fragmentSelectLab, bundle)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etPincode.windowToken, 0)
    }
}
