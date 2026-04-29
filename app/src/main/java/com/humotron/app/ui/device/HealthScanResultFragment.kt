package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentHealthScanResultBinding
import com.humotron.app.domain.modal.ui.HealthScanResult
import com.humotron.app.ui.HRVAnalyzer
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HealthScanResultFragment : BaseFragment(R.layout.fragment_health_scan_result) {

    private lateinit var binding: FragmentHealthScanResultBinding
    private var result: HealthScanResult? = null
    private var healthScanItem: HealthScanItem? = null
    private var baseLine: Float = 0f
    private var current: Float = 0f
    private var wearable: com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHealthScanResultBinding.bind(view)

        initData()
        initUI()
        initClicks()
    }

    private fun initData() {
        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("result", HealthScanResult::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("result")
        }

        healthScanItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM, HealthScanItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM)
        }

        baseLine = arguments?.getFloat("baseLine") ?: 0f
        current = arguments?.getFloat("current") ?: 0f
        wearable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }
    }

    private fun initUI() {
        val type = healthScanItem?.type ?: HealthScanType.HRV
        binding.header.title.text = type.getDisplayName3()

        val unit = type.getUnit()
        binding.tvAvgHrvValue.text = String.format(Locale.getDefault(), "%.0f %s", baseLine, unit)
        binding.tvCurrentHrvValue.text = String.format(Locale.getDefault(), "%.0f %s", current, unit)

        val state = HRVAnalyzer.analyze(baseLine.toDouble(), current.toDouble())
        binding.tvStatus.text = state.title
        binding.tvStatus.setTextColor(state.color)
        binding.tvMessage.text=state.description

        when (type) {
            HealthScanType.HRV -> {
                binding.tvTitle.text = getString(R.string.current_stress_levels)
                binding.tvCurrentHrvValue.setTextColor(state.color)
            }

            HealthScanType.HR -> {
                binding.tvTitle.text = getString(R.string.heart_rate)
            }

            HealthScanType.SPO2 -> {
                binding.tvTitle.text = "Blood Oxygen Level"
            }

            HealthScanType.TEMPERATURE -> {
                binding.tvTitle.text = "Body Temperature"
            }
        }

        result?.let {
            val sdf = SimpleDateFormat("h:mm a  |  MMM dd, yyyy", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(Date(it.timestamp))
        }
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
