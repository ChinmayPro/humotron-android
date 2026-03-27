package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeMetricsBinding
import com.humotron.app.domain.modal.ui.ActiveMetric
import com.humotron.app.domain.modal.ui.PendingMetric
import com.humotron.app.ui.decode.adapter.ActiveMetricAdapter
import com.humotron.app.ui.decode.adapter.PendingMetricAdapter
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import com.humotron.app.data.network.Resource
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
        initObservers()
        viewModel.getMetricTrackingByUserId()
        viewModel.getYetToTrackMetricByUserId()
    }

    private fun initObservers() {
        viewModel.metricTrackingData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showActiveShimmer(true)
                }
                Status.SUCCESS -> {
                    showActiveShimmer(false)
                    resource.data?.data?.let { list ->
                        val activeMetrics = list.map {
                            ActiveMetric(
                                it.id ?: "",
                                "${it.metricReading} ${it.metricUnit ?: it.metricReadingUnit ?: ""}",
                                it.metricUserFacingName ?: it.metricName ?: "",
                                it.metricDuration ?: ""
                            )
                        }
                        setupActiveMetrics(activeMetrics)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    showActiveShimmer(false)
                }
            }
        }

        viewModel.yetToTrackMetricsData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showPendingShimmer(true)
                }
                Status.SUCCESS -> {
                    showPendingShimmer(false)
                    resource.data?.data?.let { list ->
                        val pendingMetrics = list.map {
                            PendingMetric(it.id ?: "", it.metricUserFacingName ?: it.metricName ?: "")
                        }
                        setupPendingMetrics(pendingMetrics)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
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
    }

    private fun setupActiveMetrics(items: List<ActiveMetric>) {
        binding.rvActiveMetrics.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvActiveMetrics.adapter = ActiveMetricAdapter(items) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_metric", it)
            findNavController().popBackStack()
        }
    }

    private fun setupPendingMetrics(items: List<PendingMetric>) {
        binding.rvPendingMetrics.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPendingMetrics.adapter = PendingMetricAdapter(items) {
            // Handle click
        }
    }
}
