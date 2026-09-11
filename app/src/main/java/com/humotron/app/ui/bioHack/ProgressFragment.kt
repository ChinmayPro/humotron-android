package com.humotron.app.ui.bioHack

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentProgressBinding
import com.humotron.app.domain.modal.response.BioHackProgressResponse
import com.humotron.app.ui.bioHack.adapter.LearningProgressAdapter
import com.humotron.app.ui.bioHack.adapter.StreakAdapter
import com.humotron.app.ui.bioHack.adapter.TestAdapter
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ProgressFragment : BaseFragment(R.layout.fragment_progress) {

    private lateinit var binding: FragmentProgressBinding
    private val viewModel: NuggetsViewModel by viewModels()
    private lateinit var adapter: TestAdapter
    private lateinit var learningAdapter: LearningProgressAdapter
    private lateinit var streakAdapter: StreakAdapter
    private var animationRunnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProgressBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 50)
            insets
        }
        adapter = TestAdapter()
        binding.rvTests.adapter = adapter
        learningAdapter = LearningProgressAdapter { hasMore ->
            binding.cvLoadMore.visibility = if (hasMore) android.view.View.VISIBLE else android.view.View.GONE
            binding.tvLoadMore.text = "Load more"
        }
        binding.rvProgress.adapter = learningAdapter
        binding.cvLoadMore.setOnClickListener {
            if (learningAdapter.isExpanded) {
                learningAdapter.collapse()
                binding.tvLoadMore.text = "Load more"
            } else {
                learningAdapter.expandAll()
                binding.tvLoadMore.text = "Show less"
            }
        }
        streakAdapter = StreakAdapter()
        binding.rvStreak.adapter = streakAdapter
        viewModel.bioHackProgressData().value?.data.let { data ->
            if (data?.data == null) {
                viewModel.getBioHackProgress()
            } else {
                setProgressData(data.data)
            }
        }

        subscribeToObserver()

        binding.header.tvProgress.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
        binding.header.indicatorProgress.visibility = android.view.View.VISIBLE
        binding.header.tvNuggets.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
        binding.header.indicatorNuggets.visibility = android.view.View.INVISIBLE
        binding.header.tvBooks.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
        binding.header.indicatorBooks.visibility = android.view.View.INVISIBLE

        binding.header.tabNuggets.setOnClickListener {
            findNavController().navigate(R.id.fragmentNuggets)
        }

        binding.header.tabBooks.setOnClickListener {
            findNavController().navigate(R.id.fragmentBookDetail)
        }


        fun toggleFaq(
            targetAnswer: View,
            targetArrow: View,
            otherAnswers: List<View>,
            otherArrows: List<View>
        ) {
            val isCurrentlyVisible = targetAnswer.visibility == View.VISIBLE

            otherAnswers.forEach { it.visibility = View.GONE }
            otherArrows.forEach { it.rotation = 0f }

            if (isCurrentlyVisible) {
                targetAnswer.visibility = View.GONE
                targetArrow.rotation = 0f
            } else {
                targetAnswer.visibility = View.VISIBLE
                targetArrow.rotation = 180f
            }
        }

        binding.llBioHack1.setOnClickListener {
            toggleFaq(
                binding.llAnswer1,
                binding.ivArrow1,
                listOf(binding.llAnswer2, binding.llAnswer3),
                listOf(binding.ivArrow2, binding.ivArrow3)
            )
        }

        binding.llBioHack2.setOnClickListener {
            toggleFaq(
                binding.llAnswer2,
                binding.ivArrow2,
                listOf(binding.llAnswer1, binding.llAnswer3),
                listOf(binding.ivArrow1, binding.ivArrow3)
            )
        }

        binding.llBioHack3.setOnClickListener {
            toggleFaq(
                binding.llAnswer3,
                binding.ivArrow3,
                listOf(binding.llAnswer1, binding.llAnswer2),
                listOf(binding.ivArrow1, binding.ivArrow2)
            )
        }

        binding.tvExplore.setOnClickListener {
            val remaining = binding.tvExplore.tag as? BioHackProgressResponse.Data.MysteryScore
            val bundle = Bundle()
            if (remaining != null) {
                bundle.putInt("score", remaining.remainingLikes ?: 0)
                bundle.putString("level", remaining.learningStatus)
            }
            findNavController().navigate(R.id.fragmentExplorer, bundle)
        }

        binding.cvSeeLevels.setOnClickListener {
            binding.tvExplore.performClick()
        }
    }

    private fun subscribeToObserver() {
        viewModel.bioHackProgressData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.flLoaderOverlay.visibility = View.GONE
                    val data = it.data?.data ?: return@observe
                    setProgressData(data)
                }

                Status.ERROR -> {
                    binding.flLoaderOverlay.visibility = View.GONE
                    PlutoLog.e("ERROR", it.error?.errorMessage ?: "")
                    showApiError(it.error)
                }

                Status.EXCEPTION -> {
                    binding.flLoaderOverlay.visibility = View.GONE
                    PlutoLog.e("Exception", it.error?.errorMessage ?: "")
                    showApiError(it.error)
                }

                Status.LOADING -> {
                    binding.viewAnimator.displayedChild = 1
                    binding.flLoaderOverlay.visibility = View.VISIBLE
                    binding.progress.progress = 0
                    binding.tvGaugeScore.text = "0"
                    
                    val lp = binding.vThumb.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    if (lp != null) {
                        lp.horizontalBias = 0.0f
                        binding.vThumb.layoutParams = lp
                    }
                }
            }
        }
    }

    private fun animateGaugeScore(targetScore: Int) {
        animationRunnable?.let { binding.progress.removeCallbacks(it) }
        binding.progress.progress = 0
        binding.tvGaugeScore.text = "0"

        val sliderPercent = (targetScore / 100f).coerceIn(0.0f, 1f)
        
        val startBias = 0.0f
        val lp = binding.vThumb.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        if (lp != null) {
            lp.horizontalBias = startBias
            binding.vThumb.layoutParams = lp
        }

        val durationMs = 1500L
        val interpolator = DecelerateInterpolator(1.5f)
        
        animationRunnable = object : Runnable {
            var startTime = -1L
            override fun run() {
                if (startTime == -1L) {
                    startTime = System.currentTimeMillis()
                }
                val elapsed = System.currentTimeMillis() - startTime
                val rawFraction = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                val fraction = interpolator.getInterpolation(rawFraction)
                
                val currentScore = (targetScore * fraction).toInt()
                binding.progress.progress = currentScore
                binding.tvGaugeScore.text = "$currentScore"
                
                val currentBias = startBias + (sliderPercent - startBias) * fraction
                val currentLp = binding.vThumb.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                if (currentLp != null) {
                    currentLp.horizontalBias = currentBias
                    binding.vThumb.layoutParams = currentLp
                }
                
                if (rawFraction < 1f) {
                    binding.progress.postDelayed(this, 16)
                }
            }
        }
        // 300ms delay to let screen transition finish before animating
        binding.progress.postDelayed(animationRunnable, 300)
    }

    override fun onDestroyView() {
        animationRunnable?.let { binding.progress.removeCallbacks(it) }
        animationRunnable = null
        super.onDestroyView()
    }

    private fun setProgressData(data: BioHackProgressResponse.Data) {
        binding.apply {
            binding.flLoaderOverlay.visibility = View.GONE
            binding.viewAnimator.displayedChild = 1
            val mysteryScore = (data.mysteryScore?.levelCompletionScore ?: 0)
            header.tvProgress.text = "Progress"
            animateGaugeScore(mysteryScore)
            val remainingNuggets = data.mysteryScore?.remainingLikes ?: 537
            textview2.text = "You're building your foundation. $remainingNuggets Nuggets to your next level."
            if (!data.testPacks.isNullOrEmpty()) {
                adapter.setData(data.testPacks)
            }

            if (data.mysteryScore != null) {
                val status = data.mysteryScore.learningStatus ?: "Explorer"
                binding.tvExplore.text = status.replace("✦", "").trim()
                binding.tvExplore.tag = data.mysteryScore
            }

            if (!data.primaryTagScore.isNullOrEmpty()) {
                learningAdapter.setData(data.primaryTagScore)
            }

            if (!data.categoryScore.isNullOrEmpty()) {
                setupPieChart()
                val map = hashMapOf<String, Float>()
                for (item in data.categoryScore) {
                    map[item.categoryName ?: ""] = (item.percentage ?: 0).toFloat()
                }
                setPieChartData(map.toMap())
            }

            if (!data.streak.isNullOrEmpty()) {
                val sf = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                val sfDate = SimpleDateFormat("dd MMM", Locale.UK)
                val sfDay = SimpleDateFormat("EEE", Locale.UK)
                try {
                    data.streak.filter { it.date != null }.forEach {

                        val date = sf.parse(it.date!!)
                        it.formatDate = sfDate.format(date!!)
                        it.day = sfDay.format(date)


                    }
                    streakAdapter.setData(data.streak)
                    (binding.rvStreak.layoutManager as? LinearLayoutManager)?.scrollToPosition(
                        data.streak.size - 1
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


        }
    }


    private fun setupPieChart() = with(binding.pieChart) {
        setUsePercentValues(false)
        description.isEnabled = false
        isDrawHoleEnabled = true
        setHoleColor(Color.TRANSPARENT)
        holeRadius = 65f
        transparentCircleRadius = 67f
        setTransparentCircleAlpha(0)
        setDrawEntryLabels(false)
        centerText = ""
        legend.isEnabled = false
        setNoDataText("No data")
        setNoDataTextColor(Color.GRAY)
        animateY(900)
        minOffset = 0f
    }

    private fun setPieChartData(items: Map<String, Float>) {
        val palette = listOf(
            Color.parseColor("#4DC5D6"),
            Color.parseColor("#BEF264"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#5FE6A6"),
            Color.parseColor("#F87171"),
            Color.parseColor("#38BDF8"),
            Color.parseColor("#A3E635")
        )

        val entries = items.entries.map { PieEntry(it.value, it.key) }

        val dataSet = PieDataSet(entries, "").apply {
            colors = palette
            sliceSpace = 3f
            selectionShift = 4f
            setDrawValues(false)
        }

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.highlightValues(null)
        binding.pieChart.invalidate()

        binding.llPieLegend.removeAllViews()
        var colorIndex = 0
        for ((category, percentage) in items) {
            val color = palette[colorIndex % palette.size]
            colorIndex++

            val rowLayout = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 12, 0, 12)
            }

            val dotView = View(requireContext()).apply {
                val size = (10 * resources.displayMetrics.density).toInt()
                val radius = 3 * resources.displayMetrics.density
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (12 * resources.displayMetrics.density).toInt()
                }
                val drawable = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = radius
                    setColor(color)
                }
                background = drawable
            }
            rowLayout.addView(dotView)

            val tvName = androidx.appcompat.widget.AppCompatTextView(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                text = category
                setTextColor(Color.parseColor("#CBD5E1"))
                textSize = 14f
            }
            rowLayout.addView(tvName)

            val tvValue = androidx.appcompat.widget.AppCompatTextView(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = String.format(Locale.UK, "%.1f%%", percentage)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                textSize = 14f
            }
            rowLayout.addView(tvValue)

            binding.llPieLegend.addView(rowLayout)
        }
    }
}