package com.humotron.app.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.humotron.app.R
import com.humotron.app.databinding.ActivityTempBinding
import com.humotron.app.domain.modal.response.TemperatureResponse
import com.humotron.app.domain.modal.response.splitBloodPressure
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


        val dummyTemperatureResponse = TemperatureResponse(
            status = "success",
            message = "Data found Successfully",
            data = listOf(
                TemperatureResponse.TemperatureData(
                    time = "2026-05-04T21:00:00.000+0000",
                    value = "128",
                    type = "systolicBP"
                ),
                TemperatureResponse.TemperatureData(
                    time = "2026-05-04T22:00:00.000+0000",
                    value = "128",
                    type = "systolicBP"
                ),
                TemperatureResponse.TemperatureData(
                    time = "2026-05-05T00:00:00.000+0000",
                    value = "129",
                    type = "systolicBP"
                ),
                TemperatureResponse.TemperatureData(
                    time = "2026-05-05T01:00:00.000+0000",
                    value = "129",
                    type = "systolicBP"
                )
            ),
            averageReading = 128.5,
            calculationPeriod = "7",
            typicalRange = listOf(100, 119)
        )

        //to split value in two entries if receive like 125/70
        val expandedList = mutableListOf<TemperatureResponse.TemperatureData>()
        dummyTemperatureResponse.data?.forEach { item ->
            expandedList.addAll(item.splitBloodPressure())
        }
        val sortedList = expandedList.sortedBy { data -> data.time }

        val selectedTab = "Day"
        val entries = createLineChartEntries(sortedList, selectedTab)
        setupLineChart(
            binding.lineChart,
            entries,
            selectedTab,
            dummyTemperatureResponse.averageReading,
            dummyTemperatureResponse.typicalRange
        )
    }

    private fun createLineChartEntries(
        list: List<TemperatureResponse.TemperatureData>,
        tab: String?,
    ): List<Entry> {

        val entries = mutableListOf<Entry>()

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC") // Parse as UTC

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

    private fun setupLineChart(
        lineChart: LineChart,
        entries: List<Entry>,
        tab: String?,
        averageReading: Double?,
        typicalRange: List<Int>?,
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
            valueFormatter = object : ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String {
                    return entry?.y?.toInt()?.toString() ?: ""
                }
            }
        }

        val rangeMin = typicalRange?.getOrNull(0)?.toFloat() ?: 0f
        val rangeMax = typicalRange?.getOrNull(1)?.toFloat() ?: 0f

        val rangeEntries = createFlatRange(tab, entries, rangeMax)

        val rangeDataSet = LineDataSet(rangeEntries, "").apply {
            color = Color.TRANSPARENT
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)

            fillColor = "#353535".toColorInt() // your grey band
            fillAlpha = 100

            mode = LineDataSet.Mode.LINEAR

            // THIS IS KEY 👇
            fillFormatter = IFillFormatter { _, _ -> rangeMin } // bottom of range
        }

        //val lineData = LineData(dataSet)
        val lineData = LineData(rangeDataSet, dataSet)

        lineChart.apply {
            data = lineData
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                granularity = 1f
                isGranularityEnabled = false
                setDrawGridLines(false)
                /*valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }*/
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

            val minY = entries.minOfOrNull { it.y } ?: 0f
            val maxY = entries.maxOfOrNull { it.y } ?: 0f

            val minTypical = typicalRange?.minOrNull()?.toFloat() ?: minY
            val maxTypical = typicalRange?.maxOrNull()?.toFloat() ?: maxY

            val overallMin = minOf(minY, minTypical)

            averageReading?.let {
                val avgLine =
                    LimitLine(averageReading.toFloat() /*getString(R.string.your_baseline)*/).apply {
                        lineWidth = 2f
                        enableDashedLine(10f, 10f, 0f) // dotted effect
                        lineColor = Color.parseColor("#353535")
                        textColor = Color.WHITE
                        textSize = 10f
                    }
                axisLeft.addLimitLine(avgLine)
            }

            // Y Axis
            axisLeft.apply {
                granularity = 1f
                textColor = Color.WHITE
                axisMinimum = maxOf(0f, overallMin)
                axisMaximum = maxOf(maxY, maxTypical) + 5f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            invalidate()
        }
    }

    private fun createFlatRange(tab: String?, entries: List<Entry>, max: Float): List<Entry> {
        val startX = when (tab) {
            "Hour" -> 0f
            "Day" -> 0f
            else -> entries.firstOrNull()?.x ?: 0f
        }
        val endX = when (tab) {
            "Hour" -> 59f
            "Day" -> 23f
            else -> entries.lastOrNull()?.x ?: 0f
        }
        return listOf(Entry(startX, max), Entry(endX, max))
    }

}