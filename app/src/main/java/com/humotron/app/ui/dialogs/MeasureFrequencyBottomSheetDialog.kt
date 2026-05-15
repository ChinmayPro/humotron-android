package com.humotron.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.humotron.app.R
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.databinding.LayoutMeasureFrequencyBottomSheetBinding

class MeasureFrequencyBottomSheetDialog : BaseBottomSheetDialogFragment() {

    private var _binding: LayoutMeasureFrequencyBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var currentFrequency: Int = 60
    private var onSaveClick: ((Int) -> Unit)? = null

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = LayoutMeasureFrequencyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getInt(ARG_FREQUENCY)?.let {
            currentFrequency = it
        }

        updateFrequencyText(currentFrequency)
        binding.sliderFrequency.value = currentFrequency.toFloat()

        binding.sliderFrequency.addOnChangeListener { _, value, _ ->
            currentFrequency = value.toInt()
            updateFrequencyText(currentFrequency)
        }

        binding.btnSave.setOnClickListener {
            onSaveClick?.invoke(currentFrequency)
            dismiss()
        }
    }

    private fun updateFrequencyText(frequency: Int) {
        binding.tvFrequencyValue.text = getString(R.string.every_s_sec, frequency)
    }

    fun setOnSaveListener(onSave: (Int) -> Unit) {
        this.onSaveClick = onSave
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MeasureFrequencyBottomSheetDialog"
        private const val ARG_FREQUENCY = "arg_frequency"

        fun newInstance(frequency: Int = 60): MeasureFrequencyBottomSheetDialog {
            return MeasureFrequencyBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FREQUENCY, frequency)
                }
            }
        }
    }
}
