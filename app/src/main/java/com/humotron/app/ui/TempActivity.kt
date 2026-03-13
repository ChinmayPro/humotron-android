package com.humotron.app.ui

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.humotron.app.R
import com.humotron.app.databinding.ActivityTempBinding
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.Zone
import com.humotron.app.ui.metrics.adapter.ZoneAdapter
import com.pluto.plugins.logger.PlutoLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TempActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTempBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTempBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //initChart()

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(40f, "A"))
        entries.add(PieEntry(30f, "B"))
        entries.add(PieEntry(20f, "C"))
        entries.add(PieEntry(10f, "D"))

        val dataSet = PieDataSet(entries, "Sample Data")

        dataSet.colors = listOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW
        )

        val data = PieData(dataSet)
        data.setValueTextSize(12f)

        binding.pieChart.data = data
        binding.pieChart.description.isEnabled = false
        binding.pieChart.invalidate()
    }

    fun initChart() {
        val temperatureList = listOf(
            TemperatureResponse.TemperatureData("28.5", "Temperature", "2026-02-18T22:35:11.000Z"),
            TemperatureResponse.TemperatureData("29.1", "Temperature", "2026-02-18T22:30:11.000Z"),
            TemperatureResponse.TemperatureData("22.7", "Temperature", "2026-02-18T22:25:12.000Z"),
            TemperatureResponse.TemperatureData("20.0", "Temperature", "2026-02-18T22:20:12.000Z"),
            TemperatureResponse.TemperatureData("20.0", "Temperature", "2026-02-18T21:15:10.000Z"),
            TemperatureResponse.TemperatureData("24.4", "Temperature", "2026-02-18T21:05:10.000Z"),
            TemperatureResponse.TemperatureData("30.7", "Temperature", "2026-02-18T20:55:10.000Z"),
            TemperatureResponse.TemperatureData("25.5", "Temperature", "2026-02-17T19:40:09.000Z"),
            TemperatureResponse.TemperatureData("24.8", "Temperature", "2026-02-16T19:35:10.000Z"),
            TemperatureResponse.TemperatureData("23.7", "Temperature", "2026-02-15T19:30:10.000Z")
        )

        val sortedList = temperatureList.sortedBy { data -> data.time }
        var selectedTab: String? = "Day"
        when (selectedTab) {
            "Week" -> {
                binding.lineChart.visibility = View.GONE
                binding.candleChart.visibility = View.VISIBLE

                val entries = createCandleChartEntries(sortedList)
                setupCandleChart(binding.candleChart, entries)
            }

            else -> {
                binding.candleChart.visibility = View.GONE
                binding.lineChart.visibility = View.VISIBLE

                val entries = createLineChartEntries(sortedList, selectedTab)
                setupLineChart(binding.lineChart, entries, selectedTab)
            }
        }
    }

    private fun setupCandleChart(
        candleStickChart: CandleStickChart,
        entries: List<CandleEntry>,
    ) {
        val dataSet = CandleDataSet(entries, "").apply {
            color = Color.rgb(80, 80, 80)
            shadowColor = ContextCompat.getColor(this@TempActivity, R.color.white_70)
            shadowWidth = 0.7f
            decreasingColor = ContextCompat.getColor(this@TempActivity, R.color.red_1)
            decreasingPaintStyle = Paint.Style.FILL
            increasingColor = ContextCompat.getColor(this@TempActivity, R.color.green_1)
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

    private fun setupLineChart(
        lineChart: LineChart,
        entries: List<Entry>,
        tab: String?,
    ) {
        val dataSet = LineDataSet(entries, "").apply {
            color = ContextCompat.getColor(this@TempActivity, R.color.green_1)
            setDrawValues(true)
            valueTextColor = Color.WHITE
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(this@TempActivity, R.color.green_1))
            circleRadius = 4f
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@TempActivity, R.color.green_1)
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
}