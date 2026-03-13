package com.humotron.app.ui.device

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.tabs.TabLayout
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentMetricBinding
import com.humotron.app.domain.modal.param.RingReadingParam
import com.humotron.app.domain.modal.param.WristBandApiParam
import com.humotron.app.domain.modal.response.AllMetricsResponse
import com.humotron.app.domain.modal.response.MetricType
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.splitBloodPressure
import com.humotron.app.ui.device.adapter.ChatPromptAdapter
import com.humotron.app.ui.device.adapter.InsightAdapter
import com.humotron.app.ui.device.adapter.RecipesAdapter
import com.humotron.app.ui.device.adapter.RecommendationsAdapter
import com.humotron.app.ui.device.adapter.SupplementsAdapter
import com.humotron.app.util.utcOffsetToLocalTime
import com.pluto.plugins.logger.PlutoLog
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class MetricFragment : BaseFragment(R.layout.fragment_metric) {

    private lateinit var binding: FragmentMetricBinding
    private val viewModel: DeviceViewModel by viewModels()
    private val insightAdapter = InsightAdapter()
    private val recommendationsAdapter = RecommendationsAdapter()
    private val chatPromptAdapter = ChatPromptAdapter()
    private val recipesChatPromptAdapter = ChatPromptAdapter()

    private val supplementsChatPromptAdapter = ChatPromptAdapter()

    private val recipesAdapter = RecipesAdapter()
    private val supplementsAdapter = SupplementsAdapter()

    private var deviceName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMetricBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initData()
        observeData()
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.llTempMatters.cardWhatIsTemp.setOnClickListener {
            val isExpanded = binding.llTempMatters.tvMetricWhat.isVisible
            val transition = AutoTransition().apply {
                duration = 200
            }
            TransitionManager.beginDelayedTransition(
                binding.root as ViewGroup,
                transition
            )
            binding.llTempMatters.tvMetricWhat.visibility =
                if (isExpanded) View.GONE else View.VISIBLE

            binding.llTempMatters.ivArrow1.animate()
                .rotation(if (isExpanded) 0f else 180f)
                .setDuration(200)
                .start()
        }

        binding.llTempMatters.cardWhyItMatters.setOnClickListener {
            val isExpanded = binding.llTempMatters.tvMetricWhy.isVisible
            val transition = AutoTransition().apply {
                duration = 200
            }
            TransitionManager.beginDelayedTransition(
                binding.root as ViewGroup,
                transition
            )
            binding.llTempMatters.tvMetricWhy.visibility =
                if (isExpanded) View.GONE else View.VISIBLE
            binding.llTempMatters.ivArrow2.animate()
                .rotation(if (isExpanded) 0f else 180f)
                .setDuration(200)
                .start()
        }

        binding.trackTrends.btnPrevious.setOnClickListener {
            viewModel.previous()
        }

        binding.trackTrends.btnNext.setOnClickListener {
            viewModel.next()
        }

        binding.trackTrends.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabText = tab?.text?.toString()
                val dateTime = arguments?.getString("dateTime")
                if (!dateTime.isNullOrEmpty()) {
                    viewModel.setMode(tabText, dateTime)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        binding.trackTrends.lineChart.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    // Important for accessibility
                    v.performClick()
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        binding.trackTrends.candleChart.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    // Important for accessibility
                    v.performClick()
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    private fun initData() {
        binding.header.title.text = "Device Details"
        binding.trackTrends.tabLayout.getTabAt(1)?.select()

        binding.dsvInsight.adapter = insightAdapter
        binding.dsvInsight.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build()
        )

        binding.dsvRecommendation.adapter = recommendationsAdapter
        binding.dsvRecommendation.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build()
        )

        binding.dsvRecipes.adapter = recipesAdapter
        binding.dsvRecipes.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build()
        )

        binding.dsvSupplements.adapter = supplementsAdapter
        binding.dsvSupplements.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build()
        )

        binding.rvChatPrompts.adapter = chatPromptAdapter
        binding.rvRecipesChatPrompts.adapter = recipesChatPromptAdapter

        val ringId = arguments?.getString("id")
        val dateTime = arguments?.getString("dateTime")
        deviceName = arguments?.getString("deviceName")
        PlutoLog.e("Device type", "$deviceName")
        val metric = arguments?.getParcelable<AllMetricsResponse.Data.Metric>("metric")

        if (metric != null) {
            binding.header.title.text = metric.metricName
            binding.tvTypeLabel.text = metric.metricName
            binding.tvTemperatureValue.text = metric.metricValue?.value.toString()
            metric.id?.let {
                viewModel.getRecommendationsByMetricId(it)
            }
        }

        if (dateTime != null) {
            binding.tvDateTime.text = utcOffsetToLocalTime(dateTime)
        }
    }

    private fun observeData() {
        viewModel.getRecommendationsByMetricIdData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.data?.let { data ->
                        binding.tvUnit.text = data.metricUnit
                        binding.tvSource.text = data.deviceName

                        binding.llTempMatters.tvMetricWhat.text = data.metricWhat
                        binding.llTempMatters.tvMetricWhy.text = data.metricWhy

                        if (!data.insight.isNullOrEmpty()) {
                            binding.cardNoInsights.visibility = View.GONE
                            binding.dsvInsight.visibility = View.VISIBLE
                            insightAdapter.submitList(data.insight)
                        } else {
                            binding.cardNoInsights.visibility = View.VISIBLE
                            binding.dsvInsight.visibility = View.GONE
                        }

                        binding.tvMetricFreq.text =
                            if (data.metricReadingFrequency != null &&
                                !data.metricReadingUnit.isNullOrBlank()
                            ) {
                                "${data.metricReadingFrequency} ${data.metricReadingUnit} average"
                            } else {
                                ""
                            }

                        binding.tvAverageTemp.text = data.metricReading
                        binding.tvUnitAvg.text = data.metricUnit

                        val boxColorHex = data.boxColor
                        val fontColor = data.fontColor

                        if (!boxColorHex.isNullOrBlank()) {
                            try {
                                val color = "#$boxColorHex".toColorInt()
                                binding.tvAverageTemp.setTextColor(color)
                            } catch (e: IllegalArgumentException) {

                            }
                        }

                        if (!boxColorHex.isNullOrBlank()) {
                            try {
                                val color = "#$boxColorHex".toColorInt()
                                binding.tvMetricReadingSubText.setTextColor(color)
                            } catch (e: IllegalArgumentException) {

                            }
                        }

                        fontColor?.let {
                            val safeColor = if (it.startsWith("#")) it else "#$it"
                            runCatching {
                                val parsedColor = safeColor.toColorInt()
                                val drawable = binding.tvMetricReadingSubText.background.mutate()
                                drawable.setTint(parsedColor)
                            }
                        }
                        binding.tvMetricReadingSubText.text = data.metricReadingSubText ?: ""
                        binding.llMetricReadingSubText.visibility =
                            if (data.metricReadingSubText.isNullOrBlank()) View.GONE else View.VISIBLE

                        binding.tvMetricDuration.text = data.metricDuration

                        if (!boxColorHex.isNullOrBlank()) {
                            try {
                                val color = "#$boxColorHex".toColorInt()
                                binding.tvShortDescription.setTextColor(color)
                            } catch (e: IllegalArgumentException) {

                            }
                        }

                        fontColor?.let {
                            val safeColor = if (it.startsWith("#")) it else "#$it"
                            runCatching {
                                val parsedColor = safeColor.toColorInt()
                                val drawable = binding.tvShortDescription.background.mutate()
                                drawable.setTint(parsedColor)
                            }
                        }
                        binding.tvShortDescription.text = data.shortDescription ?: ""
                        binding.tvShortDescription.visibility =
                            if (data.shortDescription.isNullOrBlank()) View.GONE else View.VISIBLE

                        data.recommendations?.items?.takeIf { it.isNotEmpty() }?.let { items ->
                            binding.dsvRecommendation.visibility = View.VISIBLE
                            binding.cardNoRecommendations.visibility = View.GONE
                            recommendationsAdapter.submitList(items)
                        } ?: run {
                            binding.dsvRecommendation.visibility = View.GONE
                            binding.cardNoRecommendations.visibility = View.VISIBLE
                        }

                        data.recommendations?.chatPrompts?.takeIf { it.isNotEmpty() }
                            ?.let { items ->
                                binding.rvChatPrompts.visibility = View.VISIBLE
                                chatPromptAdapter.submitList(items)
                            } ?: run {
                            binding.rvChatPrompts.visibility = View.GONE
                        }

                        data.supplements?.takeIf { it.isNotEmpty() }?.let { items ->
                            binding.dsvSupplements.visibility = View.VISIBLE
                            binding.cardNoSupplements.visibility = View.GONE
                            supplementsAdapter.submitList(items)
                        } ?: run {
                            binding.dsvSupplements.visibility = View.GONE
                            binding.cardNoSupplements.visibility = View.VISIBLE
                        }

                        val supplementsChatPromptList = data.supplements
                            ?.mapNotNull { it.chatPrompt }
                            ?: emptyList()

                        supplementsChatPromptList?.takeIf { it.isNotEmpty() }
                            ?.let { items ->
                                binding.rvSupplementsChatPrompts.visibility = View.VISIBLE
                                supplementsChatPromptAdapter.submitList(items)
                            } ?: run {
                            binding.rvSupplementsChatPrompts.visibility = View.GONE
                        }

                        data.recipes?.items?.takeIf { it.isNotEmpty() }?.let { items ->
                            binding.dsvRecipes.visibility = View.VISIBLE
                            binding.cardNoRecipe.visibility = View.GONE
                            recipesAdapter.submitList(items)
                        } ?: run {
                            binding.dsvRecipes.visibility = View.GONE
                            binding.cardNoRecipe.visibility = View.VISIBLE
                        }

                        val chatPromptList = data.recipes?.items
                            ?.mapNotNull { it.chatPrompt }
                            ?: emptyList()

                        chatPromptList?.takeIf { it.isNotEmpty() }
                            ?.let { items ->
                                binding.rvRecipesChatPrompts.visibility = View.VISIBLE
                                recipesChatPromptAdapter.submitList(items)
                            } ?: run {
                            binding.rvRecipesChatPrompts.visibility = View.GONE
                        }
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }

                Status.EXCEPTION -> {

                }
            }
        }

        viewModel.getRingReadingTemperatureData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    if (it.data?.data.isNullOrEmpty()) {
                        binding.trackTrends.tvNoGraphData.visibility = View.VISIBLE
                        binding.trackTrends.lineChart.visibility = View.GONE
                        binding.trackTrends.candleChart.visibility = View.GONE
                    } else {
                        binding.trackTrends.tvNoGraphData.visibility = View.GONE

                        var selectedTab: String? = "Day"
                        val selectedTabPosition = binding.trackTrends.tabLayout.selectedTabPosition
                        if (selectedTabPosition != TabLayout.Tab.INVALID_POSITION) {
                            val tab = binding.trackTrends.tabLayout.getTabAt(selectedTabPosition)
                            selectedTab = tab?.text?.toString()
                        }

                        //to split value in two entries if receive like 125/70
                        val expandedList = mutableListOf<TemperatureResponse.TemperatureData>()
                        it.data.data.forEach { item ->
                            expandedList.addAll(item.splitBloodPressure())
                        }
                        val sortedList = expandedList.sortedBy { data -> data.time }

                        val maxItem = sortedList
                            .filter { it.value?.toFloatOrNull() != null }
                            .maxByOrNull { it.value!!.toFloat() }

                        val minItem = sortedList
                            .filter { it.value?.toFloatOrNull() != null }
                            .minByOrNull { it.value!!.toFloat() }

                        val rangeText = if (minItem?.value != null && maxItem?.value != null) {
                            val min = "%.2f".format(minItem.value.toFloat())
                            val max = "%.2f".format(maxItem.value.toFloat())
                            "$min-$max"
                        } else {
                            ""
                        }
                        binding.trackTrends.tvRange.text=rangeText

                        when (selectedTab) {
                            "Week" -> {
                                binding.trackTrends.lineChart.visibility = View.GONE
                                binding.trackTrends.candleChart.visibility = View.VISIBLE

                                val entries = createCandleChartEntries(sortedList)
                                setupCandleChart(binding.trackTrends.candleChart, entries)
                            }

                            else -> {
                                binding.trackTrends.candleChart.visibility = View.GONE
                                binding.trackTrends.lineChart.visibility = View.VISIBLE

                                val entries = createLineChartEntries(sortedList, selectedTab)
                                setupLineChart(binding.trackTrends.lineChart, entries, selectedTab)
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                    binding.trackTrends.tvNoGraphData.visibility = View.VISIBLE
                    binding.trackTrends.lineChart.visibility = View.GONE
                    binding.trackTrends.candleChart.visibility = View.GONE
                }

                Status.LOADING -> {
                    showProgress()
                    binding.trackTrends.tvNoGraphData.visibility = View.GONE
                }

                Status.EXCEPTION -> {
                    hideProgress()
                    binding.trackTrends.tvNoGraphData.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.dateText.collect { date ->
                        binding.trackTrends.tvDate.text = date
                    }
                }

                launch {
                    viewModel.dateRange.collect { (start, end) ->
                        if (start != null && end != null) {
                            var selectedText: String? = "Day".uppercase()
                            val selectedTabPosition =
                                binding.trackTrends.tabLayout.selectedTabPosition
                            if (selectedTabPosition != TabLayout.Tab.INVALID_POSITION) {
                                val tab =
                                    binding.trackTrends.tabLayout.getTabAt(selectedTabPosition)
                                selectedText = tab?.text?.toString()?.uppercase()
                            }

                            val metric =
                                arguments?.getParcelable<AllMetricsResponse.Data.Metric>("metric")

                            val fieldLabel = metric?.metricValue?.fieldLabel ?: ""
                            metric?.deviceId?.let { deviceId ->
                                //For WristBand call getWristBandGraphData for chart data, for Ring call getRingReadingGraphData
                                if (deviceName == "WristBand") {
                                    val param = WristBandApiParam(
                                        range = selectedText,
                                        startDate = start,
                                        endDate = end,
                                        offset = "+05:30",
                                        metricName = fieldLabel
                                    )
                                    viewModel.getWristBandGraphData(
                                        deviceId,
                                        param
                                    )
                                } else {

                                    val param = RingReadingParam(
                                        range = selectedText,
                                        startDate = start,
                                        endDate = end,
                                        offset = "+05:30"
                                    )

                                    val endpoint = when (fieldLabel) {
                                        "Temperature" -> "ringReadingTemperature"
                                        "HRV" -> "ringReadingHrv"
                                        "HeartRate" -> "ringReadingHeartRate"
                                        "STEPS" -> "ringReadingSteps"
                                        else -> "ringReadingTemperature"
                                    }
                                    viewModel.getRingReadingGraphData(
                                        endpoint,
                                        deviceId,
                                        param
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.canGoBack.collect {
                        binding.trackTrends.btnPrevious.isEnabled = it
                    }
                }

                launch {
                    viewModel.canGoNext.collect {
                        binding.trackTrends.btnNext.isEnabled = it
                    }
                }
            }
        }
    }

    private fun createLineChartEntries(
        list: List<TemperatureResponse.TemperatureData>,
        tab: String?,
    ): List<Entry> {

        val entries = mutableListOf<Entry>()

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()

        val calendar = Calendar.getInstance()

        val sortedList = list
            .filter { it.time != null && it.value != null }
            .sortedBy { it.time }

        when (tab) {
            "Hour" -> {
                for (item in sortedList) {
                    val date = sdf.parse(item.time!!) ?: continue
                    calendar.time = date
                    val minute = calendar.get(Calendar.MINUTE)
                    entries.add(
                        Entry(
                            minute.toFloat(),
                            item.value?.toFloatOrNull() ?: 0f
                        )
                    )
                }
            }

            "Day" -> {
                for (item in sortedList) {
                    val date = try {
                        sdf.parse(item.time ?: continue)
                    } catch (e: Exception) {
                        continue
                    } ?: continue
                    calendar.time = date
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    entries.add(
                        Entry(
                            hour.toFloat(),
                            item.value?.toFloatOrNull() ?: 0f
                        )
                    )
                }
            }
        }
        return entries.sortedBy { it.x }
    }

    private fun createCandleChartEntries(
        list: List<TemperatureResponse.TemperatureData>,
    ): List<CandleEntry> {
        val entries = mutableListOf<CandleEntry>()

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()

        val calendar = Calendar.getInstance()

        val sortedList = list
            .filter { it.time != null && it.value != null }
            .sortedBy { it.time }

        // Group by day of week
        val groupedByDay = sortedList.groupBy {
            val date = sdf.parse(it.time!!)
            calendar.time = date ?: return@groupBy 0
            getWeekIndex(calendar)
        }

        groupedByDay.forEach { (dayIndex, dayData) ->
            val values = dayData.mapNotNull { it.value?.toFloatOrNull() }
            if (values.isNotEmpty()) {
                val high = values.maxOrNull() ?: 0f
                val low = values.minOrNull() ?: 0f
                val open = values.first()
                val close = values.last()

                entries.add(
                    CandleEntry(
                        dayIndex.toFloat(),
                        high,
                        low,
                        open,
                        close
                    )
                )
            }
        }

        return entries.sortedBy { it.x }
    }

    private fun getWeekIndex(calendar: Calendar): Int {
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return when (day) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    private fun setupLineChart(
        lineChart: LineChart,
        entries: List<Entry>,
        tab: String?,
    ) {
        val dataSet = LineDataSet(entries, "").apply {
            color = ContextCompat.getColor(requireActivity(), R.color.green_1)
            setDrawValues(true)
            valueTextColor = Color.WHITE
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(requireActivity(), R.color.green_1))
            circleRadius = 4f
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireActivity(), R.color.green_1)
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)

        lineChart.apply {
            data = lineData
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                granularity = 1f
                setDrawGridLines(false)
                when (tab) {
                    "Hour" -> {
                        axisMinimum = 0f
                        axisMaximum = 59f
                    }

                    "Day" -> {
                        axisMinimum = 0f
                        axisMaximum = 23f
                    }
                }
            }
            // Y Axis
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            invalidate()
        }
    }

    private fun setupCandleChart(
        candleStickChart: CandleStickChart,
        entries: List<CandleEntry>,
    ) {
        val dataSet = CandleDataSet(entries, "").apply {
            color = Color.rgb(80, 80, 80)
            shadowColor = ContextCompat.getColor(requireActivity(), R.color.white_70)
            shadowWidth = 0.7f
            decreasingColor = ContextCompat.getColor(requireActivity(), R.color.red_1)
            decreasingPaintStyle = Paint.Style.FILL
            increasingColor = ContextCompat.getColor(requireActivity(), R.color.green_1)
            increasingPaintStyle = Paint.Style.FILL
            neutralColor = Color.BLUE
            setDrawValues(true)
            valueTextColor = Color.WHITE
        }

        val candleData = CandleData(dataSet)

        candleStickChart.apply {
            data = candleData
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                granularity = 1f
                setDrawGridLines(false)

                val weekLabels = listOf(
                    "Mon", "Tue", "Wed",
                    "Thu", "Fri", "Sat", "Sun"
                )

                axisMinimum = -0.5f
                axisMaximum = 6.5f
                labelCount = 7
                valueFormatter = IndexAxisValueFormatter(weekLabels)
            }
            // Y Axis
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            invalidate()
        }
    }
}