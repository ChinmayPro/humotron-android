package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeInsightsBinding
import com.humotron.app.ui.decode.adapter.DecodeInsightsAdapter
import com.humotron.app.ui.decode.adapter.DisplayMetric
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import com.humotron.app.data.network.Status
import com.humotron.app.domain.modal.response.InsightMetricsOverviewData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeInsightsFragment : BaseFragment(R.layout.fragment_decode_insights) {

    private lateinit var binding: FragmentDecodeInsightsBinding
    private val viewModel: DecodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeInsightsBinding.bind(view)

        initViews()
    }

    private fun initViews() {
        initObservers()
        viewModel.getInsightMetricsOverview()
    }

    private fun initObservers() {
        viewModel.insightMetricsOverviewData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.shimmerMetricsList.startShimmer()
                    binding.shimmerMetricsList.visibility = View.VISIBLE
                    binding.rvMetricsList.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding.shimmerMetricsList.stopShimmer()
                    binding.shimmerMetricsList.visibility = View.GONE
                    binding.rvMetricsList.visibility = View.VISIBLE
                    resource.data?.data?.let { data ->
                        populateMetrics(data)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.shimmerMetricsList.stopShimmer()
                    binding.shimmerMetricsList.visibility = View.GONE
                    binding.rvMetricsList.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun populateMetrics(data: InsightMetricsOverviewData) {
        // 1. Map groupedMetrics
        val groupedList = data.groupedMetrics?.map {
            DisplayMetric(
                id = it.categoryId ?: "",
                name = it.categoryName ?: "",
                deviceName = it.deviceName ?: "",
                state = it.groupState ?: "",
                hasInsight = it.groupState?.contains("DATA_READY", ignoreCase = true) == true,
                availableDays = it.groupAvailableDays ?: 0,
                insightMinData = it.groupInsightMinData ?: 5,
                lastSyncDate = it.groupLastSyncDate
            )
        } ?: emptyList()

        // 2. Map individualMetrics
        val individualList = data.individualMetrics?.map {
            DisplayMetric(
                id = it.metricId ?: "",
                name = it.metricUserFacingName ?: it.metricName ?: "",
                deviceName = it.deviceName ?: "",
                state = it.state ?: "",
                hasInsight = it.state?.contains("DATA_READY", ignoreCase = true) == true,
                availableDays = it.availableDays ?: 0,
                insightMinData = it.insightMinData ?: 5,
                lastSyncDate = it.lastSyncDate
            )
        } ?: emptyList()

        // 3. Combine and sort: ready first, then alphabetical by name
        val combinedList = (groupedList + individualList).sortedWith(
            compareByDescending<DisplayMetric> { it.hasInsight }
                .thenBy { it.name }
        )

        // 4. Set layout manager and adapter
        binding.rvMetricsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMetricsList.adapter = DecodeInsightsAdapter(combinedList) { metric ->
            navigateToPatternWindow(metric.id, metric.name, metric.hasInsight)
        }

        // Animate cards staggered
        DecodeAnimationUtils.animateCardsIn(binding.rvMetricsList)
    }

    private fun navigateToPatternWindow(metricId: String, metricName: String, hasInsight: Boolean) {
        val bundle = Bundle().apply {
            putString("metricId", metricId)
            putString("metricName", metricName)
            putBoolean("hasInsight", hasInsight)
        }
        findNavController().navigate(R.id.action_fragmentDecode_to_fragmentDecodePatternWindow, bundle)
    }
}
