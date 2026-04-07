package com.humotron.app.ui.bioHack

import android.graphics.Color
import android.os.Bundle
import android.view.View
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
        learningAdapter = LearningProgressAdapter()
        binding.rvProgress.adapter = learningAdapter
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

        binding.header.ivBooks.setImageResource(R.drawable.ic_books_disable)
        binding.header.ivNuggets.setImageResource(R.drawable.ic_nuggets_disable)
        binding.header.ivProgress.setImageResource(R.drawable.ic_biohack_progress_checked)

        binding.header.ivNuggets.setOnClickListener {
            findNavController().navigate(R.id.fragmentNuggets)
        }

        binding.header.ivBooks.setOnClickListener {
            findNavController().navigate(R.id.fragmentBookDetail)
        }


        binding.llBioHack1.setOnClickListener {
//            findNavController().navigate(R.id.action_progress_to_faqs, Bundle().apply {
//                putInt("type", 1)
//            })
            val dialog = BioHackFaqsFragment()
            dialog.arguments = Bundle().apply {
                putInt("type", 1)
            }
            dialog.show(childFragmentManager, "BioHackFaqsFragment")
        }

        binding.llBioHack2.setOnClickListener {
//            findNavController().navigate(R.id.action_progress_to_faqs, Bundle().apply {
//                putInt("type", 2)
//            })

            val dialog = BioHackFaqsFragment()
            dialog.arguments = Bundle().apply {
                putInt("type", 2)
            }
            dialog.show(childFragmentManager, "BioHackFaqsFragment")
        }

        binding.llBioHack3.setOnClickListener {
//            findNavController().navigate(R.id.action_progress_to_faqs, Bundle().apply {
//                putInt("type", 3)
//            })

            val dialog = BioHackFaqsFragment()
            dialog.arguments = Bundle().apply {
                putInt("type", 3)
            }
            dialog.show(childFragmentManager, "BioHackFaqsFragment")
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
    }

    private fun subscribeToObserver() {
        viewModel.bioHackProgressData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = it.data?.data ?: return@observe
                    setProgressData(data)
                }

                Status.ERROR -> {
                    PlutoLog.e("ERROR", it.error?.errorMessage ?: "")
                }

                Status.EXCEPTION -> {
                    PlutoLog.e("Exception", it.error?.errorMessage ?: "")
                }

                Status.LOADING -> {
                    binding.viewAnimator.displayedChild = 0
                }
            }
        }
    }

    private fun setProgressData(data: BioHackProgressResponse.Data) {
        binding.apply {
            binding.viewAnimator.displayedChild = 1
            val mysteryScore = (data.mysteryScore?.levelCompletionScore ?: 0)
            tvProgress.text = "$mysteryScore"
            progress.progress = mysteryScore
            textview2.text = getString(R.string.you_re__closer_, (100.0 - mysteryScore))
            if (!data.testPacks.isNullOrEmpty()) {
                adapter.setData(data.testPacks)
            }

            if (data.mysteryScore != null) {
                binding.tvExplore.text = data.mysteryScore.learningStatus
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
        holeRadius = 55f
        transparentCircleRadius = 60f
        setTransparentCircleAlpha(0)
        setEntryLabelColor(Color.WHITE)
        setEntryLabelTextSize(12f)
        setDrawEntryLabels(true)

        centerText = ""

        legend.isEnabled = false

        setNoDataText("No data")
        setNoDataTextColor(Color.GRAY)

        animateY(900)
        minOffset = 50f

    }

    private fun setPieChartData(items: Map<String, Float>) {
        val entries = items.entries.map { PieEntry(it.value, it.key) }

        val dataSet = PieDataSet(entries, "").apply {
            // nice default palette; you can plug in your own colors here
            colors = listOf(
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light),
                Color.parseColor("#8E44AD"),
                Color.parseColor("#16A085")
            )
            sliceSpace = 2f
            selectionShift = 6f
            // show value lines outside slices
            valueLinePart1Length = 0.5f
            valueLinePart2Length = 0.6f
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLineColor = Color.LTGRAY
        }

        val percentFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value < 1f) "<1%" else "${value.toInt()}%"
            }
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(percentFormatter)
            setValueTextSize(12f)
            setValueTextColor(Color.DKGRAY)
        }

        binding.pieChart.data = data
        binding.pieChart.highlightValues(null)
        binding.pieChart.invalidate()
    }
}