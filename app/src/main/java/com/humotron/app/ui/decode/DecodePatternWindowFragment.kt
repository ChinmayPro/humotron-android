package com.humotron.app.ui.decode

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDecodePatternWindowBinding
import com.humotron.app.domain.modal.response.InsightTimelineData
import com.humotron.app.domain.modal.response.TimelineWindow
import com.humotron.app.ui.decode.adapter.DecodeTimelineAdapter
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodePatternWindowFragment : BaseFragment(R.layout.fragment_decode_pattern_window) {

    private lateinit var binding: FragmentDecodePatternWindowBinding
    private val viewModel: DecodeViewModel by viewModels()
    private lateinit var timelineAdapter: DecodeTimelineAdapter
    
    private var metricId: String = ""
    private var metricName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodePatternWindowBinding.bind(view)

        metricId = arguments?.getString("metricId") ?: ""
        metricName = arguments?.getString("metricName") ?: ""

        setupInsets()
        initViews()
        initClicks()
        initObservers()
        
        viewModel.getInsightTimeline(metricId)
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

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun initViews() {
        binding.header.title.text = metricName
        DecodeAnimationUtils.addPressEffect(binding.btnGenerateInsight)
        DecodeAnimationUtils.addPressEffect(binding.btnMoreHistory)
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initObservers() {
        viewModel.insightTimelineData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentScrollView.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentScrollView.visibility = View.VISIBLE
                    resource.data?.data?.let { timelineData ->
                        bindTimeline(timelineData)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentScrollView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun bindTimeline(data: InsightTimelineData) {
        val title = data.metricUserFacingName ?: data.metricName ?: metricName
        binding.header.title.text = title

        val currentWindow = data.currentWindow
        val windows = data.insightWindows ?: emptyList()

        // Determine if we should show the top card as ready to generate insight
        val firstPastWindow = windows.firstOrNull()
        val isFirstPastWindowGenerate = firstPastWindow?.windowAction == "GENERATE_INSIGHT"

        val activeWindow: TimelineWindow?
        val pastWindowsList: List<TimelineWindow>

        if (isFirstPastWindowGenerate) {
            activeWindow = firstPastWindow
            pastWindowsList = windows.subList(1, windows.size)
        } else {
            activeWindow = currentWindow
            pastWindowsList = windows
        }

        // Bind top card and title subtitle

        if (activeWindow != null) {
            val daysStr = getString(R.string.insights_days_format, activeWindow.patternDays ?: 7)
            binding.tvPatternWindowSubtitle.text = "${activeWindow.label ?: ""} · $daysStr"
            val action = activeWindow.windowAction ?: "NOT_ENOUGH_DATA"
            if (action == "GENERATE_INSIGHT") {
                binding.tvBadgeIcon.text = "✦"
                binding.tvBadgeIcon.setBackgroundResource(R.drawable.bg_decode_icon_ready)
                binding.tvBadgeIcon.setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_green))
                binding.tvReadyTitle.text = getString(R.string.insights_ready_title)
                binding.tvReadyTitle.setTextColor(Color.WHITE)
                binding.tvReadySubtitle.text = getString(R.string.insights_ready_subtitle)
                binding.btnGenerateInsight.visibility = View.VISIBLE
                binding.layoutProgress.visibility = View.GONE
                
                binding.btnGenerateInsight.setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("metricName", title)
                        putString("metricId", metricId)
                        putString("selectedRange", activeWindow?.label)
                        putParcelableArray("pastWindows", pastWindowsList.toTypedArray())
                    }
                    findNavController().navigate(R.id.action_fragmentDecodePatternWindow_to_fragmentDecodeProcessing, bundle)
                }
            } else {
                binding.tvBadgeIcon.text = "🔒"
                binding.tvBadgeIcon.setBackgroundResource(R.drawable.bg_decode_icon_none)
                binding.tvBadgeIcon.setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_slate_grey))
                
                val hasHistory = windows.any { it.windowAction == "VIEW_INSIGHT" }
                if (hasHistory) {
                    binding.tvReadyTitle.text = getString(R.string.insights_unlock_latest_title)
                    binding.tvReadyTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_green))
                } else {
                    binding.tvReadyTitle.text = getString(R.string.insights_unlock_first_title)
                    binding.tvReadyTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.insights_watch_amber))
                }
                
                binding.tvReadySubtitle.text = getString(R.string.insights_unlock_subtitle)
                binding.btnGenerateInsight.visibility = View.GONE
                
                val avail = activeWindow.availableDays ?: 0
                val min = activeWindow.minData ?: 3
                val boldText = "$avail / $min days"
                val fullText = getString(R.string.insights_progress_required_format, boldText)
                val spannable = SpannableStringBuilder(fullText)
                val startIndex = fullText.indexOf(boldText)
                if (startIndex != -1) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        startIndex + boldText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.WHITE),
                        startIndex,
                        startIndex + boldText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                binding.tvProgressText.text = spannable
                binding.layoutProgress.visibility = View.VISIBLE
            }

            binding.cardTopWindow.visibility = View.VISIBLE
        } else {
            binding.tvPatternWindowSubtitle.text = ""
            binding.cardTopWindow.visibility = View.GONE
        }

        // Setup past items list RecyclerView
        binding.rvPastInsights.layoutManager = LinearLayoutManager(requireContext())
        
        // Handle "More history" button click
        if (pastWindowsList.size > 3) {
            binding.btnMoreHistory.visibility = View.VISIBLE
            timelineAdapter = DecodeTimelineAdapter(pastWindowsList.subList(0, 3)) { item ->
                handlePastItemClick(item, title, pastWindowsList)
            }
            binding.rvPastInsights.adapter = timelineAdapter

            binding.btnMoreHistory.setOnClickListener {
                timelineAdapter.updateItems(pastWindowsList)
                binding.btnMoreHistory.visibility = View.GONE
                DecodeAnimationUtils.animateCardsIn(binding.rvPastInsights)
            }
        } else {
            binding.btnMoreHistory.visibility = View.GONE
            timelineAdapter = DecodeTimelineAdapter(pastWindowsList) { item ->
                handlePastItemClick(item, title, pastWindowsList)
            }
            binding.rvPastInsights.adapter = timelineAdapter
        }

        DecodeAnimationUtils.animateCardsIn(binding.rvPastInsights)
    }

    private fun handlePastItemClick(item: TimelineWindow, title: String, pastWindows: List<TimelineWindow>) {
        val action = item.windowAction ?: return
        when (action) {
            "GENERATE_INSIGHT", "VIEW_INSIGHT" -> {
                val bundle = Bundle().apply {
                    putString("metricName", title)
                    putString("metricId", metricId)
                    putString("selectedRange", item.label)
                    putParcelableArray("pastWindows", pastWindows.toTypedArray())
                }
                findNavController().navigate(R.id.action_fragmentDecodePatternWindow_to_fragmentDecodePatterns, bundle)
            }
        }
    }
}
