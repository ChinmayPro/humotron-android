package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentScanReportRingBandBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.domain.modal.response.PastScanData
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.analyzer.BPMLoadAnalyzer
import com.humotron.app.util.analyzer.HRVAnalyzer
import com.humotron.app.util.analyzer.SpO2Analyzer
import com.humotron.app.util.analyzer.ThermalAnalyzer
import com.humotron.app.util.utcOffsetToLocalTime
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ScanReportRingBandFragment :
    BaseFragment(R.layout.fragment_scan_report_ring_band),
    View.OnClickListener {

    private lateinit var binding: FragmentScanReportRingBandBinding
    private var userDevice: UserDevice? = null
    private var healthScanItem: HealthScanItem? = null
    private var pastScanData: PastScanData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanReportRingBandBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
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

        pastScanData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(NavKeys.PAST_SCAN, PastScanData::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(NavKeys.PAST_SCAN) as? PastScanData
        }

        bindReport()
    }

    private fun bindReport() = with(binding) {
        val type = HealthScanType.values().firstOrNull { it.value == pastScanData?.type }
            ?: healthScanItem?.type
            ?: HealthScanType.HRV
        val baseline = pastScanData?.baseline ?: 0.0
        val current = pastScanData?.current ?: 0.0
        val createdAt = utcOffsetToLocalTime(pastScanData?.createdAt, "dd MMM yyyy, hh:mm a")

        val (title, color, description) = when (type) {
            HealthScanType.HRV -> {
                val state = HRVAnalyzer.analyze(baseline, current)
                Triple(state.title, state.color, state.description)
            }

            HealthScanType.HR -> {
                val state = BPMLoadAnalyzer.analyze(baseline, current)
                Triple(state.title, state.color, state.description)
            }

            HealthScanType.SPO2 -> {
                val state = SpO2Analyzer.analyze(baseline, current)
                Triple(state.title, state.color, state.description)
            }

            HealthScanType.TEMPERATURE -> {
                val state = ThermalAnalyzer.analyze(baseline, current)
                Triple(state.title, state.color, state.description)
            }
        }

        header.tvTitle.text = healthScanItem?.title ?: type.getDisplayName()
        tvMainTitle.text = title
        tvMainTitle.setTextColor(color)
        tvSubtitle.text = if (createdAt.isBlank()) type.getDisplayName2() else createdAt
        tvStatusMessage.text = description
        tvStatusMessage.setTextColor(color)

        tvBaselineVal.text = String.format(Locale.getDefault(), "%.1f", baseline)
        tvCurrentVal.text = String.format(Locale.getDefault(), "%.1f", current)
        tvBaselineUnit.text = type.getUnit()
        tvCurrentUnit.text = type.getUnit()
        tvMeansDesc.text = description
        tvBaselineTitle.text = getString(R.string.baseline)
        tvCurrentTitle.text = getString(R.string.current_, type.getDisplayName2())
        tvMeansTitle.text = "What this means"
        tvNextTitle.text = "What to do next"
    }

    override fun onClick(v: View?) {
        when (v) {
            //binding.header.btnBack -> findNavController().popBackStack()
        }
    }
}
