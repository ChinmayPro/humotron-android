package com.humotron.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.humotron.app.R
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.databinding.LayoutPowerOptionsBottomSheetBinding

class PowerOptionBottomSheetDialog : BaseBottomSheetDialogFragment() {

    private var _binding: LayoutPowerOptionsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var onRestartClick: (() -> Unit)? = null
    private var onShutDownClick: (() -> Unit)? = null

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutPowerOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRestart.setOnClickListener {
            onRestartClick?.invoke()
            dismiss()
        }

        binding.btnShutDown.setOnClickListener {
            onShutDownClick?.invoke()
            dismiss()
        }
    }

    fun setListeners(onRestart: () -> Unit, onShutDown: () -> Unit) {
        this.onRestartClick = onRestart
        this.onShutDownClick = onShutDown
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PowerOptionBottomSheetDialog"
        fun newInstance(): PowerOptionBottomSheetDialog {
            return PowerOptionBottomSheetDialog()
        }
    }
}
