package com.humotron.app.ui.decode.custom

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

object ChartHelper {
    
    fun setupVerticalBarChart(chart: BarChart, values: List<Float>, labels: List<String>, colorRes: Int) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textColor = Color.parseColor("#859390") // deep_dives_ink3
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f

        val yAxisLeft = chart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.textColor = Color.TRANSPARENT

        chart.axisRight.isEnabled = false

        val entries = values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        val dataSet = BarDataSet(entries, "Values")
        dataSet.color = colorRes
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 13f

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        chart.data = barData
        chart.invalidate()
    }

    fun setupLineChart(chart: LineChart, datasets: List<LineDatasetInfo>, labels: List<String>) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setDrawGridBackground(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#859390")
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f

        val yAxisLeft = chart.axisLeft
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.textColor = Color.TRANSPARENT

        chart.axisRight.isEnabled = false

        val lineDataSets = datasets.map { info ->
            val entries = info.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
            val dataSet = LineDataSet(entries, info.name)
            dataSet.color = info.color
            dataSet.setCircleColor(info.color)
            dataSet.circleRadius = 4f
            dataSet.lineWidth = 2f
            dataSet.valueTextSize = 0f // hide values on dots
            dataSet
        }

        chart.data = LineData(lineDataSets)
        chart.invalidate()
    }

    data class LineDatasetInfo(val name: String, val values: List<Float>, val color: Int)
}
