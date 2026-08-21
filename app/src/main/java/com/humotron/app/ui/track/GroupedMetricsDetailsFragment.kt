package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentGroupedMetricsDetailsBinding
import com.humotron.app.databinding.ItemMetricBulletPointBinding
import com.humotron.app.domain.modal.response.GroupedMetricData
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupedMetricsDetailsFragment : Fragment(R.layout.fragment_grouped_metrics_details) {

    private lateinit var binding: FragmentGroupedMetricsDetailsBinding
    private val viewModel: GroupedMetricsDetailsViewModel by viewModels()
    private var groupedMetric: GroupedMetricData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGroupedMetricsDetailsBinding.bind(view)

        groupedMetric = arguments?.getParcelable(NavKeys.GROUPED_METRIC)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()
    }

    private fun initViews() {
        binding.header.tvTitle.text = groupedMetric?.categoryName ?: ""

        groupedMetric?.categoryId?.let {
            viewModel.getMetricCategoryDetailsById(it)
        }
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.groupedMetricsDetails.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val data = resource.data?.data
                    data?.let { details ->
                        binding.apply {
                            tvCategoryName.text = details.categoryName
                            tvStatus.text = getString(R.string.not_yet_tracking)

                            details.devices?.firstOrNull()?.deviceName?.let {
                                btnAppleWatch.text = it
                            }

                            // Metrics in card
                            details.moreMetrics?.getOrNull(0)?.let {
                                tvDistanceWalking.text = it.metricName
                                tvDistanceWalkingDesc.text = it.metricDescription
                            }
                            details.moreMetrics?.getOrNull(1)?.let {
                                tvFlightsClimbed.text = it.metricName
                                tvFlightsClimbedDesc.text = it.metricDescription
                            }
                            details.moreMetrics?.getOrNull(2)?.let {
                                tvStepCount.text = it.metricName
                                tvStepCountDesc.text = it.metricDescription
                            }

                            // What is section
                            tvWhatIsTitle.text = getString(R.string.what_is_activity_metrics)
                            tvIntroText.text = details.intro

                            // Why track section
                            tvWhyTrackTitle.text = getString(R.string.why_should_you_track, details.categoryName ?: "")
                            tvWhyTrackDesc.text = details.whyMatters

                            // How to track section
                            tvHowToTrackTitle.text = getString(R.string.how_to_track_effectively, details.categoryName ?: "")
                            tvBestPracticeDesc.text = details.practice
                            tvWhyContinuousMattersDesc.text = details.whyMatters

                            tvThingsAffectTitle.text = getString(R.string.things_that_affect_readings, details.categoryName ?: "")

                            // Bullet points
                            llThingsAffect.removeAllViews()
                            details.thingsAccuracy?.forEach { thing ->
                                val bulletBinding = ItemMetricBulletPointBinding.inflate(
                                    LayoutInflater.from(requireContext()),
                                    llThingsAffect,
                                    false
                                )
                                bulletBinding.tvBulletText.text = thing
                                llThingsAffect.addView(bulletBinding.root)
                            }
                        }
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    // Handle error
                }

                Status.LOADING -> {
                    // Show loading
                }
            }
        }
    }

    companion object {

    }
}