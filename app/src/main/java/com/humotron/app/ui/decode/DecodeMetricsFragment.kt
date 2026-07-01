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
                            val readingVal = it.metricReading
                            val readingStr = if (readingVal != null && readingVal.toString().trim().isNotEmpty()) {
                                "${readingVal} ${it.metricUnit ?: it.metricReadingUnit ?: ""}".trim()
                            } else {
                                ""
                            }
                            ActiveMetric(
                                id = it.id ?: "",
                                value = readingStr,
                                label = it.metricUserFacingName ?: it.metricName ?: "",
                                dateRange = it.metricDuration ?: "",
                                deviceName = it.deviceName ?: "",
                                status = it.status
                            )
                        }
                        setupActiveMetrics(activeMetrics)
                    }

                    resource.data?.data?.groupMetrics?.let { list ->
                        val groupMetrics = list.map {
                            ActiveMetric(
                                id = it.categoryId ?: "",
                                value = "",
                                label = it.categoryName ?: "",
                                dateRange = "",
                                deviceName = it.deviceName ?: "",
                                status = if (it.hasMinimumData == true) "ready" else "unready"
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
                putString("chat_prompt_title", getString(R.string.chat_analyze_metric_prompt, it.label ?: ""))
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
                putString("chat_prompt_title", getString(R.string.chat_analyze_metric_prompt, it.label ?: ""))
            }
            findNavController().navigate(R.id.fragmentTronChat, bundle)
        }
        DecodeAnimationUtils.animateCardsIn(binding.rvPendingMetrics)
    }
}
