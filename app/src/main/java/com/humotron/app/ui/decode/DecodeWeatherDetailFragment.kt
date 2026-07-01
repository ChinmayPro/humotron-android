package com.humotron.app.ui.decode

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentWeatherDetailBinding
import com.humotron.app.domain.modal.response.WeatherAction
import com.humotron.app.domain.modal.response.WeatherDetailData
import com.humotron.app.domain.modal.response.WeatherObservation
import com.humotron.app.domain.modal.response.WeatherSection
import com.humotron.app.ui.decode.custom.ChartHelper
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeWeatherDetailFragment : BaseFragment(R.layout.fragment_weather_detail) {

    private lateinit var binding: FragmentWeatherDetailBinding
    private val viewModel: DecodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWeatherDetailBinding.bind(view)

        val weatherId = arguments?.getString("weatherId") ?: "69d76eb2656631a675f00569"

        initClicks()
        observeData()

        viewModel.getWeatherResilienceReportDetail(weatherId)
    }

    private fun initClicks() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeData() {
        viewModel.weatherDetailData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.shimmerWeatherDetail.visibility = View.VISIBLE
                    binding.shimmerWeatherDetail.startShimmer()
                    binding.llContent.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding.shimmerWeatherDetail.visibility = View.GONE
                    binding.shimmerWeatherDetail.stopShimmer()
                    binding.llContent.visibility = View.VISIBLE
                    resource.data?.data?.let { data ->
                        populateUI(data)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.shimmerWeatherDetail.visibility = View.GONE
                    binding.shimmerWeatherDetail.stopShimmer()
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateUI(data: WeatherDetailData) {
        binding.tvWeatherTitle.text = data.title
        val rangeStr = getString(
            R.string.paired_days_range_format,
            data.meta?.analysisWindow?.start ?: "",
            data.meta?.analysisWindow?.end ?: "",
            data.meta?.validDayCount ?: 0
        )
        binding.tvWeatherRange.text = rangeStr

        data.sections?.forEach { section ->
            when (section.section) {
                "weather_impact_summary" -> {
                    binding.tvWeatherSummary.text = section.narration
                    binding.tvImpactValue.text = getString(
                        R.string.percentage_format,
                        section.impact_score ?: 0
                    )
                    binding.tvImpactNote.text = section.key_finding

                    // Set Donut Progress
                    binding.donutProgress.setProgress(section.impact_score?.toFloat() ?: 0f)
                    try {
                        val color = Color.parseColor(section.score_color)
                        binding.donutProgress.setProgressColor(color)
                    } catch (e: Exception) {
                        binding.donutProgress.setProgressColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_watch))
                    }

                    populateImpactLevels(section)
                }
                "trend_visualization" -> {
                    binding.tvTrendTitle.text = section.title
                    setupCharts(section)
                    section.observations?.let { populateInsights(it) }
                }
                "personalized_action_plan" -> {
                    section.actions?.let { populatePlan(it) }
                }
            }
        }
    }

    private fun setupCharts(section: WeatherSection) {
        val seriesData = section.series ?: return
        if (seriesData.size < 2) return
        
        val color1 = ContextCompat.getColor(requireContext(), R.color.deep_dives_series)
        val color2 = ContextCompat.getColor(requireContext(), R.color.deep_dives_cool)
        
        val aVals = seriesData[0].data?.map { it.value ?: 0f } ?: emptyList()
        val bVals = seriesData[1].data?.map { it.value ?: 0f } ?: emptyList()

        val xLabels = seriesData[0].data?.map { it.date ?: "" } ?: emptyList()

        ChartHelper.setupLineChart(
            binding.chartTrend,
            listOf(
                ChartHelper.LineDatasetInfo(seriesData[0].label ?: "", aVals, color1),
                ChartHelper.LineDatasetInfo(seriesData[1].label ?: "", bVals, color2)
            ),
            xLabels
        )
    }

    private fun populateImpactLevels(section: WeatherSection) {
        val container = binding.llImpactLevel
        container.removeAllViews()

        val impactScale = section.impact_scale ?: return

        for (scale in impactScale) {
            val view = layoutInflater.inflate(R.layout.item_impact_level, container, false)
            val tvRange = view.findViewById<android.widget.TextView>(R.id.tvImpactLevelRange)
            val tvName = view.findViewById<android.widget.TextView>(R.id.tvImpactLevelName)
            
            tvRange.text = scale.range
            tvName.text = scale.label

            if (scale.is_active == true) {
                view.setBackgroundResource(R.drawable.bg_impact_level_selected)
                
                try {
                    val activeColor = Color.parseColor(section.score_color)
                    tvRange.setTextColor(activeColor)
                    tvName.setTextColor(activeColor)
                } catch (e: Exception) {
                    tvRange.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_watch))
                    tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_watch))
                }
            } else {
                view.background = null
                tvRange.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_ink2))
                tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_ink3))
            }

            container.addView(view)
        }
    }

    private fun populateInsights(insights: List<WeatherObservation>) {
        val container = binding.llInsights
        container.removeAllViews()
        
        for (insight in insights) {
            val view = layoutInflater.inflate(R.layout.item_weather_insight, container, false)
            view.findViewById<android.widget.TextView>(R.id.tvDate).text = insight.dates?.firstOrNull() ?: ""
            view.findViewById<android.widget.TextView>(R.id.tvTitle).text = insight.title
            view.findViewById<android.widget.TextView>(R.id.tvDesc).text = insight.description
            container.addView(view)
        }
    }

    private fun populatePlan(planList: List<WeatherAction>) {
        val container = binding.llPlan
        container.removeAllViews()
        
        for (plan in planList) {
            val view = layoutInflater.inflate(R.layout.item_weather_plan, container, false)
            val tvLetter = view.findViewById<android.widget.TextView>(R.id.tvLetter)
            
            tvLetter.text = plan.letter
            
            try {
                val letterBgColor = Color.parseColor(plan.letter_bg_color)
                val textColor = Color.parseColor(plan.text_color)
                tvLetter.backgroundTintList = android.content.res.ColorStateList.valueOf(letterBgColor)
                tvLetter.setTextColor(textColor)
            } catch (e: Exception) {
                tvLetter.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.deep_dives_lime)
                )
            }
            
            view.findViewById<android.widget.TextView>(R.id.tvPlanTitle).text = plan.title
            view.findViewById<android.widget.TextView>(R.id.tvPlanDesc).text = plan.description
            
            container.addView(view)
        }
    }
}
