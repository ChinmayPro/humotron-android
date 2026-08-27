package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeMetricsBinding
import com.humotron.app.domain.modal.ui.ActiveMetric
import com.humotron.app.ui.decode.adapter.ActiveMetricAdapter
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import com.humotron.app.data.network.Status
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class DecodeMetricsFragment : BaseFragment(R.layout.fragment_decode_metrics) {

    private lateinit var binding: FragmentDecodeMetricsBinding
    private val viewModel: DecodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeMetricsBinding.bind(view)

        initViews()
        initClicks()
    }

    private fun initViews() {
        binding.header.title.text = getString(R.string.chat_choose_metrics_header)
        initObservers()
        viewModel.getHealthMetricTrackingByUserId()
    }

    private fun formatMetricDate(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return ""
        return try {
            val tsClean = timestamp.replace("Z", "+0000")
            val sdfIn = if (tsClean.contains("+")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
            } else {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            }
            val date = sdfIn.parse(tsClean)
            if (date != null) {
                SimpleDateFormat("d MMM yy", Locale.ENGLISH).format(date)
            } else ""
        } catch (e: Exception) {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(timestamp.take(10))
                if (date != null) {
                    SimpleDateFormat("d MMM yy", Locale.ENGLISH).format(date)
                } else ""
            } catch (e2: Exception) {
                ""
            }
        }
    }

    private fun initObservers() {
        viewModel.metricTrackingData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showActiveShimmer(true)
                    showPendingShimmer(true)
                }
                Status.SUCCESS -> {
                    showActiveShimmer(false)
                    showPendingShimmer(false)
                    
                    resource.data?.data?.individualMetrics?.let { list ->
                        val activeMetrics = list.map {
                            val readingVal = it.metricReading ?: it.metricValue?.value
                            val valStr = if (!readingVal.isNullOrBlank()) {
                                "${readingVal} ${it.metricUnit ?: it.metricReadingUnit ?: ""}".trim()
                            } else {
                                ""
                            }
                            val dateStr = formatMetricDate(it.metricValue?.timestamp)
                            val displayValue = when {
                                valStr.isNotEmpty() && dateStr.isNotEmpty() -> "$valStr · $dateStr"
                                valStr.isNotEmpty() -> valStr
                                dateStr.isNotEmpty() -> dateStr
                                else -> ""
                            }
                            val isReady = it.hasMinimumData == true
                            ActiveMetric(
                                id = it.id ?: "",
                                value = displayValue,
                                label = it.metricUserFacingName ?: it.metricName ?: "",
                                dateRange = it.metricDuration ?: "",
                                deviceName = it.deviceName ?: "",
                                status = if (isReady) "ready" else "unready"
                            )
                        }
                        setupActiveMetrics(activeMetrics)
                    }

                    resource.data?.data?.groupMetrics?.let { list ->
                        val groupMetrics = list.map {
                            val isReady = it.hasMinimumData == true
                            ActiveMetric(
                                id = it.categoryId ?: "",
                                value = "",
                                label = it.categoryName ?: "",
                                dateRange = "",
                                deviceName = it.deviceName ?: "",
                                status = if (isReady) "ready" else "unready"
                            )
                        }
                        setupGroupMetrics(groupMetrics)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    showActiveShimmer(false)
                    showPendingShimmer(false)
                }
            }
        }
    }

    private fun showActiveShimmer(show: Boolean) {
        binding.shimmerActiveView.isVisible = show
        if (show) binding.shimmerActiveView.startShimmer() else binding.shimmerActiveView.stopShimmer()
        binding.rvActiveMetrics.isVisible = !show
    }

    private fun showPendingShimmer(show: Boolean) {
        binding.shimmerPendingView.isVisible = show
        if (show) binding.shimmerPendingView.startShimmer() else binding.shimmerPendingView.stopShimmer()
        binding.rvPendingMetrics.isVisible = !show
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupActiveMetrics(items: List<ActiveMetric>) {
        binding.rvActiveMetrics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveMetrics.adapter = ActiveMetricAdapter(items) {
            // Navigate directly to TronChat — preserves backstack: Decode → Metrics → TronChat
            val bundle = Bundle().apply {
                putString("chat_prompt_id", it.id)
                putString("chat_prompt_title", getString(R.string.chat_analyze_metric_prompt, it.label))
            }
            findNavController().navigate(R.id.fragmentTronChat, bundle)
        }
        DecodeAnimationUtils.animateCardsIn(binding.rvActiveMetrics)
    }

    private fun setupGroupMetrics(items: List<ActiveMetric>) {
        binding.rvPendingMetrics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingMetrics.adapter = ActiveMetricAdapter(items) {
            // Navigate directly to TronChat — preserves backstack: Decode → Metrics → TronChat
            val bundle = Bundle().apply {
                putString("chat_prompt_id", it.id)
                putString("chat_prompt_title", getString(R.string.chat_analyze_metric_prompt, it.label))
            }
            findNavController().navigate(R.id.fragmentTronChat, bundle)
        }
        DecodeAnimationUtils.animateCardsIn(binding.rvPendingMetrics)
    }
}
