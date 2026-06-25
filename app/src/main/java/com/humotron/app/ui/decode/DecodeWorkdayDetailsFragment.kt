package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentWorkdayDetailsBinding
import com.humotron.app.ui.decode.custom.ChartHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeWorkdayDetailsFragment : BaseFragment(R.layout.fragment_workday_details) {

    private lateinit var binding: FragmentWorkdayDetailsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWorkdayDetailsBinding.bind(view)

        initViews()
        initClicks()
        setupCharts()
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

    private fun setupCharts() {
        // Vertical bar chart: Weekly stress trend
        val barColor = ContextCompat.getColor(requireContext(), R.color.deep_dives_series)
        ChartHelper.setupVerticalBarChart(
            binding.chartWeeklyStress,
            listOf(49f, 54f),
            listOf("24 Mar", "1 Apr"),
            barColor
        )

        // Line chart: Start of day ramp
        val color1 = ContextCompat.getColor(requireContext(), R.color.deep_dives_series)
        val color2 = ContextCompat.getColor(requireContext(), R.color.deep_dives_lime)
        val color3 = ContextCompat.getColor(requireContext(), R.color.deep_dives_cool)

        ChartHelper.setupLineChart(
            binding.chartStartOfDay,
            listOf(
                ChartHelper.LineDatasetInfo("24 Mar", listOf(72f, 28f, 18f), color1),
                ChartHelper.LineDatasetInfo("25 Mar", listOf(29f, 29f, 30f), color2),
                ChartHelper.LineDatasetInfo("26 Mar", listOf(14f, 20f, 18f), color3)
            ),
            listOf("09:00", "10:00", "11:00")
        )
    }
}
