package com.humotron.app.ui.decode

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
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
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class DecodeWorkdayReportFragment : BaseFragment(R.layout.fragment_workday_report) {

    private lateinit var binding: FragmentWorkdayReportBinding
    private lateinit var dayAdapter: WorkdayDayAdapter
    private val days = DeepDivesMockData.WORKDAY_DAYS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWorkdayReportBinding.bind(view)

        initViews()
        initClicks()
        setupToggle()
        setupDayScroller()

        // Default state — show day view with last day selected
        setDayView()
        updateDayContent(days.size - 1)
    }

    private fun initViews() {
        binding.header.title.text = "Workday Stress"
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
        binding.tvTabMonth.setTextColor(Color.parseColor("#90A0A0"))

        binding.containerDayView.visibility = View.VISIBLE
        binding.containerMonthView.visibility = View.GONE
    }

    private fun setMonthView() {
        binding.tvTabMonth.setBackgroundResource(R.drawable.bg_seg_selected)
        binding.tvTabMonth.setTextColor(Color.WHITE)

        binding.tvTabDay.background = null
        binding.tvTabDay.setTextColor(Color.parseColor("#90A0A0"))

        binding.containerDayView.visibility = View.GONE
        binding.containerMonthView.visibility = View.VISIBLE
        
        populateMonthlyReports()
    }

    private fun populateMonthlyReports() {
        val container = binding.llMonthlyReports
        if (container.childCount > 0) return // Already populated

        val reports = DeepDivesMockData.MONTHLY_REPORTS

        for (report in reports) {
            when (report.state) {
                "ready" -> {
                    val viewReady = layoutInflater.inflate(R.layout.item_monthly_report_ready, container, false)
                    viewReady.findViewById<TextView>(R.id.tvMonth).text = report.month
                    viewReady.findViewById<TextView>(R.id.tvRange).text = report.range
                    viewReady.findViewById<TextView>(R.id.tvScore).apply {
                        text = report.score
                        setTextColor(Color.parseColor("#E7A93C")) // watch
                    }
                    viewReady.findViewById<TextView>(R.id.tvAvg).text = "avg · ${report.zone}"
                    viewReady.findViewById<View>(R.id.tvAction).setOnClickListener {
                        findNavController().navigate(R.id.action_fragmentWorkdayReport_to_fragmentWorkdayDetails)
                    }
                    container.addView(viewReady)
                }
                "generate" -> {
                    val viewGen = layoutInflater.inflate(R.layout.item_monthly_report_gen, container, false)
                    viewGen.findViewById<TextView>(R.id.tvMonth).text = report.month
                    viewGen.findViewById<TextView>(R.id.tvRange).text = report.range
                    viewGen.findViewById<TextView>(R.id.tvNote).text = report.note
                    viewGen.findViewById<View>(R.id.btnGenerate).setOnClickListener {
                        findNavController().navigate(R.id.action_fragmentWorkdayReport_to_fragmentWorkdayDetails)
                    }
                    container.addView(viewGen)
                }
                "collecting" -> {
                    val viewCol = layoutInflater.inflate(R.layout.item_monthly_report_collecting, container, false)
                    viewCol.findViewById<TextView>(R.id.tvMonth).text = report.month
                    viewCol.findViewById<TextView>(R.id.tvRange).text = report.range
                    viewCol.findViewById<TextView>(R.id.tvNote).text = report.note
                    viewCol.findViewById<android.widget.ProgressBar>(R.id.progressBar).progress = report.pct ?: 0
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

        val dist = DeepDivesMockData.zoneDist(day)

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
