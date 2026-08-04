package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemShopMetricBinding
import com.humotron.app.domain.modal.response.DeviceDetailResponse

class ShopMetricAdapter(private val metrics: List<DeviceDetailResponse.Metric>) :
    RecyclerView.Adapter<ShopMetricAdapter.MetricViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val binding = ItemShopMetricBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MetricViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        holder.bind(metrics[position])
    }

    override fun getItemCount(): Int = metrics.size

    class MetricViewHolder(private val binding: ItemShopMetricBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(metric: DeviceDetailResponse.Metric) {
            binding.tvMetricName.text = metric.metricName
            binding.tvMetricWhat.text = metric.metricWhat

            val unit = getMetricUnit(metric.metricName)
            if (unit.isNotEmpty()) {
                binding.tvMetricUnit.visibility = View.VISIBLE
                binding.tvMetricUnit.text = unit
            } else {
                binding.tvMetricUnit.visibility = View.GONE
            }

            binding.ivMetricIcon.setImageResource(getMetricIcon(metric.metricName))
        }

        private fun getMetricUnit(metricName: String?): String {
            val name = metricName?.lowercase() ?: return ""
            return when {
                name.contains("weight") && !name.contains("rate") -> "KG"
                name.contains("fat rate") || name.contains("protein") || name.contains("spo2") || name.contains("subcutaneous") -> "%"
                name.contains("fat") && !name.contains("rate") && !name.contains("subcutaneous") -> "LEVEL"
                name.contains("lean") || name.contains("bone") -> "KG"
                name.contains("bmi") -> "INDEX"
                name.contains("bmr") || name.contains("calories") -> "KCAL"
                name.contains("age") -> "YRS"
                name.contains("bp") || name.contains("pressure") -> "MMHG"
                name.contains("pulse") || name.contains("heart rate") || name.contains("hr") -> "BPM"
                name.contains("hrv") -> "MS"
                name.contains("temp") -> "°C"
                name.contains("sleep") -> "HRS"
                name.contains("steps") || name.contains("count") -> "COUNT"
                name.contains("glucose") -> "MG/DL"
                name.contains("ph") -> "LEVEL"
                name.contains("ratio") || name.contains("gravity") -> "RATIO"
                else -> ""
            }
        }

        private fun getMetricIcon(metricName: String?): Int {
            val name = metricName?.lowercase() ?: return R.drawable.ic_metrics_spark
            return when {
                name.contains("weight") || name.contains("mass") || name.contains("bone") -> R.drawable.ic_metrics_scale
                name.contains("fat") || name.contains("droplet") || name.contains("ph") || name.contains("blood") -> R.drawable.ic_metrics_droplet
                name.contains("lean") || name.contains("pulse") || name.contains("hrv") -> R.drawable.ic_metrics_pulse
                name.contains("bmi") || name.contains("gravity") || name.contains("stress") || name.contains("map") -> R.drawable.ic_metrics_gauge
                name.contains("bmr") || name.contains("energy") || name.contains("ketone") || name.contains("activity") || name.contains("age") -> R.drawable.ic_start_energy
                name.contains("heart") -> R.drawable.ic_metrics_heart
                name.contains("sleep") -> R.drawable.ic_metrics_moon
                name.contains("o2") || name.contains("oxygen") -> R.drawable.ic_metrics_o2
                name.contains("temp") -> R.drawable.ic_metrics_thermo
                name.contains("steps") -> R.drawable.ic_track
                name.contains("glucose") -> R.drawable.ic_metrics_glucose
                name.contains("protein") || name.contains("flask") || name.contains("nitrite") || name.contains("leukocyte") -> R.drawable.ic_opt_flask
                else -> R.drawable.ic_metrics_spark
            }
        }
    }
}
