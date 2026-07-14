package com.humotron.app.ui.device.cuff

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSmartCuffReadingInstructionsBinding
import com.humotron.app.domain.modal.BPMachineReadingType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmartCuffReadingInstructionFragment :
    BaseFragment(R.layout.fragment_smart_cuff_reading_instructions),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartCuffReadingInstructionsBinding
    private var userDevice: UserDevice? = null
    private var readingType: BPMachineReadingType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartCuffReadingInstructionsBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@SmartCuffReadingInstructionFragment)
        binding.btnStartMeasurement.setOnClickListener(this@SmartCuffReadingInstructionFragment)
    }

    private fun initViews() = with(binding) {
        header.tvTitle.text = getString(R.string.take_a_reading)
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }
        readingType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.READING_TYPE, BPMachineReadingType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.READING_TYPE)
        }

        when (readingType) {
            BPMachineReadingType.BLOOD_PRESSURE -> {
                mainTitleTextView.text = getString(R.string.bp_title)
                mainDescriptionTextView.text = getString(R.string.bp_desc)

                tvStep1Title.text = getString(R.string.bp_step1_title)
                tvStep1.text = getString(R.string.bp_step1_desc)

                tvStep2Title.text = getString(R.string.bp_step2_title)
                tvStep2.text = getString(R.string.bp_step2_desc)

                tvStep3Title.text = getString(R.string.bp_step3_title)
                tvStep3.text = getString(R.string.bp_step3_desc)

                btnStartMeasurement.text = getString(R.string.bp_button)

                // Set Icons (Assuming you have these drawables)
                ivType.setImageResource(R.drawable.ic_wave_2)
                ivType.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.good))

                // Set Card Background Colors
                mcvIcon.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.good_14
                    )
                )
            }

            BPMachineReadingType.ECG -> {
                mainTitleTextView.text = getString(R.string.ecg_title)
                mainDescriptionTextView.text = getString(R.string.ecg_desc)

                tvStep1Title.text = getString(R.string.ecg_step1_title)
                tvStep1.text = getString(R.string.ecg_step1_desc)

                tvStep2Title.text = getString(R.string.ecg_step2_title)
                tvStep2.text = getString(R.string.ecg_step2_desc)

                tvStep3Title.text = getString(R.string.ecg_step3_title)
                tvStep3.text = getString(R.string.ecg_step3_desc)

                btnStartMeasurement.text = getString(R.string.ecg_button)

                // Set Icons (Assuming you have these drawables)
                ivType.setImageResource(R.drawable.ic_heart_2)
                ivType.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.series))

                // Set Card Background Colors
                mcvIcon.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.series_14
                    )
                )
            }

            else -> {}
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
            binding.btnStartMeasurement -> {
                when (readingType) {
                    BPMachineReadingType.BLOOD_PRESSURE -> {
                        findNavController().navigate(
                            R.id.fragmentSmartCuffBPReading,
                            Bundle().apply {
                                putParcelable(NavKeys.WEARABLE, userDevice)
                                putParcelable(NavKeys.READING_TYPE, BPMachineReadingType.BLOOD_PRESSURE)
                            }
                        )
                    }

                    BPMachineReadingType.ECG -> {
                        findNavController().navigate(
                            R.id.fragmentSmartCuffECGReading,
                            Bundle().apply {
                                putParcelable(NavKeys.WEARABLE, userDevice)
                                putParcelable(NavKeys.READING_TYPE, BPMachineReadingType.ECG)
                            }
                        )
                    }

                    else -> {
                        return
                    }
                }
            }
        }
    }
}
