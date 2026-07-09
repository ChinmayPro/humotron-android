package com.humotron.app.ui.device.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
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
                    setColorIcons(state.color, R.drawable.ic_heart_2, binding)
                    state.title to state.color
                }

                HealthScanType.TEMPERATURE.value -> {
                    val state = ThermalAnalyzer.analyze(baseline, current)
                    setColorIcons(state.color, R.drawable.ic_wave_2, binding)
                    state.title to state.color
                }

                HealthScanType.HR.value -> {
                    val state = BPMLoadAnalyzer.analyze(baseline, current)
                    setColorIcons(state.color, R.drawable.ic_metrics_o2, binding)
                    state.title to state.color
                }

                HealthScanType.SPO2.value -> {
                    val state = SpO2Analyzer.analyze(baseline, current)
                    setColorIcons(state.color, R.drawable.ic_metrics_thermo, binding)
                    state.title to state.color
                }

                else -> {
                    "Stable" to android.graphics.Color.parseColor("#00C620")
                }
            }

            binding.tvStatusTitle.text = title
            binding.tvStatusTitle.setTextColor(color)
            binding.tvReportDate.text = utcOffsetToLocalTime(item.createdAt, "dd MMM yyyy, hh:mm a")
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

    fun setColorIcons(mColor: Int, drawable: Int, binding: ItemPastScanBinding) {
        binding.ivHeart.setImageResource(drawable)
        binding.ivHeart.setColorFilter(ContextCompat.getColor(binding.root.context, mColor))
        val transparentColor = ColorUtils.setAlphaComponent(mColor, (255 * 0.2f).toInt())
        binding.cvIconBackground.setCardBackgroundColor(transparentColor)
        binding.btnViewReport.setTextColor(ContextCompat.getColor(binding.root.context, mColor))
        binding.btnViewReport.iconTint =
            ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, mColor))
    }
}
