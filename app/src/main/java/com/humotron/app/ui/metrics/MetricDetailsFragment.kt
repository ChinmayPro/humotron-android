package com.humotron.app.ui.metrics

import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.humotron.app.databinding.FragmentMetricDetailsBinding
import com.humotron.app.domain.modal.param.RingReadingParam
import com.humotron.app.domain.modal.param.WristBandApiParam
import com.humotron.app.domain.modal.response.DailyMetricItem
import com.humotron.app.domain.modal.response.MetricType
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.WristBandSleepDurationItem
import com.humotron.app.domain.modal.response.splitBloodPressure
import com.humotron.app.ui.device.adapter.SleepMetricsAdapter
import com.humotron.app.ui.metrics.adapter.ZoneAdapter
import com.humotron.app.util.convertDecimalHours
import com.humotron.app.util.formatLocalDateToIso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.find

@AndroidEntryPoint
class MetricDetailsFragment : BaseFragment(R.layout.fragment_metric_details) {
    private lateinit var binding: FragmentMetricDetailsBinding
    private val viewModel: MetricDetailsViewModel by viewModels()
    private var metricId: String? = null
    private var deviceId: String? = null
    private var deviceName: String? = null
    private var metricName: String? = null
    private val zoneAdapter by lazy { ZoneAdapter() }
    private val sleepMetricsAdapter by lazy { SleepMetricsAdapter { _, _ -> } }
    private val respiratoryMetricsAdapter by lazy { SleepMetricsAdapter { _, _ -> } }
    private val cardioVascularMetricsAdapter by lazy { SleepMetricsAdapter { _, _ -> } }
    private var isInitialLoad = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMetricDetailsBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainMetrics) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initData()
        initChart()
        observeData()
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.llCalender.weekCalendarView.setOnDateSelected {
            val metricType = viewModel.getMetric()
            if (deviceName == "WristBand" && metricType is MetricType.Sleep) {
                val data = viewModel.wristBandSleepDuration.value?.data?.data
                updateWristBandSleepDurationMetricDetails(data, it)
            } else {
                val data = viewModel.dailyCalculatedMetrics.value?.data?.data
                updateMetricDetails(data, it)
            }
        }
        binding.llCalender.weekCalendarView.setOnWeekChanged { _, start, end ->
            metricId?.let { metricId ->
                deviceId?.let { deviceId ->
                    if (deviceName == "WristBand" && binding.header.title.text == "Sleep Duration") {
                        viewModel.getWristBandSleepDurationData(
                            deviceId,
                            formatLocalDateToIso(start),
                            formatLocalDateToIso(end)
                        )
                    } else {
                        viewModel.getDailyCalculatedMetrics(
                            deviceId,
                            metricId,
                            formatLocalDateToIso(start),
                            formatLocalDateToIso(end)
                        )
                    }
                }
            }
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
                val dateTime = when (val metricType = viewModel.getMetric()) {
                    is MetricType.PhysicalRecovery -> metricType.dataSync
                    is MetricType.Stress -> metricType.dataSync
                    else -> null
                }
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
        binding.rvZones.apply {
            adapter = zoneAdapter
        }
        val metricType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("metric", MetricType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("metric")
        }

        metricType?.let {
            viewModel.setMetric(it)
            val dataSync = when (metricType) {
                is MetricType.Exercise -> metricType.dataSync
                is MetricType.Sleep -> metricType.dataSync
                is MetricType.PhysicalRecovery -> metricType.dataSync
                is MetricType.Stress -> metricType.dataSync
            }
            dataSync?.let {
                binding.llCalender.weekCalendarView.setDataSyncDate(dataSync)
            }
        }

        viewModel.getMetric()?.let { type ->
            when (type) {
                is MetricType.Exercise -> {
                    binding.header.title.text = "Exercise Intensity"
                    binding.llCalender.tvScore.text = type.metric.score.toString()
                    binding.llCalender.tvUnitLabel.text = type.metric.unit
                    binding.llCalender.tvSubtitle.text = type.metric.metricUserFacingName
                    binding.llTempMatters.tvTitle1.text =
                        "What is ${type.metric.metricUserFacingName}?"

                    metricId = type.metric.metricId
                    deviceId = type.deviceId
                    deviceName = type.deviceName
                    metricName = type.metric.metricName
                }

                is MetricType.Sleep -> {
                    binding.header.title.text = "Sleep Patterns"
                    binding.llCalender.llUnit.visibility = View.GONE
                    binding.llCalender.llTime.visibility = View.VISIBLE

                    val (hours, minutes) = convertDecimalHours(type.metric.value?.totalSleepHours)
                    binding.llCalender.tvHour.text = "$hours"
                    binding.llCalender.tvMinute.text = "$minutes"

                    binding.llCalender.tvUnitLabel.visibility = View.GONE
                    binding.llCalender.tvSubtitle.text = type.metric.metricUserFacingName
                    binding.llTempMatters.tvTitle1.text =
                        "What is ${type.metric.metricUserFacingName}?"

                    metricId = type.metric.metricId
                    deviceId = type.deviceId
                    deviceName = type.deviceName
                    metricName = type.metric.metricName
                }

                is MetricType.PhysicalRecovery -> {
                    binding.header.title.text = type.metric.metricUserFacingName
                    binding.llCalender.tvScore.text =
                        String.format(Locale.getDefault(), "%.2f", type.metric.value)
                    binding.llCalender.tvUnitLabel.text = type.metric.unit
                    binding.llCalender.tvSubtitle.text = type.metric.metricUserFacingName
                    binding.llTempMatters.tvTitle1.text =
                        "What is ${type.metric.metricUserFacingName}?"

                    metricId = type.metric.metricId
                    deviceId = type.deviceId
                    deviceName = type.deviceName
                    metricName = type.metric.metricName

                    //show track your trends for PhysicalRecovery
                    binding.trackTrends.root.visibility = View.VISIBLE
                    binding.trackTrends.tabLayout.getTabAt(1)?.select()
                }

                is MetricType.Stress -> {
                    binding.header.title.text = "Stress Score"
                    binding.llCalender.tvScore.text = type.metric.value.toString()
                    binding.llCalender.tvUnitLabel.text = type.metric.unit
                    binding.llCalender.tvSubtitle.text = type.metric.metricUserFacingName
                    binding.llTempMatters.tvTitle1.text =
                        "What is ${type.metric.metricUserFacingName}?"

                    metricId = type.metric.metricId
                    deviceId = type.deviceId
                    deviceName = type.deviceName
                    metricName = type.metric.metricName

                    //show track your trends for Stress Score
                    binding.trackTrends.root.visibility = View.VISIBLE
                    binding.trackTrends.tabLayout.getTabAt(1)?.select()
                }
            }

            metricId?.let { metricId ->
                deviceId?.let { deviceId ->
                    val (start, end) = binding.llCalender.weekCalendarView.getWeekRange()
                    if (deviceName == "WristBand" && type is MetricType.Sleep) {
                        viewModel.getWristBandSleepDurationData(
                            deviceId,
                            formatLocalDateToIso(start),
                            formatLocalDateToIso(end)
                        )
                    } else {
                        viewModel.getDailyCalculatedMetrics(
                            deviceId,
                            metricId,
                            formatLocalDateToIso(start),
                            formatLocalDateToIso(end)
                        )
                    }
                }
            }
        }
    }

    private fun initChart() {
        binding.pieChart.apply {
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 70f
            isHighlightPerTapEnabled = false
            legend.isEnabled = false
            setExtraOffsets(24f, 24f, 24f, 24f)
            setNoDataText("No zone activity for this period.")
            setNoDataTextColor(requireContext().getColor(R.color.white_70))
            description.isEnabled = false
            setDrawEntryLabels(false)
        }
    }

    private fun observeData() {
        viewModel.dailyCalculatedMetrics.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    if (isInitialLoad) {
                        val data = resource.data?.data
                        val selectedDate = binding.llCalender.weekCalendarView.getSelectedDate()
                        updateMetricDetails(data, selectedDate)
                        isInitialLoad = false
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }
            }
        }

        viewModel.wristBandSleepDuration.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    if (isInitialLoad) {
                        val data = resource.data?.data
                        val selectedDate = binding.llCalender.weekCalendarView.getSelectedDate()
                        updateWristBandSleepDurationMetricDetails(data, selectedDate)
                        isInitialLoad = false
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
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
                            val param = WristBandApiParam(
                                range = selectedText,
                                startDate = start,
                                endDate = end,
                                offset = "+05:30",
                                metricName = metricName ?: ""
                            )
                            deviceId?.let { deviceId ->
                                viewModel.getWristBandGraphData(
                                    deviceId,
                                    param
                                )
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

    private fun updateMetricDetails(data: List<DailyMetricItem>?, date: LocalDate) {
        val currentDate = formatLocalDateToIso(date)
        val dailyMetricItem =
            data?.find { it.key.take(10) == currentDate.take(10) }

        if (dailyMetricItem != null && dailyMetricItem.value.isNotEmpty()) {
            val metricDetail = dailyMetricItem.value[0]
            binding.llTempMatters.tvMetricWhat.text = metricDetail.metricWhat
            binding.llTempMatters.tvMetricWhy.text = metricDetail.metricWhy

            if (!metricDetail.zones.isNullOrEmpty()) {
                val filteredZones = metricDetail.zones.filter { it.zone != 0 }
                zoneAdapter.submitList(filteredZones)
                binding.llTimeSpent.visibility = View.VISIBLE

                val chartHelper = RingExerciseIntensityZonesChartView(binding.pieChart)
                chartHelper.configure(filteredZones)
            } else {
                binding.llTimeSpent.visibility = View.GONE
            }


        } else {
            binding.llTempMatters.tvMetricWhat.text = ""
            binding.llTempMatters.tvMetricWhy.text = ""
            zoneAdapter.submitList(emptyList())
            binding.llTimeSpent.visibility = View.GONE
            binding.pieChart.clear()
        }
    }

    private fun updateWristBandSleepDurationMetricDetails(
        data: List<WristBandSleepDurationItem>?,
        date: LocalDate,
    ) {
        val currentDate = formatLocalDateToIso(date)
        val dailyMetricItem =
            data?.find { it.key.take(10) == currentDate.take(10) }

        if (dailyMetricItem != null && dailyMetricItem.value.isNotEmpty()) {
            val metricDetail = dailyMetricItem.value[0]
            binding.llTempMatters.tvMetricWhat.text = metricDetail.metricWhat
            binding.llTempMatters.tvMetricWhy.text = metricDetail.metricWhy
            binding.llTimeSpent.visibility = View.GONE

            val sleepQualityLabels = setOf(
                "Avg. effieiency",
                "deepSleepInfo",
                "temperature",
                "sleepEfficiency",
                "mainSleepDuration",
                "sleepSessions",
                "sleepConsistency",
                "sleepScore"
            )

            val sleepQualityMetrics = dailyMetricItem.value.filter {
                sleepQualityLabels.any { label ->
                    it.metricValue.fieldLabel.equals(label, true)
                }
            }

            if (sleepQualityMetrics.isNotEmpty()) {
                binding.llSleepQuality.visibility = View.VISIBLE
                binding.rvSleepQuality.adapter = sleepMetricsAdapter
                sleepMetricsAdapter.submitList(sleepQualityMetrics)
            } else {
                binding.llSleepQuality.visibility = View.GONE
            }

            // Respiratory Metrics
            val respiratoryLabels = setOf(
                "spo2",
                "Avg. br"
            )
            val respiratoryMetrics = dailyMetricItem.value.filter {
                respiratoryLabels.any { label ->
                    it.metricValue.fieldLabel.equals(label, true)
                }
            }

            if (respiratoryMetrics.isNotEmpty()) {
                binding.llRespiratory.visibility = View.VISIBLE
                binding.rvRespiratory.adapter = respiratoryMetricsAdapter
                respiratoryMetricsAdapter.submitList(respiratoryMetrics)
            } else {
                binding.llRespiratory.visibility = View.GONE
            }

            // Cardiovascular Metrics
            val cardioVascularLabels = setOf(
                "Avg. hr",
                "Avg. hrv",
                "Avg. hrDip"
            )
            val cardioVascularMetrics = dailyMetricItem.value.filter {
                cardioVascularLabels.any { label ->
                    it.metricValue.fieldLabel.equals(label, true)
                }
            }

            if (cardioVascularMetrics.isNotEmpty()) {
                binding.llCardiovascular.visibility = View.VISIBLE
                binding.rvCardiovascular.adapter = cardioVascularMetricsAdapter
                cardioVascularMetricsAdapter.submitList(cardioVascularMetrics)
            } else {
                binding.llCardiovascular.visibility = View.GONE
            }

        } else {
            binding.llTempMatters.tvMetricWhat.text = ""
            binding.llTempMatters.tvMetricWhy.text = ""
            zoneAdapter.submitList(emptyList())
            binding.llTimeSpent.visibility = View.GONE
            binding.pieChart.clear()
            binding.llSleepQuality.visibility = View.GONE
            binding.llRespiratory.visibility = View.GONE
            binding.llCardiovascular.visibility = View.GONE
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
                            item.value!!.toFloat()
                        )
                    )
                }
            }

            "Day" -> {
                for (item in sortedList) {
                    val date = sdf.parse(item.time!!) ?: continue
                    calendar.time = date
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    entries.add(
                        Entry(
                            hour.toFloat(),
                            item.value!!.toFloat()
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
            val values = dayData.mapNotNull { it.value?.toFloat() }
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
