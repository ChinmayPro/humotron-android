package com.humotron.app.ui.bloodTest.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.BottomSheetMultiSelectBinding

class SingleSelectBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMultiSelectBinding? = null
    private val binding get() = _binding!!

    private var onItemSelected: ((String) -> Unit)? = null
    private var title: String? = null
    private var subtitle: String? = null
    private var options: List<String> = emptyList()
    private var selectedItem: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMultiSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.let {
            title = it.getString(ARG_TITLE)
            subtitle = it.getString(ARG_SUBTITLE)
            options = it.getStringArrayList(ARG_OPTIONS)?.toList() ?: emptyList()
            selectedItem = it.getString(ARG_SELECTED)
        }

        setupUI()
    }

    private fun setupUI() {
        // Reuse multi-select layout but hide multi-select specific elements
        binding.btnSave.visibility = View.GONE
        
        // Find title/subtitle if they are in the layout (the layout we are reusing has them)
        // In bottom_sheet_multi_select.xml, they don't have IDs, so we find them by type/index or if we can.
        // Actually, let's just set the container content.
        
        binding.btnClose.setOnClickListener { dismiss() }

        options.forEach { option ->
            val row = layoutInflater.inflate(R.layout.item_radio_option, binding.optionsContainer, false) as RadioButton
            row.text = option
            row.isChecked = (option == selectedItem)
            
            row.setOnClickListener {
                onItemSelected?.invoke(option)
                dismiss()
            }
            
            binding.optionsContainer.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    companion object {
        const val TAG = "SingleSelectBottomSheet"
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"
        private const val ARG_OPTIONS = "options"
        private const val ARG_SELECTED = "selected"

        fun newInstance(
            title: String,
            subtitle: String,
            options: List<String>,
            selectedItem: String? = null,
            onItemSelected: (String) -> Unit
        ): SingleSelectBottomSheet {
            return SingleSelectBottomSheet().apply {
                this.onItemSelected = onItemSelected
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putString(ARG_SELECTED, selectedItem)
                }
            }
        }
    }
}
