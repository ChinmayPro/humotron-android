package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentWeatherDetailBinding
import com.humotron.app.ui.decode.custom.ChartHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeWeatherDetailFragment : BaseFragment(R.layout.fragment_weather_detail) {

    private lateinit var binding: FragmentWeatherDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWeatherDetailBinding.bind(view)

        val weatherId = arguments?.getString("weatherId") ?: "a"
        val detail = com.humotron.app.ui.decode.data.DeepDivesMockData.WEATHER_DETAILS[weatherId]

        initClicks()
        
        if (detail != null) {
            populateUI(detail)
            setupCharts(detail)
            populateImpactLevels(detail)
            populateInsights(detail.ins)
            populatePlan(detail.plan)
        }
    }

    private fun initClicks() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun populateUI(detail: com.humotron.app.ui.decode.data.WeatherDetail) {
        binding.tvWeatherTitle.text = detail.title
        binding.tvWeatherRange.text = detail.range
        binding.tvWeatherSummary.text = detail.summary
        binding.tvImpactValue.text = "${detail.impact}%"
        binding.tvImpactNote.text = detail.impactNote
        binding.tvTrendTitle.text = "${detail.a} vs ${detail.b}"
    }

    private fun setupCharts(detail: com.humotron.app.ui.decode.data.WeatherDetail) {
        // Set Donut Progress
        binding.donutProgress.setProgress(detail.impact.toFloat())
        
        // Color changes depending on level, we map loosely based on HTML: 
        // HTML has levels like 40-59 with class `b-watch`. We'll just use deep_dives_watch generally
        // or conditionally. For simplicity, just use watch for now or check level.
        val progressColor = if (detail.impact >= 50) {
            ContextCompat.getColor(requireContext(), R.color.deep_dives_attention)
        } else {
            ContextCompat.getColor(requireContext(), R.color.deep_dives_watch)
        }
        binding.donutProgress.setProgressColor(progressColor)

        // Set Trend Line Chart
        val color1 = ContextCompat.getColor(requireContext(), R.color.deep_dives_series)
        val color2 = ContextCompat.getColor(requireContext(), R.color.deep_dives_cool)
        
        val aVals = detail.aPts.map { it.toFloat() }
        val bVals = detail.bPts.map { it.toFloat() }

        ChartHelper.setupLineChart(
            binding.chartTrend,
            listOf(
                ChartHelper.LineDatasetInfo(detail.a, aVals, color1),
                ChartHelper.LineDatasetInfo(detail.b, bVals, color2)
            ),
            (1..aVals.size).map { it.toString() } // Fake x-axis
        )
    }

    private fun populateImpactLevels(detail: com.humotron.app.ui.decode.data.WeatherDetail) {
        val container = binding.llImpactLevel
        container.removeAllViews()

        val levels = listOf(
            "0–19" to "Minimal",
            "20–39" to "Mild",
            "40–59" to "Moderate",
            "60–79" to "Strong",
            "80–100" to "Very strong"
        )

        for ((range, name) in levels) {
            val view = layoutInflater.inflate(R.layout.item_impact_level, container, false)
            val tvRange = view.findViewById<android.widget.TextView>(R.id.tvImpactLevelRange)
            val tvName = view.findViewById<android.widget.TextView>(R.id.tvImpactLevelName)
            
            tvRange.text = range
            tvName.text = name

            if (range == detail.level) {
                view.setBackgroundResource(R.drawable.bg_impact_level_selected)
                tvRange.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_watch))
                tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_watch))
            } else {
                view.background = null
                tvRange.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_ink2))
                tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_dives_ink3))
            }

            container.addView(view)
        }
    }

    private fun populateInsights(insights: List<com.humotron.app.ui.decode.data.WeatherInsight>) {
        val container = binding.llInsights
        container.removeAllViews()
        
        for (insight in insights) {
            val view = layoutInflater.inflate(R.layout.item_weather_insight, container, false)
            view.findViewById<android.widget.TextView>(R.id.tvDate).text = insight.date
            view.findViewById<android.widget.TextView>(R.id.tvTitle).text = insight.title
            view.findViewById<android.widget.TextView>(R.id.tvDesc).text = insight.description
            container.addView(view)
        }
    }

    private fun populatePlan(planList: List<com.humotron.app.ui.decode.data.WeatherPlan>) {
        val container = binding.llPlan
        container.removeAllViews()
        
        for (plan in planList) {
            val view = layoutInflater.inflate(R.layout.item_weather_plan, container, false)
            val tvLetter = view.findViewById<android.widget.TextView>(R.id.tvLetter)
            
            tvLetter.text = plan.letter
            
            // Map the colorCode string to an actual resource
            val bgColorRes = when (plan.colorCode) {
                "lime" -> R.color.deep_dives_lime
                "cool" -> R.color.deep_dives_cool
                "watch" -> R.color.deep_dives_watch
                "attention" -> R.color.deep_dives_attention
                else -> R.color.deep_dives_lime
            }
            
            tvLetter.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), bgColorRes)
            )
            
            view.findViewById<android.widget.TextView>(R.id.tvPlanTitle).text = plan.title
            view.findViewById<android.widget.TextView>(R.id.tvPlanDesc).text = plan.description
            
            container.addView(view)
        }
    }
}
