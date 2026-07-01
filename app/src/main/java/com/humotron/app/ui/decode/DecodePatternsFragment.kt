package com.humotron.app.ui.decode

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodePatternsBinding
import com.humotron.app.domain.modal.response.TimelineWindow
import com.humotron.app.domain.modal.response.InsightSummaryResponse
import com.humotron.app.domain.modal.response.SummaryWindow
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import com.humotron.app.data.network.Status
import androidx.fragment.app.viewModels
import com.humotron.app.ui.decode.adapter.DecodePatternsAdapter
import com.humotron.app.ui.decode.adapter.PatternItem
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodePatternsFragment : BaseFragment(R.layout.fragment_decode_patterns) {

    private lateinit var binding: FragmentDecodePatternsBinding
    private lateinit var patternsAdapter: DecodePatternsAdapter
    private val viewModel: DecodeViewModel by viewModels()

    private var metricName: String = ""
    private var metricId: String = ""
    private var selectedRange: String = ""
    private var pastWindows: List<TimelineWindow> = emptyList()
    private var apiWindows: List<SummaryWindow> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodePatternsBinding.bind(view)

        metricName = arguments?.getString("metricName") ?: ""
        metricId = arguments?.getString("metricId") ?: ""
        selectedRange = arguments?.getString("selectedRange") ?: ""
        
        val rawArray = arguments?.getParcelableArray("pastWindows")
        pastWindows = rawArray?.map { it as TimelineWindow } ?: emptyList()

        setupInsets()
        initViews()
        initClicks()
        initObservers()
        setupRecyclerView()

        // Fetch actual patterns summary data from API
        viewModel.getInsightSummaryByMetricId(metricId)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentScrollView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
    }

    private fun initViews() {
        binding.header.title.text = metricName
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initObservers() {
        viewModel.insightSummaryData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // Could add a loading indicator or shimmer
                }
                Status.SUCCESS -> {
                    resource.data?.data?.let { summaryData ->
                        apiWindows = summaryData.windows ?: emptyList()
                        // Ensure selectedRange matches a label in apiWindows, else default to first
                        val normalized = normalizeDateString(selectedRange)
                        if (apiWindows.none { normalizeDateString(it.label ?: "") == normalized }) {
                            selectedRange = apiWindows.firstOrNull()?.label ?: ""
                        }
                        setupTabs()
                        updatePatternsList()
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    // Fallback to generating simulated patterns if API fails
                    generateFallbackMockData()
                }
            }
        }
    }

    private fun setupTabs() {
        binding.layoutTabs.removeAllViews()

        val activeSelectedNormalized = normalizeDateString(selectedRange)

        if (apiWindows.isNotEmpty()) {
            apiWindows.forEach { window ->
                val label = window.label ?: ""
                val formattedLabel = formatTimelineRange(window.startDate, window.endDate)
                val tabTextView = TextView(requireContext()).apply {
                    text = formattedLabel
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    typeface = resources.getFont(R.font.manrope_semibold)
                    gravity = Gravity.CENTER
                    setPadding(dpToPx(12), dpToPx(5), dpToPx(12), dpToPx(5))
                    
                    val isSelected = normalizeDateString(label) == activeSelectedNormalized
                    if (isSelected) {
                        setBackgroundResource(R.drawable.bg_decode_tab_active)
                        setTextColor(Color.WHITE)
                    } else {
                        background = null
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_slate_grey))
                    }

                    setOnClickListener {
                        if (normalizeDateString(label) != activeSelectedNormalized) {
                            selectedRange = label
                            setupTabs()
                            updatePatternsList()
                        }
                    }
                }

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(dpToPx(3), dpToPx(2), dpToPx(3), dpToPx(2))
                }
                binding.layoutTabs.addView(tabTextView, params)
            }

            // Update subheader title date range info using the selected window
            val selectedWindow = apiWindows.find { normalizeDateString(it.label ?: "") == activeSelectedNormalized }
                ?: apiWindows.firstOrNull()
            if (selectedWindow != null) {
                val formattedRange = formatTimelineRange(selectedWindow.startDate, selectedWindow.endDate)
                binding.tvSubheader.text = "${metricName.uppercase()} · ${formattedRange.uppercase()}"
            }
        } else {
            // Fallback setup using pastWindows navigation argument
            pastWindows.forEach { window ->
                val label = window.label ?: ""
                val formattedLabel = formatTimelineRange(window.startDate, window.endDate)
                val tabTextView = TextView(requireContext()).apply {
                    text = if (formattedLabel.isNotEmpty()) formattedLabel else label
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    typeface = resources.getFont(R.font.manrope_semibold)
                    gravity = Gravity.CENTER
                    setPadding(dpToPx(12), dpToPx(5), dpToPx(12), dpToPx(5))
                    
                    val isSelected = normalizeDateString(label) == activeSelectedNormalized
                    if (isSelected) {
                        setBackgroundResource(R.drawable.bg_decode_tab_active)
                        setTextColor(Color.WHITE)
                    } else {
                        background = null
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_slate_grey))
                    }

                    setOnClickListener {
                        if (normalizeDateString(label) != activeSelectedNormalized) {
                            selectedRange = label
                            setupTabs()
                            updatePatternsList()
                        }
                    }
                }

                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(dpToPx(3), dpToPx(2), dpToPx(3), dpToPx(2))
                }
                binding.layoutTabs.addView(tabTextView, params)
            }

            val selectedWindow = pastWindows.find { normalizeDateString(it.label ?: "") == activeSelectedNormalized }
                ?: pastWindows.firstOrNull()
            if (selectedWindow != null) {
                val formattedRange = formatTimelineRange(selectedWindow.startDate, selectedWindow.endDate)
                val displayRange = if (formattedRange.isNotEmpty()) formattedRange else selectedWindow.label ?: ""
                binding.tvSubheader.text = "${metricName.uppercase()} · ${displayRange.uppercase()}"
            } else {
                binding.tvSubheader.text = "${metricName.uppercase()} · ${selectedRange.uppercase()}"
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvPatterns.layoutManager = LinearLayoutManager(requireContext())
        patternsAdapter = DecodePatternsAdapter(emptyList()) { pattern ->
            android.util.Log.d("DecodePatternsFragment", "Pattern item clicked: ${pattern.title}")
            val bundle = Bundle().apply {
                putString("metricName", pattern.title)
                putString("insightId", pattern.id)
            }
            findNavController().navigate(
                R.id.action_fragmentDecodePatterns_to_fragmentDecodeDetail,
                bundle
            )
        }
        binding.rvPatterns.adapter = patternsAdapter
        updatePatternsList()
    }

    private fun updatePatternsList() {
        val activeSelectedNormalized = normalizeDateString(selectedRange)
        if (apiWindows.isNotEmpty()) {
            val selectedWindow = apiWindows.find { normalizeDateString(it.label ?: "") == activeSelectedNormalized }
                ?: apiWindows.firstOrNull()

            if (selectedWindow != null) {
                val patterns = selectedWindow.observationLenses?.mapIndexed { index, lens ->
                    val isLocked = lens.action != "VIEW_INSIGHT"
                    val badgeText = if (isLocked) getString(R.string.insights_badge_collecting) else getString(R.string.insights_badge_pattern)
                    val actionText = if (isLocked) {
                        getString(R.string.insights_progress_days_format, selectedWindow.availableDays ?: 0, selectedWindow.minData ?: 3)
                    } else {
                        getString(R.string.insights_view_insight_arrow)
                    }
                    
                    PatternItem(
                        id = lens.insightId ?: "lens_${index}",
                        title = lens.lens ?: "",
                        description = lens.title ?: getString(R.string.insights_insufficient_data),
                        isLocked = isLocked,
                        badgeText = badgeText,
                        actionText = actionText
                    )
                } ?: emptyList()

                val unlockedCount = patterns.count { !it.isLocked }
                val lockedCount = patterns.count { it.isLocked }
                binding.tvPatternStatus.text = getString(R.string.insights_pattern_status_format, unlockedCount, lockedCount)

                patternsAdapter.updateItems(patterns)
                DecodeAnimationUtils.animateCardsIn(binding.rvPatterns)
                return
            }
        }

        // Generate fallback mock patterns
        generateFallbackMockData()
    }

    private fun generateFallbackMockData() {
        val isBloodPressure = metricName.contains("BP", ignoreCase = true) || 
                              metricName.contains("Blood Pressure", ignoreCase = true) ||
                              metricName.contains("Systolic", ignoreCase = true) ||
                              metricName.contains("Diastolic", ignoreCase = true)

        val nameToken = if (isBloodPressure) {
            if (metricName.contains("Diastolic", ignoreCase = true)) {
                getString(R.string.insights_diastolic_bp)
            } else {
                getString(R.string.insights_systolic_bp)
            }
        } else {
            metricName
        }

        val patterns = listOf(
            PatternItem(
                id = "bp_load",
                title = getString(R.string.insights_detail_bp_load_title),
                description = getString(R.string.insights_fallback_work_hours_desc, nameToken),
                isLocked = false,
                badgeText = getString(R.string.insights_badge_pattern),
                actionText = getString(R.string.insights_view_insight_arrow)
            ),
            PatternItem(
                id = "morning_surge",
                title = getString(R.string.insights_fallback_morning_title),
                description = getString(R.string.insights_fallback_morning_desc, nameToken),
                isLocked = false,
                badgeText = getString(R.string.insights_badge_pattern),
                actionText = getString(R.string.insights_view_insight_arrow)
            ),
            PatternItem(
                id = "night_recovery",
                title = getString(R.string.insights_fallback_night_title),
                description = getString(R.string.insights_fallback_night_desc, nameToken),
                isLocked = false,
                badgeText = getString(R.string.insights_badge_pattern),
                actionText = getString(R.string.insights_view_insight_arrow)
            ),
            PatternItem(
                id = "post_exercise",
                title = getString(R.string.insights_fallback_exercise_title),
                description = getString(R.string.insights_fallback_exercise_desc),
                isLocked = true,
                badgeText = getString(R.string.insights_badge_collecting),
                actionText = getString(R.string.insights_fallback_exercise_action, 4, 7)
            )
        )

        val unlockedCount = patterns.count { !it.isLocked }
        val lockedCount = patterns.count { it.isLocked }
        binding.tvPatternStatus.text = getString(R.string.insights_pattern_status_format, unlockedCount, lockedCount)

        patternsAdapter.updateItems(patterns)
        DecodeAnimationUtils.animateCardsIn(binding.rvPatterns)
    }

    private fun normalizeDateString(date: String): String {
        return date.replace("–", "-")
            .replace("—", "-")
            .replace("\\s".toRegex(), "")
            .lowercase()
    }

    private fun formatTimelineRange(startDateStr: String?, endDateStr: String?): String {
        if (startDateStr == null || endDateStr == null) return ""
        return try {
            val start = java.time.ZonedDateTime.parse(startDateStr)
            val end = java.time.ZonedDateTime.parse(endDateStr)

            val dayFormatter = java.time.format.DateTimeFormatter.ofPattern("dd", java.util.Locale.ENGLISH)
            val monthFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.ENGLISH)

            val startDay = dayFormatter.format(start).toInt().toString()
            val endDay = dayFormatter.format(end).toInt().toString()
            val startMonth = monthFormatter.format(start)
            val endMonth = monthFormatter.format(end)

            if (start.month == end.month) {
                "$startDay-$endDay $endMonth"
            } else {
                val startDayFormatted = dayFormatter.format(start)
                val endDayFormatted = dayFormatter.format(end)
                "$startDayFormatted $startMonth-$endDayFormatted $endMonth"
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
