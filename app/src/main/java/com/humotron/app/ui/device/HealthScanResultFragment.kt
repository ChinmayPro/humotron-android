package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentHealthScanResultBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.ui.HealthScanResult
import com.humotron.app.util.analyzer.BPMLoadAnalyzer
import com.humotron.app.util.analyzer.HRVAnalyzer
import com.humotron.app.util.analyzer.SpO2Analyzer
import com.humotron.app.util.analyzer.ThermalAnalyzer
import com.humotron.app.view.LayerCard
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
    private var wearable: GetAllDeviceResponse.Data.Wearable? = null

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
            arguments?.getParcelable(
                NavKeys.WEARABLE,
                com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable::class.java
            )
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
        binding.tvCurrentHrvValue.text =
            String.format(Locale.getDefault(), "%.0f %s", current, unit)

        binding.tvAverage.text = getString(R.string.average_, type.getDisplayName2())
        binding.tvCurrent.text = getString(R.string.average_, type.getDisplayName2())

        val (title, color, description) = when (type) {
            HealthScanType.HRV -> {
                val state = HRVAnalyzer.analyze(baseLine.toDouble(), current.toDouble())
                Triple(state.title, state.color, state.description)
            }

            HealthScanType.TEMPERATURE -> {
                val state = ThermalAnalyzer.analyze(baseLine.toDouble(), current.toDouble())
                Triple(state.title, state.color, state.description)
            }

            HealthScanType.HR -> {
                val state = BPMLoadAnalyzer.analyze(baseLine.toDouble(), current.toDouble())
                Triple(state.title, state.color, state.description)
            }

            HealthScanType.SPO2 -> {
                val state = SpO2Analyzer.analyze(baseLine.toDouble(), current.toDouble())
                Triple(state.title, state.color, state.description)
            }

        }

        val cards = when (type) {
            HealthScanType.HRV -> {
                HRVAnalyzer.infoList.map {
                    LayerCard(it.value, it.title, it.description, it.color)
                }
            }

            HealthScanType.TEMPERATURE -> {
                ThermalAnalyzer.infoList.mapIndexed { index, it ->
                    LayerCard(index, it.title, it.description, it.color)
                }
            }

            HealthScanType.HR -> {
                BPMLoadAnalyzer.infoList.map {
                    LayerCard(it.value, it.title, it.description, it.color)
                }
            }

            HealthScanType.SPO2 -> {
                SpO2Analyzer.infoList.map {
                    LayerCard(it.value, it.title, it.description, it.color)
                }
            }
        }
        binding.layeredCardView.setCards(cards)

        binding.tvStatus.text = title
        binding.tvStatus.setTextColor(color)
        binding.tvMessage.text = description
        binding.mcvMessage.setCardBackgroundColor(color)

        when (type) {
            HealthScanType.HRV -> {
                binding.tvTitle.text = getString(R.string.current_stress_levels)
                binding.tvCurrentHrvValue.setTextColor(color)

                binding.ecvFaq1.setTitle(getString(R.string.faq_q1_hrv))
                binding.ecvFaq2.setTitle(getString(R.string.faq_q2_hrv))
                binding.ecvFaq3.setTitle(getString(R.string.faq_q3_hrv))

                binding.ecvFaq1.setDescription(getString(R.string.faq_a1_hrv))
                binding.ecvFaq2.setDescription(getString(R.string.faq_a2_hrv))
                binding.ecvFaq3.setDescription(getString(R.string.faq_a3_hrv))
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
