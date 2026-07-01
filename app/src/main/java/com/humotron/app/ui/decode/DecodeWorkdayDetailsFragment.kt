package com.humotron.app.ui.decode

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentWorkdayDetailsBinding
import com.humotron.app.domain.modal.response.WorkdayStressReportDetailData
import com.humotron.app.ui.decode.custom.ChartHelper
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@AndroidEntryPoint
class DecodeWorkdayDetailsFragment : BaseFragment(R.layout.fragment_workday_details) {

    private lateinit var binding: FragmentWorkdayDetailsBinding
    private val viewModel: DecodeViewModel by viewModels()
    private var reportId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWorkdayDetailsBinding.bind(view)

        reportId = arguments?.getString("reportId") ?: ""

        initViews()
        initClicks()
        setupObservers()

        if (reportId.isNotEmpty()) {
            viewModel.getWorkdayStressReportById(reportId)
        }
    }

    private fun initViews() {
        binding.header.title.text = "Workday Stress Report"
        binding.ivInfo.visibility = View.VISIBLE
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.workdayStressReportDetailData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.shimmerContainer.visibility = View.VISIBLE
                    binding.scrollContent.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding.shimmerContainer.visibility = View.GONE
                    binding.scrollContent.visibility = View.VISIBLE
                    resource.data?.data?.let { data ->
                        populateReportData(data)
                    }
                }
                else -> {
                    binding.shimmerContainer.visibility = View.GONE
                    binding.scrollContent.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun populateReportData(data: WorkdayStressReportDetailData) {
        // Date range
        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val sdfOut = SimpleDateFormat("MMM dd", Locale.ENGLISH)

        val startStr = data.analysisWindow?.start?.let { d ->
            try { sdfIn.parse(d)?.let { sdfOut.format(it) } } catch (e: Exception) { d }
        } ?: ""
        val endStr = data.analysisWindow?.end?.let { d ->
            try { sdfIn.parse(d)?.let { sdfOut.format(it) } } catch (e: Exception) { d }
        } ?: ""
        val dayCount = data.validDayCount ?: 0

        binding.tvDateRange.text = "$startStr – $endStr · Workday 9am – 5pm · $dayCount days of data"

        // Avg score
        val avgStress = data.summary?.avgWorkdayStress
        if (avgStress != null) {
            val scoreText = if (avgStress == avgStress.toLong().toDouble()) {
                avgStress.toLong().toString()
            } else {
                avgStress.toString()
            }
            binding.tvAvgScore.text = scoreText

            val colorStr = data.summary.stressLevelColor
            if (!colorStr.isNullOrEmpty()) {
                try {
                    binding.tvAvgScore.setTextColor(Color.parseColor(colorStr))
                } catch (_: Exception) {}
            }
        }

        // Zone badge
        val stressLevel = data.summary?.stressLevel
        if (!stressLevel.isNullOrEmpty()) {
            binding.tvZoneBadge.text = stressLevel
            val colorStr = data.summary.stressLevelColor
            if (!colorStr.isNullOrEmpty()) {
                try {
                    binding.tvZoneBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(
                        Color.parseColor(colorStr)
                    )
                } catch (_: Exception) {}
            }
        }

        // Stat grid
        data.summary?.highestWeek?.let { v ->
            binding.tvHighestWeek.text = if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
        }
        data.summary?.lowestWeek?.let { v ->
            binding.tvLowestWeek.text = if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
        }
        data.summary?.bestWeek?.let { v ->
            binding.tvBestWeek.text = if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
        }

        // Sections
        populateSections(data)

        // Disclaimer
        binding.tvDisclaimer.text = "Composed from your anonymised workday signals · $dayCount days"
    }

    private fun populateSections(data: WorkdayStressReportDetailData) {
        val sections = data.sections
        if (sections == null) {
            binding.tvDisclaimer.text = "Error: sections is null\nRaw Data:\n" + com.google.gson.Gson().toJson(data)
            return
        }
        
        binding.llSections.removeAllViews()
        
        // Update disclaimer
        binding.tvDisclaimer.text = "Composed from your anonymised workday signals · ${data.validDayCount ?: 13} days"

        for (section in sections) {
            val sectionId = section.section ?: continue

            // Inflate Header
            val headerView = layoutInflater.inflate(R.layout.layout_workday_section_header, binding.llSections, false)
            val tvSectionLabel = headerView.findViewById<android.widget.TextView>(R.id.tvSectionLabel)
            val tvSectionTitle = headerView.findViewById<android.widget.TextView>(R.id.tvSectionTitle)
            val tvSectionDesc = headerView.findViewById<android.widget.TextView>(R.id.tvSectionDesc)

            val label = section.title ?: sectionId.replace("_", " ")
            tvSectionLabel.text = label
            tvSectionTitle.text = section.headline ?: ""
            tvSectionDesc.text = section.narration ?: ""
            
            if (section.headline.isNullOrEmpty()) {
                tvSectionTitle.visibility = View.GONE
            }
            if (section.narration.isNullOrEmpty()) {
                tvSectionDesc.visibility = View.GONE
            }

            binding.llSections.addView(headerView)

            // Render specific chart type
            val rendered = when (section.displayMode) {
                "vertical_bar_chart" -> renderVerticalBarChart(section)
                "horizontal_bar_chart" -> renderHorizontalBars(section)
                "line_chart" -> renderLineChart(section)
                "stat_card", "badge" -> renderComparisonCards(section)
                "action_list" -> true // Handled elsewhere or just show header
                else -> {
                    when (sectionId) {
                        "weekly_stress_trend" -> renderVerticalBarChart(section)
                        "start_of_day_ramp", "late_day_wind_down" -> renderLineChart(section)
                        "meeting_load_and_stress", "back_to_back_meetings", "weekdays_vs_weekends" -> renderHorizontalBars(section)
                        "best_day_and_worst_day", "stress_fragmentation", "best_day_worst_day_examples" -> renderComparisonCards(section)
                        else -> false
                    }
                }
            }
            
            if (!rendered) {
                val placeholder = layoutInflater.inflate(R.layout.layout_workday_no_data, binding.llSections, false)
                binding.llSections.addView(placeholder)
            }
        }
    }

    private fun extractArray(data: com.google.gson.JsonElement?): com.google.gson.JsonArray? {
        if (data == null) return null
        var array: com.google.gson.JsonArray? = null
        if (data.isJsonArray) {
            array = data.asJsonArray
        } else if (data.isJsonObject) {
            val obj = data.asJsonObject
            array = obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
        }
        
        if (array == null) return null
        
        if (array.size() == 1 && array[0].isJsonObject) {
            val innerObj = array[0].asJsonObject
            if (innerObj.has("data") && innerObj.get("data").isJsonArray) {
                return innerObj.get("data").asJsonArray
            }
        }
        return array
    }

    private fun extractBars(data: com.google.gson.JsonElement?): List<com.humotron.app.domain.modal.response.WorkdayReportBar>? {
        val array = extractArray(data) ?: return null
        val list = mutableListOf<com.humotron.app.domain.modal.response.WorkdayReportBar>()
        for (item in array) {
            if (item.isJsonObject) {
                val obj = item.asJsonObject
                val label = obj.get("label")?.asString ?: obj.get("name")?.asString ?: ""
                val value = obj.get("value")?.asFloat ?: obj.get("val")?.asFloat ?: obj.get("score")?.asFloat ?: 0f
                val pct = obj.get("percentage")?.asFloat ?: obj.get("pct")?.asFloat ?: 0f
                val color = obj.get("color")?.asString
                list.add(com.humotron.app.domain.modal.response.WorkdayReportBar(label, value, pct, color))
            }
        }
        return list
    }

    private fun extractSeries(data: com.google.gson.JsonElement?): List<com.humotron.app.domain.modal.response.WorkdayReportSeries>? {
        val array = extractArray(data) ?: return null
        val list = mutableListOf<com.humotron.app.domain.modal.response.WorkdayReportSeries>()
        for (item in array) {
            if (item.isJsonObject) {
                val obj = item.asJsonObject
                val label = obj.get("label")?.asString ?: obj.get("name")?.asString ?: obj.get("date")?.asString ?: ""
                val ptsArray = obj.get("data")?.asJsonArray ?: obj.get("points")?.asJsonArray ?: obj.get("pts")?.asJsonArray ?: obj.get("seriesData")?.asJsonArray
                val points = mutableListOf<com.humotron.app.domain.modal.response.WorkdayReportSeriesPoint>()
                if (ptsArray != null) {
                    for (p in ptsArray) {
                        if (p.isJsonObject) {
                            val px = p.asJsonObject.get("x")?.asString ?: p.asJsonObject.get("name")?.asString ?: p.asJsonObject.get("time")?.asString ?: ""
                            val py = p.asJsonObject.get("y")?.asFloat ?: p.asJsonObject.get("val")?.asFloat ?: p.asJsonObject.get("value")?.asFloat ?: 0f
                            points.add(com.humotron.app.domain.modal.response.WorkdayReportSeriesPoint(px, py))
                        } else if (p.isJsonPrimitive && p.asJsonPrimitive.isNumber) {
                            points.add(com.humotron.app.domain.modal.response.WorkdayReportSeriesPoint("", p.asFloat))
                        }
                    }
                }
                list.add(com.humotron.app.domain.modal.response.WorkdayReportSeries(label, points))
            }
        }
        return list
    }

    private fun extractComparisons(data: com.google.gson.JsonElement?): List<com.humotron.app.domain.modal.response.WorkdayReportComparison>? {
        val array = extractArray(data) ?: return null
        val list = mutableListOf<com.humotron.app.domain.modal.response.WorkdayReportComparison>()
        for (item in array) {
            if (item.isJsonObject) {
                val obj = item.asJsonObject
                val label = obj.get("label")?.asString ?: obj.get("cmpt")?.asString ?: obj.get("title")?.asString ?: ""
                val date = obj.get("date")?.asString ?: obj.get("cmpd")?.asString ?: ""
                val day = obj.get("day")?.asString ?: obj.get("cmpm")?.asString ?: obj.get("subtext")?.asString ?: ""
                val score = obj.get("score")?.asInt ?: obj.get("val")?.asInt ?: obj.get("value")?.asInt ?: 0
                val color = obj.get("color")?.asString
                list.add(com.humotron.app.domain.modal.response.WorkdayReportComparison(label, date, day, score, color))
            }
        }
        return list
    }

    private fun renderVerticalBarChart(section: com.humotron.app.domain.modal.response.WorkdayReportSection): Boolean {
        val bars = section.bars ?: extractBars(section.data)
        if (bars.isNullOrEmpty()) return false

        val view = layoutInflater.inflate(R.layout.layout_workday_vertical_bar_chart, binding.llSections, false)
        val chart = view.findViewById<com.github.mikephil.charting.charts.BarChart>(R.id.chartBar)
        
        val barValues = bars.map { it.value ?: 0f }
        val barLabels = bars.map { it.label ?: "" }
        val barColor = ContextCompat.getColor(requireContext(), R.color.deep_dives_series)
        
        ChartHelper.setupVerticalBarChart(chart, barValues, barLabels, barColor)
        binding.llSections.addView(view)
        return true
    }

    private fun renderLineChart(section: com.humotron.app.domain.modal.response.WorkdayReportSection): Boolean {
        val seriesList = section.series ?: extractSeries(section.data)
        if (seriesList.isNullOrEmpty()) return false

        val view = layoutInflater.inflate(R.layout.layout_workday_line_chart, binding.llSections, false)
        val chart = view.findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.chartLine)

        val color1 = ContextCompat.getColor(requireContext(), R.color.deep_dives_series)
        val color2 = ContextCompat.getColor(requireContext(), R.color.deep_dives_lime)
        val color3 = ContextCompat.getColor(requireContext(), R.color.deep_dives_cool)
        val colors = listOf(color1, color2, color3)

        val chartSeries = seriesList.mapIndexed { index, series ->
            val points = series.data?.map { it.y ?: 0f } ?: emptyList()
            ChartHelper.LineDatasetInfo(
                series.label ?: "Series ${index + 1}",
                points,
                colors[index % colors.size]
            )
        }
        val xLabels = seriesList.firstOrNull()?.data?.map { it.x ?: "" } ?: emptyList()
        ChartHelper.setupLineChart(chart, chartSeries, xLabels)
        
        binding.llSections.addView(view)
        return true
    }

    private fun renderHorizontalBars(section: com.humotron.app.domain.modal.response.WorkdayReportSection): Boolean {
        val bars = section.bars ?: extractBars(section.data)
        if (bars.isNullOrEmpty()) return false

        val view = layoutInflater.inflate(R.layout.layout_workday_horizontal_bars, binding.llSections, false)
        val container = view.findViewById<android.widget.LinearLayout>(R.id.llHorizontalBarsContainer)

        for (bar in bars) {
            val itemView = layoutInflater.inflate(R.layout.layout_workday_horizontal_bar_item, container, false)
            val tvLabel = itemView.findViewById<android.widget.TextView>(R.id.tvLabel)
            val tvValue = itemView.findViewById<android.widget.TextView>(R.id.tvValue)
            val vProgressFill = itemView.findViewById<android.view.View>(R.id.vProgressFill)
            val vProgressEmpty = itemView.findViewById<android.view.View>(R.id.vProgressEmpty)

            tvLabel.text = bar.label ?: ""
            val rawValue = bar.value ?: 0f
            tvValue.text = if (rawValue == rawValue.toLong().toFloat()) rawValue.toLong().toString() else rawValue.toString()

            val pct = bar.percentage ?: (if (rawValue > 100) 100f else rawValue)
            val weightFill = pct / 100f
            val weightEmpty = 1f - weightFill
            
            // Set weight on fill
            val fillParams = vProgressFill.layoutParams as android.widget.LinearLayout.LayoutParams
            fillParams.weight = weightFill
            vProgressFill.layoutParams = fillParams

            val emptyParams = vProgressEmpty.layoutParams as android.widget.LinearLayout.LayoutParams
            emptyParams.weight = weightEmpty
            vProgressEmpty.layoutParams = emptyParams

            if (!bar.color.isNullOrEmpty()) {
                try {
                    vProgressFill.setBackgroundColor(Color.parseColor(bar.color))
                } catch (e: Exception) {}
            }
            container.addView(itemView)
        }

        binding.llSections.addView(view)
        return true
    }

    private fun renderComparisonCards(section: com.humotron.app.domain.modal.response.WorkdayReportSection): Boolean {
        val comparisons = section.comparisons ?: extractComparisons(section.data)
        if (comparisons.isNullOrEmpty() || comparisons.size < 2) return false

        val view = layoutInflater.inflate(R.layout.layout_workday_comparison_cards, binding.llSections, false)
        
        val tvCard1Value = view.findViewById<android.widget.TextView>(R.id.tvCard1Value)
        val tvCard1Desc = view.findViewById<android.widget.TextView>(R.id.tvCard1Desc)
        val tvCard1Label = view.findViewById<android.widget.TextView>(R.id.tvCard1Label)
        
        val tvCard2Value = view.findViewById<android.widget.TextView>(R.id.tvCard2Value)
        val tvCard2Desc = view.findViewById<android.widget.TextView>(R.id.tvCard2Desc)
        val tvCard2Label = view.findViewById<android.widget.TextView>(R.id.tvCard2Label)

        // Card 1
        val c1 = comparisons[0]
        tvCard1Value.text = c1.date ?: ""
        tvCard1Desc.text = "${c1.day ?: ""} · Score ${c1.score ?: 0}"
        tvCard1Label.text = c1.label ?: ""
        if (!c1.color.isNullOrEmpty()) {
            try {
                tvCard1Label.setTextColor(Color.parseColor(c1.color))
            } catch (e: Exception) {}
        }

        // Card 2
        val c2 = comparisons[1]
        tvCard2Value.text = c2.date ?: ""
        tvCard2Desc.text = "${c2.day ?: ""} · Score ${c2.score ?: 0}"
        tvCard2Label.text = c2.label ?: ""
        if (!c2.color.isNullOrEmpty()) {
            try {
                tvCard2Label.setTextColor(Color.parseColor(c2.color))
            } catch (e: Exception) {}
        }

        binding.llSections.addView(view)
        return true
    }
}
