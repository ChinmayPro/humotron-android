package com.humotron.app.ui.metrics

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.humotron.app.domain.modal.response.Zone

class RingExerciseIntensityZonesChartView(
    private val pieChart: PieChart,
) {

    init {
        setupChart()
    }

    private fun setupChart() {
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.holeRadius = 65f
        pieChart.isHighlightPerTapEnabled = false
        pieChart.legend.isEnabled = false
        //pieChart.setExtraOffsets(24f, 24f, 24f, 24f)
        pieChart.setNoDataText("No zone activity for this period.")
        pieChart.setNoDataTextColor(Color.GRAY)
    }

    fun configure(zoneList: List<Zone>) {

        val activeZones = zoneList.filter {
            it.type != ExerciseIntensityType.RESTING_INTENSITY.chartLegendName && it.zone > 0
        }

        val allZonesSorted = activeZones.sortedBy { it.zone }

        val zonesWithTime = allZonesSorted.filter { it.timeSpent > 0 }

        val totalTime = zonesWithTime.sumOf { it.timeSpent }

        if (allZonesSorted.isEmpty()) {
            pieChart.clear()
            return
        }

        val proportionByZone = mutableMapOf<Int, Float>()

        if (totalTime > 0 && zonesWithTime.isNotEmpty()) {
            zonesWithTime.forEach {
                proportionByZone[it.zone] = (it.timeSpent / totalTime).toFloat()
            }
        }

        allZonesSorted.map { it.zone }.forEach {
            if (!proportionByZone.containsKey(it)) {
                proportionByZone[it] = 0f
            }
        }

        val entries = ArrayList<PieEntry>()
        val zoneColors = allZonesSorted.map { zoneValue ->
            when (zoneValue.zone) {
                1 -> "#FFF36C".toColorInt()
                2 -> "#FF9100".toColorInt()
                3 -> "#6563FF".toColorInt()
                4 -> "#B99FFE".toColorInt()
                5 -> "#91D40B".toColorInt()
                else -> Color.GRAY
            }
        }

        allZonesSorted.forEach { zoneValue ->
            val proportion = proportionByZone[zoneValue.zone] ?: 0f
            entries.add(
                PieEntry(
                    proportion,
                    ""
                )
            )
        }

        val dataSet = PieDataSet(entries, "")

        dataSet.sliceSpace = 20f
        dataSet.selectionShift = 0f
        dataSet.colors = zoneColors

        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 11f
        dataSet.valueFormatter = PercentageOnlyFormatter()

        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.invalidate()
    }

    // Kotlin equivalent of Swift private extension
    private val ExerciseIntensityType.chartLegendName: String
        get() = when (this) {
            ExerciseIntensityType.LIGHT_INTENSITY -> "Light"
            ExerciseIntensityType.MODERATE_INTENSITY -> "Moderate"
            ExerciseIntensityType.VIGOROUS_INTENSITY -> "Vigorous"
            ExerciseIntensityType.HIGH_INTENSITY -> "High"
            ExerciseIntensityType.MAXIMUM_INTENSITY -> "Max"
            ExerciseIntensityType.RESTING_INTENSITY -> "Resting"
        }
}

class PercentageOnlyFormatter : ValueFormatter() {
    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
        if (value <= 0f) return ""
        val percent = (value * 100).toInt()
        return "$percent%"
    }
}

enum class ExerciseIntensityType {
    LIGHT_INTENSITY,
    MODERATE_INTENSITY,
    VIGOROUS_INTENSITY,
    HIGH_INTENSITY,
    MAXIMUM_INTENSITY,
    RESTING_INTENSITY
}