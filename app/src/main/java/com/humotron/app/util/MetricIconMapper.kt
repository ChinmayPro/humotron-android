package com.humotron.app.util

import com.humotron.app.R

object MetricIconMapper {
    fun getIconName(metricName: String?): String {
        val name = metricName?.lowercase() ?: ""
        return when {
            name.contains("sleep") || name.contains("rem") || name.contains("deep") || name.contains("nap") -> "moon.fill"
            name.contains("hrv") || name.contains("heart") || name.contains("pulse") || name.contains("cardio") || name.contains("rhr") -> "heart.fill"
            name.contains("oxygen") || name.contains("spo2") || name.contains("o2") || name.contains("breath") || name.contains("respir") -> "lungs.fill"
            name.contains("glucose") || name.contains("sugar") || name.contains("insulin") -> "drop.fill"
            name.contains("weight") || name.contains("mass") || name.contains("bmi") || name.contains("body fat") || name.contains("fat") -> "scalemass.fill"
            name.contains("temp") -> "thermometer.medium"
            name.contains("strain") || name.contains("exercise") || name.contains("steps") || name.contains("train") || name.contains("activity") || name.contains("energy") -> "bolt.fill"
            name.contains("pressure") || name.contains("bp") || name.contains("systolic") || name.contains("diastolic") -> "gauge.with.dots.needle.bottom.50percent"
            name.contains("stress") || name.contains("recovery") || name.contains("mood") -> "waveform.path.ecg"
            else -> "chart.line.uptrend.xyaxis"
        }
    }

    fun getIconResource(metricName: String?): Int {
        return when (getIconName(metricName)) {
            "moon.fill" -> R.drawable.ic_metrics_moon
            "heart.fill" -> R.drawable.ic_metrics_heart
            "lungs.fill" -> R.drawable.ic_metrics_o2
            "drop.fill" -> R.drawable.ic_metrics_glucose
            "scalemass.fill" -> R.drawable.ic_metrics_scale
            "thermometer.medium" -> R.drawable.ic_metrics_thermo
            "bolt.fill" -> R.drawable.ic_metrics_spark
            "gauge.with.dots.needle.bottom.50percent" -> R.drawable.ic_metrics_gauge
            "waveform.path.ecg" -> R.drawable.ic_metrics_pulse
            else -> R.drawable.ic_metrics_moon // Default
        }
    }
}
