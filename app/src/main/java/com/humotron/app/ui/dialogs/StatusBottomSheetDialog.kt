package com.humotron.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.humotron.app.R
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.databinding.LayoutStatusBottomSheetBinding

class StatusBottomSheetDialog : BaseBottomSheetDialogFragment() {

    private var _binding: LayoutStatusBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var isSuccess: Boolean = true
    private var title: String? = null
    private var subtitle: String? = null
    private var buttonText: String? = null
    private var onActionClick: (() -> Unit)? = null

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        arguments?.let {
            isSuccess = it.getBoolean(ARG_IS_SUCCESS, true)
            title = it.getString(ARG_TITLE)
            subtitle = it.getString(ARG_SUBTITLE)
            buttonText = it.getString(ARG_BUTTON_TEXT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutStatusBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        if (isSuccess) {
            binding.ivStatusIcon.setImageResource(R.drawable.ic_big_green_check)
            binding.btnAction.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.lime_green)
            binding.btnAction.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        } else {
            binding.ivStatusIcon.setImageResource(R.drawable.ic_warning_24px)
            binding.btnAction.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_1)
            binding.btnAction.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        title?.let { binding.tvTitle.text = it }
        subtitle?.let { binding.tvSubtitle.text = it }
        buttonText?.let { binding.btnAction.text = it }

        binding.btnAction.setOnClickListener {
            onActionClick?.invoke()
            dismiss()
        }
    }

    fun setActionListener(listener: () -> Unit) {
        this.onActionClick = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "StatusBottomSheetDialog"
        private const val ARG_IS_SUCCESS = "arg_is_success"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_SUBTITLE = "arg_subtitle"
        private const val ARG_BUTTON_TEXT = "arg_button_text"

        fun newInstance(
            isSuccess: Boolean,
            title: String,
            subtitle: String,
            buttonText: String = "Done"
        ): StatusBottomSheetDialog {
            val args = Bundle().apply {
                putBoolean(ARG_IS_SUCCESS, isSuccess)
                putString(ARG_TITLE, title)
                putString(ARG_SUBTITLE, subtitle)
                putString(ARG_BUTTON_TEXT, buttonText)
            }
            return StatusBottomSheetDialog().apply {
                arguments = args
            }
        }
    }
}
