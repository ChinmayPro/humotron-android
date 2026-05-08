package com.humotron.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.LayoutInfoBottomSheetBinding

class InfoBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: LayoutInfoBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var title: String? = null
    private var message: String? = null

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = LayoutInfoBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE)
        val message = arguments?.getString(ARG_MESSAGE)

        binding.tvTitle.isVisible = !title.isNullOrBlank()
        title?.let { binding.tvTitle.text = it }
        message?.let { binding.tvMessage.text = it }

        binding.btnGotIt.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "InfoBottomSheetDialog"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(title: String? = null, message: String): InfoBottomSheetDialog {
            return InfoBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                }
            }
        }
    }
}