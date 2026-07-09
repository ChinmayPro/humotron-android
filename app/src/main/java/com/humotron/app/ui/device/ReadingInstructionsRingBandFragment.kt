package com.humotron.app.ui.device

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentReadingInstructionsRingBandBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import java.util.Locale.getDefault

@AndroidEntryPoint
class ReadingInstructionsRingBandFragment :
    BaseFragment(R.layout.fragment_reading_instructions_ring_band),
    View.OnClickListener {

    private lateinit var binding: FragmentReadingInstructionsRingBandBinding
    private var userDevice: UserDevice? = null
    private var healthScanItem: HealthScanItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReadingInstructionsRingBandBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@ReadingInstructionsRingBandFragment)
        binding.btnPastScans.setOnClickListener(this@ReadingInstructionsRingBandFragment)
        binding.btnStartScan.setOnClickListener(this@ReadingInstructionsRingBandFragment)
    }

    private fun initViews() = with(binding) {
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        healthScanItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM, HealthScanItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM)
        }

        healthScanItem?.let {
            val mTitle = healthScanItem?.title ?: ""
            header.tvTitle.text = mTitle
            binding.btnStartScan.text = getString(
                R.string.start_item,
                mTitle.lowercase(getDefault())
            )

            when (it.type) {
                HealthScanType.HRV -> {
                    setColorIcons(R.color.series, R.color.series_14, R.drawable.ic_heart_2)
                    binding.mainTitleTextView.setText(R.string.hrv_title)
                    binding.mainDescriptionTextView.setText(R.string.hrv_desc)
                    binding.tvUnit.setText(R.string.hrv_unit)
                    binding.hrvAverageLabelTextView.setText(R.string.hrv_value_label)

                    binding.instructionDescriptionTextView.setText(R.string.hrv_before_start_desc)
                    binding.whenToRunDescriptionTextView.setText(R.string.hrv_when_to_run_desc)
                    binding.whatItMeasuresDescriptionTextView.setText(R.string.hrv_what_measures_desc)
                    binding.whatToDoDescriptionTextView.setText(R.string.hrv_what_to_do_desc)
                    binding.footerDisclaimerTextView.setText(R.string.takes_about_2_minutes)
                }

                HealthScanType.HR -> {
                    setColorIcons(R.color.watch, R.color.watch_14, R.drawable.ic_wave_2)
                    binding.mainTitleTextView.setText(R.string.hr_title)
                    binding.mainDescriptionTextView.setText(R.string.hr_desc)
                    binding.tvUnit.setText(R.string.hr_unit)
                    binding.hrvAverageLabelTextView.setText(R.string.hr_value_label)

                    binding.instructionDescriptionTextView.setText(R.string.hr_before_start_desc)
                    binding.whenToRunDescriptionTextView.setText(R.string.hr_when_to_run_desc)
                    binding.whatItMeasuresDescriptionTextView.setText(R.string.hr_what_measures_desc)
                    binding.whatToDoDescriptionTextView.setText(R.string.hr_what_to_do_desc)
                    binding.footerDisclaimerTextView.setText(R.string.takes_about_1_minutes)
                }

                HealthScanType.SPO2 -> {
                    setColorIcons(R.color.cool, R.color.cool_14, R.drawable.ic_metrics_o2)
                    binding.mainTitleTextView.setText(R.string.spo2_title)
                    binding.mainDescriptionTextView.setText(R.string.spo2_desc)
                    binding.tvUnit.setText(R.string.spo2_unit)
                    binding.hrvAverageLabelTextView.setText(R.string.spo2_value_label)

                    binding.instructionDescriptionTextView.setText(R.string.spo2_before_start_desc)
                    binding.whenToRunDescriptionTextView.setText(R.string.spo2_when_to_run_desc)
                    binding.whatItMeasuresDescriptionTextView.setText(R.string.spo2_what_measures_desc)
                    binding.whatToDoDescriptionTextView.setText(R.string.spo2_what_to_do_desc)
                    binding.footerDisclaimerTextView.setText(R.string.takes_about_45_seconds)
                }

                HealthScanType.TEMPERATURE -> {
                    setColorIcons(R.color.series, R.color.series_14, R.drawable.ic_metrics_thermo)
                    binding.mainTitleTextView.setText(R.string.temp_title)
                    binding.mainDescriptionTextView.setText(R.string.temp_desc)
                    binding.tvUnit.setText(R.string.temp_unit)
                    binding.hrvAverageLabelTextView.setText(R.string.temp_value_label)

                    binding.instructionDescriptionTextView.setText(R.string.temp_before_start_desc)
                    binding.whenToRunDescriptionTextView.setText(R.string.temp_when_to_run_desc)
                    binding.whatItMeasuresDescriptionTextView.setText(R.string.temp_what_measures_desc)
                    binding.whatToDoDescriptionTextView.setText(R.string.temp_what_to_do_desc)
                    binding.footerDisclaimerTextView.setText(R.string.takes_about_1_minutes)

                }
            }
        }
        val deviceType = DeviceType.from(userDevice?.deviceName)
    }

    fun setColorIcons(mColor: Int, mColor14: Int, drawable: Int) {
        binding.ivType.setImageResource(drawable)
        binding.ivType.setColorFilter(ContextCompat.getColor(requireActivity(), mColor))
        binding.mcvIcon.setCardBackgroundColor(
            ContextCompat.getColor(requireActivity(), mColor14)
        )
        binding.tvValue.setTextColor(ContextCompat.getColor(requireActivity(), mColor))
        binding.btnPastScans.setTextColor(ContextCompat.getColor(requireActivity(), mColor))
        binding.btnPastScans.iconTint =
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), mColor))

        binding.whenToRunIconImageView.setColorFilter(
            ContextCompat.getColor(
                requireActivity(),
                mColor
            )
        )
        binding.whatItMeasuresIconImageView.setColorFilter(
            ContextCompat.getColor(
                requireActivity(),
                mColor
            )
        )
        binding.whatToDoIconImageView.setColorFilter(
            ContextCompat.getColor(
                requireActivity(),
                mColor
            )
        )
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
            binding.btnPastScans -> {
                findNavController().navigate(
                    R.id.fragmentPastScans,
                    bundleOf(
                        NavKeys.HEALTH_SCAN_ITEM to healthScanItem,
                        NavKeys.WEARABLE to userDevice
                    )
                )
            }

            binding.btnStartScan -> {
                findNavController().navigate(
                    R.id.fragmentRealTimeScanRingBand,
                    bundleOf(
                        NavKeys.HEALTH_SCAN_ITEM to healthScanItem,
                        NavKeys.WEARABLE to userDevice
                    )
                )
            }
        }
    }
}
