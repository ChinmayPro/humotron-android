package com.humotron.app.ui.assesment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.BottomSheetMultiSelectBinding

class MultiSelectBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMultiSelectBinding? = null
    private val binding get() = _binding!!

    private val selectedItems = mutableListOf<String>()
    private var onSave: ((List<String>) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMultiSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.skipCollapsed = true
        behavior.isDraggable = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheet.post {
            bottomSheet.translationY = bottomSheet.height.toFloat()
            SpringAnimation(bottomSheet, DynamicAnimation.TRANSLATION_Y, 0f).apply {
                spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                spring.stiffness = SpringForce.STIFFNESS_MEDIUM
                setStartValue(bottomSheet.height.toFloat())
                start()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = arguments?.getStringArrayList(ARG_OPTIONS) ?: return
        val preSelected = arguments?.getStringArrayList(ARG_SELECTED) ?: arrayListOf()
        selectedItems.addAll(preSelected)

        binding.btnClose.setOnClickListener { dismiss() }

        // Build option rows dynamically
        options.forEach { option ->
            val row = layoutInflater.inflate(
                R.layout.item_multiselect_row,
                binding.optionsContainer,
                false
            )
            val tvLabel = row.findViewById<android.widget.TextView>(R.id.tvOptionLabel)
            val checkbox = row.findViewById<View>(R.id.viewCheckbox)
            val checkboxBg = row.findViewById<View>(R.id.viewCheckboxBg)

            tvLabel.text = option

            fun updateCheckbox() {
                val isSelected = selectedItems.contains(option)
                checkboxBg.setBackgroundResource(
                    if (isSelected) R.drawable.bg_checkbox_selected else R.drawable.bg_checkbox_unselected
                )
            }
            updateCheckbox()

            row.setOnClickListener {
                if (selectedItems.contains(option)) selectedItems.remove(option)
                else selectedItems.add(option)
                updateCheckbox()
            }

            binding.optionsContainer.addView(row)
        }

        binding.btnSave.setOnClickListener {
            onSave?.invoke(selectedItems.toList())
            dismiss()
        }
    }

    private fun dismissWithAnimation() {
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
        bottomSheet?.animate()
            ?.translationY(bottomSheet.rootView.height.toFloat())
            ?.setDuration(300)
            ?.withEndAction { dismiss() }
            ?.start() ?: dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    companion object {
        const val TAG = "MultiSelectBottomSheet"
        private const val ARG_OPTIONS = "options"
        private const val ARG_SELECTED = "selected"

        fun newInstance(
            options: List<String>,
            selectedItems: List<String>,
            onSave: (List<String>) -> Unit
        ): MultiSelectBottomSheet {
            return MultiSelectBottomSheet().apply {
                this.onSave = onSave
                arguments = Bundle().apply {
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putStringArrayList(ARG_SELECTED, ArrayList(selectedItems))
                }
            }
        }
    }
}