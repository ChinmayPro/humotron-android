package com.humotron.app.ui.decode

import androidx.core.content.ContextCompat
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentWorkdayReportBinding
import com.humotron.app.ui.decode.adapter.WorkdayDayAdapter
import com.humotron.app.ui.decode.data.DeepDivesMockData
import com.humotron.app.ui.decode.data.StressZone
import com.humotron.app.ui.decode.data.WorkdayDay
import androidx.fragment.app.viewModels
import com.humotron.app.data.network.Status
import com.humotron.app.ui.decode.data.HourData
import com.humotron.app.ui.decode.data.ZoneDistribution
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class DecodeWorkdayReportFragment : BaseFragment(R.layout.fragment_workday_report) {

    private lateinit var binding: FragmentWorkdayReportBinding
    private lateinit var dayAdapter: WorkdayDayAdapter
    private val viewModel: DecodeViewModel by viewModels()
    private var days = mutableListOf<WorkdayDay>()
    private var monthlyReports = listOf<com.humotron.app.domain.modal.response.WorkDayOverviewReport>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWorkdayReportBinding.bind(view)

        initViews()
        initClicks()
        setupToggle()
        setupDayScroller()
        setupObservers()

        // Default state
        setDayView()
        
        // Fetch data
        viewModel.getWorkDayStressReportDay("2026-06-22T00:00:00.000Z", "2026-06-28T00:00:00.000Z")
        viewModel.getWorkDayStressReportMonth("2026-06-01T00:00:00.000Z", "2026-06-30T00:00:00.000Z")
        viewModel.getWorkdayStressOverview()
    }
    
    private fun setupObservers() {
        viewModel.workDayStressReportDayData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                resource.data?.data?.let { apiDays ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    days.clear()
                    
                    for (apiDay in apiDays) {
                        if (apiDay.hourlyStress == null || apiDay.hourlyStress.isEmpty()) continue
                        
                        val parsedDate = try {
                            apiDay.date?.let { sdf.parse(it) } ?: java.util.Date()
                        } catch (e: Exception) {
                            java.util.Date()
                        }
                        
                        val cal = Calendar.getInstance().apply { time = parsedDate }
                        val score = apiDay.workdayStressScore ?: 0
                        val zone = try { StressZone.fromScore(score) } catch(e: Exception) { StressZone.THRIVING }
                        
                        val hoursList = mutableListOf<HourData>()
                        apiDay.hourlyStress.forEach { hr ->
                            val hrNum = hr.hour?.split(":")?.firstOrNull()?.toIntOrNull() ?: 0
                            hoursList.add(HourData(hrNum, hr.score ?: 0))
                        }
                        
                        days.add(WorkdayDay(
                            date = parsedDate,
                            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
                            score = score,
                            zone = zone,
                            hours = hoursList,
                            hasData = true
                        ))
                    }
                    
                    dayAdapter.notifyDataSetChanged()
                    if (days.isNotEmpty()) {
                        binding.rvDays.post {
                            binding.rvDays.scrollToPosition(days.size - 1)
                        }
                        updateDayContent(days.size - 1)
                    }
                }
            }
        }
        
        viewModel.workDayStressReportMonthData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                resource.data?.data?.let { apiDays ->
                    binding.calendarHeatmap.setData(apiDays)
                }
            }
        }
        
        viewModel.workdayStressOverviewData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                resource.data?.data?.reports?.let {
                    monthlyReports = it
                    populateMonthlyReports(it)
                }
            }
        }

        // Observe report detail for navigation after View Report / Generate Report
        viewModel.workdayStressReportDetailData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // Could show a loading indicator on the button
                }
                Status.SUCCESS -> {
                    resource.data?.data?.let { reportData ->
                        val reportId = reportData.id ?: ""
                        val bundle = Bundle().apply {
                            putString("reportId", reportId)
                        }
                        findNavController().navigate(
                            R.id.action_fragmentWorkdayReport_to_fragmentWorkdayDetails,
                            bundle
                        )
                    }
                }
                else -> {
                    // Handle error
                    Toast.makeText(requireContext(), R.string.failed_to_load_report, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initViews() {
        binding.header.title.text = getString(R.string.workday_stress)
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupToggle() {
        binding.tvTabDay.setOnClickListener {
            setDayView()
        }
        binding.tvTabMonth.setOnClickListener {
            setMonthView()
        }
    }

    private fun setDayView() {
        binding.tvTabDay.setBackgroundResource(R.drawable.bg_seg_selected)
        binding.tvTabDay.setTextColor(Color.WHITE)

        binding.tvTabMonth.background = null
        binding.tvTabMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text_color))

        binding.containerDayView.visibility = View.VISIBLE
        binding.containerMonthView.visibility = View.GONE
    }

    private fun setMonthView() {
        binding.tvTabMonth.setBackgroundResource(R.drawable.bg_seg_selected)
        binding.tvTabMonth.setTextColor(Color.WHITE)

        binding.tvTabDay.background = null
        binding.tvTabDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text_color))

        binding.containerDayView.visibility = View.GONE
        binding.containerMonthView.visibility = View.VISIBLE
        
        populateMonthlyReports(monthlyReports)
    }

    private fun populateMonthlyReports(reports: List<com.humotron.app.domain.modal.response.WorkDayOverviewReport> = emptyList()) {
        val container = binding.llMonthlyReports
        container.removeAllViews()

        for (report in reports) {
            val dayCount = report.validDayCount ?: 0
            val state = when {
                dayCount < 20 && report.summary?.avgWorkdayStress == null -> "collecting"
                dayCount >= 20 && report.summary?.avgWorkdayStress == null -> "generate"
                else -> "ready"
            }
            
            // Format month
            val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val sdfOutMonth = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            val sdfOutRange = SimpleDateFormat("MMM dd", Locale.ENGLISH)
            
            val monthStr = report.selectedDate?.let { d ->
                try {
                    sdfIn.parse(d)?.let { sdfOutMonth.format(it) }
                } catch(e: Exception) { d }
            } ?: ""
            
            val startStr = report.analysisWindow?.start?.let { d ->
                try { sdfIn.parse(d)?.let { sdfOutRange.format(it) } } catch(e:Exception) { d }
            } ?: ""
            
            val endStr = report.analysisWindow?.end?.let { d ->
                try { sdfIn.parse(d)?.let { sdfOutRange.format(it) } } catch(e:Exception) { d }
            } ?: ""
            
            val rangeStr = getString(R.string.workday_range_format, startStr, endStr, dayCount)
            
            when (state) {
                "ready" -> {
                    val viewReady = layoutInflater.inflate(R.layout.item_monthly_report_ready, container, false)
                    viewReady.findViewById<TextView>(R.id.tvMonth).text = monthStr
                    viewReady.findViewById<TextView>(R.id.tvRange).text = rangeStr
                    
                    val scoreStr = report.summary?.avgWorkdayStress?.toString() ?: ""
                    viewReady.findViewById<TextView>(R.id.tvScore).apply {
                        text = if (scoreStr.endsWith(".0")) scoreStr.substringBefore(".0") else scoreStr
                        try {
                            val colorStr = report.summary?.stressLevelColor
                            if (colorStr != null) {
                                setTextColor(Color.parseColor(colorStr))
                            } else {
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_watch_amber))
                            }
                        } catch (e: Exception) {
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_watch_amber))
                        }
                    }
                    viewReady.findViewById<TextView>(R.id.tvAvg).text = getString(R.string.avg_stress_level_format, report.summary?.stressLevel ?: "")
                    viewReady.findViewById<View>(R.id.tvAction).setOnClickListener {
                        val reportId = report.id ?: ""
                        if (reportId.isNotEmpty()) {
                            viewModel.getWorkdayStressReportById(reportId)
                        }
                    }
                    container.addView(viewReady)
                }
                "generate" -> {
                    val viewGen = layoutInflater.inflate(R.layout.item_monthly_report_gen, container, false)
                    viewGen.findViewById<TextView>(R.id.tvMonth).text = monthStr
                    viewGen.findViewById<TextView>(R.id.tvRange).text = rangeStr
                    viewGen.findViewById<TextView>(R.id.tvNote).text = getString(R.string.ready_to_compose)
                    viewGen.findViewById<View>(R.id.btnGenerate).setOnClickListener {
                        viewModel.generateWorkdayStressReport()
                    }
                    container.addView(viewGen)
                }
                "collecting" -> {
                    val viewCol = layoutInflater.inflate(R.layout.item_monthly_report_collecting, container, false)
                    viewCol.findViewById<TextView>(R.id.tvMonth).text = monthStr
                    
                    // Show just the date range without workday days in the range view for collecting
                    val collRange = if (startStr.isNotBlank() && endStr.isNotBlank()) "$startStr - $endStr" else rangeStr
                    viewCol.findViewById<TextView>(R.id.tvRange).text = collRange
                    
                    viewCol.findViewById<TextView>(R.id.tvNote).text = getString(R.string.workday_days_count, dayCount)
                    
                    val pct = if (dayCount > 20) 100 else (dayCount * 100) / 20
                    viewCol.findViewById<android.widget.ProgressBar>(R.id.progressBar).progress = pct
                    container.addView(viewCol)
                }
            }
        }
    }

    // ─── Day Scroller ────────────────────────────────────────────────

    private fun setupDayScroller() {
        dayAdapter = WorkdayDayAdapter(days) { index ->
            updateDayContent(index)
        }

        binding.rvDays.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = dayAdapter
        }

        // Scroll to the last (selected) day
        binding.rvDays.post {
            binding.rvDays.scrollToPosition(days.size - 1)
        }
    }

    // ─── Update All Day Content ──────────────────────────────────────

    private fun updateDayContent(index: Int) {
        if (index < 0 || index >= days.size) return
        val day = days[index]

        updateDateLabel(day)
        updateScoreCard(day)
        updateZoneDistribution(day)
        updateHourStripChart(day)
    }

    private fun updateDateLabel(day: WorkdayDay) {
        val dateFormat = SimpleDateFormat("EEEE d MMM", Locale.ENGLISH)
        binding.tvDayDate.text = "${dateFormat.format(day.date)} · Workday 9am – 5pm"
    }

    private fun updateScoreCard(day: WorkdayDay) {
        // Score number with zone color
        binding.tvDayScore.text = day.score.toString()
        binding.tvDayScore.setTextColor(day.zone.color)

        // Zone chip badge
        binding.tvDayZone.text = day.zone.label
        binding.tvDayZone.backgroundTintList = ColorStateList.valueOf(day.zone.color)
    }

    // ─── Zone Distribution Bars ──────────────────────────────────────

    private fun updateZoneDistribution(day: WorkdayDay) {
        val container = binding.llZoneDist
        container.removeAllViews()

        val dist = mutableListOf<ZoneDistribution>()
        if (day.score > 0) {
            val t = if (day.zone == StressZone.THRIVING) 100 else 10
            val b = if (day.zone == StressZone.BALANCED) 90 else 0
            val s = if (day.zone == StressZone.STRESSED) 90 else 0
            dist.add(ZoneDistribution(StressZone.THRIVING.label, StressZone.THRIVING.colorHex, t))
            if (b > 0) dist.add(ZoneDistribution(StressZone.BALANCED.label, StressZone.BALANCED.colorHex, b))
            if (s > 0) dist.add(ZoneDistribution(StressZone.STRESSED.label, StressZone.STRESSED.colorHex, s))
        }

        for (zone in dist) {
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_zone_distribution, container, false)

            val tvLabel = itemView.findViewById<TextView>(R.id.tvZoneLabel)
            val viewFill = itemView.findViewById<View>(R.id.viewZoneFill)
            val tvPercent = itemView.findViewById<TextView>(R.id.tvZonePercent)

            tvLabel.text = zone.name
            tvPercent.text = "${zone.percentage}%"

            // Set fill width as percentage of parent
            viewFill.post {
                val parent = viewFill.parent as FrameLayout
                val parentWidth = parent.width
                val fillWidth = (parentWidth * zone.percentage) / 100
                val lp = viewFill.layoutParams
                lp.width = fillWidth
                viewFill.layoutParams = lp

                // Set fill color with rounded corners
                val fillDrawable = GradientDrawable().apply {
                    setColor(Color.parseColor(zone.colorHex))
                    cornerRadius = dpToPx(5f)
                }
                viewFill.background = fillDrawable
            }

            container.addView(itemView)
        }
    }

    // ─── Hour Strip Bar Chart ────────────────────────────────────────

    private fun updateHourStripChart(day: WorkdayDay) {
        val container = binding.llHourStrip
        container.removeAllViews()

        day.hours.forEach { h ->
            val itemView = layoutInflater.inflate(R.layout.item_hour_col, container, false)
            val viewBar = itemView.findViewById<View>(R.id.viewHourBar)
            val tvLabel = itemView.findViewById<TextView>(R.id.tvHourLabel)

            // Label formatting (9a, 12p, 5p)
            val hrStr = when (h.hour) {
                9 -> "9a"
                12 -> "12p"
                17 -> "5p"
                else -> if (h.hour > 12) (h.hour - 12).toString() else h.hour.toString()
            }
            tvLabel.text = hrStr

            val zone = StressZone.fromScore(h.stressValue)

            // Compute bar height proportionally
            // The HTML logic: height:${Math.max(8,Math.round(h.v/85*78))}px;
            // In Android, our container is ~92dp tall (120dp - 28dp padding).
            // Let's use layoutParams to set a proportional height based on a max value.
            viewBar.post {
                val parentHeight = container.height - dpToPx(20f) // subtract some space for label
                val maxStress = 85f
                val minHeightPx = dpToPx(8f)
                val calculatedHeight = (h.stressValue / maxStress) * parentHeight
                
                val lp = viewBar.layoutParams
                lp.height = maxOf(minHeightPx, calculatedHeight).toInt()
                viewBar.layoutParams = lp

                val drawable = GradientDrawable().apply {
                    setColor(Color.parseColor(zone.colorHex))
                    cornerRadii = floatArrayOf(
                        dpToPx(5f), dpToPx(5f), // Top left
                        dpToPx(5f), dpToPx(5f), // Top right
                        dpToPx(3f), dpToPx(3f), // Bottom right
                        dpToPx(3f), dpToPx(3f)  // Bottom left
                    )
                }
                viewBar.background = drawable
            }

            container.addView(itemView)
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
