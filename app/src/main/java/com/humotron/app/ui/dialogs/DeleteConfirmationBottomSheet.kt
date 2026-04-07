package com.humotron.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.LayoutDeleteConfirmationBinding

class DeleteConfirmationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutDeleteConfirmationBinding? = null
    private val binding get() = _binding!!
    
    private var onDeleteConfirmed: (() -> Unit)? = null

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutDeleteConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {
            onDeleteConfirmed?.invoke()
            dismiss()
        }
    }

    fun setDeleteListener(listener: () -> Unit) {
        onDeleteConfirmed = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DeleteConfirmationBottomSheet"
        
        fun newInstance(onDelete: () -> Unit): DeleteConfirmationBottomSheet {
            val sheet = DeleteConfirmationBottomSheet()
            sheet.onDeleteConfirmed = onDelete
            return sheet
        }
    }
}
