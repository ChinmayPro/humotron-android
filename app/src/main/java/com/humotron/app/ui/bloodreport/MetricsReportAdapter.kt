package com.humotron.app.ui.bloodreport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemReadingBinding
import com.humotron.app.domain.modal.response.MedicalPdfMetric

class MetricsReportAdapter(
    private var metrics: List<MedicalPdfMetric> = emptyList()
) : RecyclerView.Adapter<MetricsReportAdapter.MetricViewHolder>() {

    fun updateData(newMetrics: List<MedicalPdfMetric>) {
        metrics = newMetrics
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val binding = ItemReadingBinding.inflate(
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

    class MetricViewHolder(private val binding: ItemReadingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(metric: MedicalPdfMetric) {
            binding.tvReadingName.text = metric.metricName
            binding.tvReadingValue.text = metric.metricReading
            binding.tvReadingUnit.text = metric.metricUnit
            binding.tvReadingRange.text = metric.metricDate

            // Hide status badge as MedicalPdfMetric doesn't have status info
            binding.llReadingStatus.visibility = View.GONE
        }
    }
}
