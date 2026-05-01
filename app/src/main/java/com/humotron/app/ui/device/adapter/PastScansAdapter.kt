package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemPastScanBinding
import com.humotron.app.domain.modal.response.PastScanData
import com.humotron.app.util.analyzer.BPMLoadAnalyzer
import com.humotron.app.util.analyzer.HRVAnalyzer
import com.humotron.app.util.analyzer.SpO2Analyzer
import com.humotron.app.util.analyzer.ThermalAnalyzer
import com.humotron.app.util.utcOffsetToLocalTime

class PastScansAdapter(
    private val onItemClick: (PastScanData) -> Unit,
) : ListAdapter<PastScanData, PastScansAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPastScanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPastScanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PastScanData) {
            val typeString = item.type ?: "HRV"
            val baseline = item.baseline ?: 0.0
            val current = item.current ?: 0.0

            val (title, color) = when (typeString) {
                HealthScanType.HRV.value -> {
                    val state = HRVAnalyzer.analyze(baseline, current)
                    state.title to state.color
                }

                HealthScanType.TEMPERATURE.value -> {
                    val state = ThermalAnalyzer.analyze(baseline, current)
                    state.title to state.color
                }

                HealthScanType.HR.value -> {
                    val state = BPMLoadAnalyzer.analyze(baseline, current)
                    state.title to state.color
                }

                HealthScanType.SPO2.value -> {
                    val state = SpO2Analyzer.analyze(baseline, current)
                    state.title to state.color
                }

                else -> {
                    "Stable" to android.graphics.Color.parseColor("#00C620")
                }
            }

            binding.tvStatus.text = title
            binding.tvStatus.setTextColor(color)
            binding.tvDate.text = utcOffsetToLocalTime(item.createdAt, "dd MMM yyyy, hh:mm a")
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PastScanData>() {
        override fun areItemsTheSame(oldItem: PastScanData, newItem: PastScanData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PastScanData, newItem: PastScanData): Boolean {
            return oldItem == newItem
        }
    }
}
