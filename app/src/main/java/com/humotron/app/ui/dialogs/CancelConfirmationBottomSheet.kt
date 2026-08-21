package com.humotron.app.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.LayoutCancelConfirmationBinding

class CancelConfirmationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutCancelConfirmationBinding? = null
    private val binding get() = _binding!!

    private var onCancelConfirmed: ((reason: String) -> Unit)? = null

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        @Suppress("DEPRECATION")
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutCancelConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ARG_TITLE)?.let { binding.tvTitle.text = it }
        arguments?.getString(ARG_SUBTITLE)?.let { binding.tvSubtitle.text = it }
        arguments?.getString(ARG_KEEP_TEXT)?.let { binding.btnKeep.text = it }
        arguments?.getString(ARG_CANCEL_TEXT)?.let { binding.btnConfirmCancel.text = it }
        arguments?.getString(ARG_HINT)?.let { binding.etReason.hint = it }

        binding.btnKeep.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmCancel.setOnClickListener {
            val reason = binding.etReason.text?.toString()?.trim().orEmpty()
            onCancelConfirmed?.invoke(reason)
            dismiss()
        }

        setupKeyboardInsetListener(view)
    }

    private fun setupKeyboardInsetListener(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val bottomPadding = if (imeHeight > 0) imeHeight + 16 else navHeight + 24
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CancelConfirmationBottomSheet"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_SUBTITLE = "arg_subtitle"
        private const val ARG_KEEP_TEXT = "arg_keep_text"
        private const val ARG_CANCEL_TEXT = "arg_cancel_text"
        private const val ARG_HINT = "arg_hint"

        fun newInstance(
            title: String? = null,
            subtitle: String? = null,
            keepButtonText: String? = null,
            cancelButtonText: String? = null,
            hint: String? = null,
            onCancel: (reason: String) -> Unit
        ): CancelConfirmationBottomSheet {
            val sheet = CancelConfirmationBottomSheet()
            sheet.onCancelConfirmed = onCancel
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_SUBTITLE, subtitle)
                putString(ARG_KEEP_TEXT, keepButtonText)
                putString(ARG_CANCEL_TEXT, cancelButtonText)
                putString(ARG_HINT, hint)
            }
            sheet.arguments = args
            return sheet
        }
    }
}
