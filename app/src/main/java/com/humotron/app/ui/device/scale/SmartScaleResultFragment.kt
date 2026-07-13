package com.humotron.app.ui.device.scale

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSmartScaleResultBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SmartScaleResultFragment :
    BaseFragment(R.layout.fragment_smart_scale_result),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartScaleResultBinding
    private var userDevice: UserDevice? = null
    private var measuredAt: String? = null
    private var weightValue: String? = null
    private var bmiValue: String? = null
    private var bodyFatValue: String? = null
    private var leanBodyWeightValue: String? = null
    private var muscleMassValue: String? = null
    private var boneMassValue: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartScaleResultBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@SmartScaleResultFragment)
        binding.btnDone.setOnClickListener(this@SmartScaleResultFragment)
    }

    private fun initViews() = with(binding) {
        header.tvTitle.text = getString(R.string.take_a_reading)
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        measuredAt = arguments?.getString(NavKeys.SCALE_MEASURED_AT)
        weightValue = arguments?.getString(NavKeys.SCALE_WEIGHT)
        bmiValue = arguments?.getString(NavKeys.SCALE_BMI)
        bodyFatValue = arguments?.getString(NavKeys.SCALE_BODY_FAT)
        leanBodyWeightValue = arguments?.getString(NavKeys.SCALE_LEAN_BODY_WEIGHT)
        muscleMassValue = arguments?.getString(NavKeys.SCALE_MUSCLE_MASS)
        boneMassValue = arguments?.getString(NavKeys.SCALE_BONE_MASS)

        tvReadingStatus.text = getString(R.string.weight_scale_instruction_complete)
        tvTimestamp.text = buildTimestampText()

        tvLeadValue.text = weightValue ?: "--"
        tvLeadUnit.text = getString(R.string.metrics_weight_unit)
        tvLeadLabel.text = buildLeadLabel()

        tvBmiValue.text = bmiValue ?: "--"
        tvBodyFatValue.text = bodyFatValue ?: "--"
        tvLeanBodyValue.text = leanBodyWeightValue ?: "--"
        tvMuscleMassValue.text = muscleMassValue ?: "--"
        tvBoneMassValue.text = boneMassValue ?: "--"

        //tvLeadMetric.isVisible = true
        cvBmiMetric.isVisible = true
        cvBodyFatMetric.isVisible = true
        cvLeanBodyMetric.isVisible = true
        cvMuscleMassMetric.isVisible = true
        cvBoneMassMetric.isVisible = true
    }

    private fun buildTimestampText(): String {
        val deviceName = userDevice?.deviceFacingName ?: userDevice?.deviceName ?: "Smart Scale"
        val timestamp = measuredAt?.takeIf { it.isNotBlank() } ?: "just now"
        return "$deviceName - $timestamp"
    }

    private fun buildLeadLabel(): String {
        val weight = weightValue?.takeIf { it.isNotBlank() } ?: "--"
        return String.format(Locale.US, "Weight - %s kg", weight)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
            binding.btnDone -> findNavController().popBackStack(R.id.fragmentTrack, false)
        }
    }
}
