package com.humotron.app.ui.shop.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.LayoutBottomsheetSelectQuantityBinding

class SelectQuantityBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomsheetSelectQuantityBinding? = null
    private val binding get() = _binding!!

    private var quantity = 1
    private var onContinueListener: ((Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomsheetSelectQuantityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the background of the system container to transparent
        dialog?.setOnShowListener {
            val d = it as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
            bottomSheet?.let { sheet ->
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        initViews()
    }

    private fun initViews() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityText()
            }
        }

        binding.btnPlus.setOnClickListener {
            quantity++
            updateQuantityText()
        }

        binding.btnContinue.setOnClickListener {
            onContinueListener?.invoke(quantity)
            dismiss()
        }

        updateQuantityText()
    }

    private fun updateQuantityText() {
        binding.tvQuantity.text = quantity.toString()
    }

    fun setOnContinueListener(listener: (Int) -> Unit) {
        this.onContinueListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SelectQuantityBottomSheet {
            return SelectQuantityBottomSheet()
        }
    }
}
