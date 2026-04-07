package com.humotron.app.ui.bloodTest.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.databinding.LayoutBottomsheetChooseUploadMethodBinding

class ChooseUploadMethodBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomsheetChooseUploadMethodBinding? = null
    private val binding get() = _binding!!
    
    private var onMethodSelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomsheetChooseUploadMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageButtonClose.setOnClickListener {
            dismiss()
        }

        binding.clImportEmail.setOnClickListener {
            onMethodSelected?.invoke("email")
            dismiss()
        }

        binding.clDeviceUpload.setOnClickListener {
            onMethodSelected?.invoke("device")
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            navigationBarColor = Color.BLACK
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightNavigationBars = false
        }
    }

    fun setMethodSelectionListener(listener: (String) -> Unit) {
        onMethodSelected = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ChooseUploadMethodBottomSheet"
    }
}
