package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemDecodeMetricCardBinding
import com.humotron.app.ui.decode.DecodeAnimationUtils

data class DisplayMetric(
    val id: String,
    val name: String,
    val deviceName: String,
    val state: String,
    val hasInsight: Boolean,
    val availableDays: Int,
    val insightMinData: Int,
    val lastSyncDate: String?
)

class DecodeInsightsAdapter(
    private val items: List<DisplayMetric>,
    private val onItemClick: (DisplayMetric) -> Unit
) : RecyclerView.Adapter<DecodeInsightsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDecodeMetricCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemDecodeMetricCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DisplayMetric) {
            val context = itemView.context
            val name = item.name
            binding.tvMetricName.text = name

            val rawDevice = item.deviceName
            val deviceEmoji = when {
                rawDevice.contains("WristBand", ignoreCase = true) -> "⌚"
                rawDevice.contains("AppleWatch", ignoreCase = true) -> "⌚"
                rawDevice.contains("Ring", ignoreCase = true) -> "💍"
                rawDevice.contains("BPMachine", ignoreCase = true) -> "🩺"
                rawDevice.contains("UrineStrip", ignoreCase = true) -> "🧪"
                rawDevice.contains("Weight", ignoreCase = true) -> "⚖️"
                rawDevice.contains("DeepScan", ignoreCase = true) -> "🔍"
                else -> "⌚"
            }
            binding.tvDeviceName.text = "$deviceEmoji $rawDevice"

            val availableDays = item.availableDays
            val minDays = item.insightMinData

            val stateLower = item.state.lowercase()
            when {
                stateLower.contains("data_ready") || stateLower.contains("ready") -> {
                    binding.tvMetricStatusBadge.text = context.getString(R.string.insights_badge_ready)
                    binding.tvMetricStatusBadge.setBackgroundResource(R.drawable.bg_badge_ready)
                    binding.tvMetricStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_green))

                    binding.tvActionReady.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                    binding.tvActionNotTracked.visibility = View.GONE
                    binding.root.alpha = 1.0f
                }
                stateLower.contains("insufficient_data") || stateLower.contains("collecting") || stateLower.contains("almost") -> {
                    binding.tvMetricStatusBadge.text = context.getString(R.string.insights_badge_collecting)
                    binding.tvMetricStatusBadge.setBackgroundResource(R.drawable.bg_badge_gen)
                    binding.tvMetricStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_amber))

                    binding.tvActionReady.visibility = View.GONE
                    binding.layoutProgress.visibility = View.VISIBLE
                    binding.tvActionNotTracked.visibility = View.GONE
                    binding.root.alpha = 1.0f

                    val progressPct = if (minDays > 0) (availableDays * 100) / minDays else 0
                    binding.pbMetricProgress.progress = progressPct
                    binding.tvProgressDays.text = context.getString(R.string.insights_progress_days_format, availableDays, minDays)
                }
                else -> {
                    binding.tvMetricStatusBadge.text = context.getString(R.string.insights_badge_not_tracked)
                    binding.tvMetricStatusBadge.setBackgroundResource(R.drawable.bg_badge_none)
                    binding.tvMetricStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_slate_grey))

                    binding.tvActionReady.visibility = View.GONE
                    binding.layoutProgress.visibility = View.GONE
                    binding.tvActionNotTracked.visibility = View.VISIBLE
                    binding.root.alpha = 0.6f
                }
            }

            binding.root.setOnClickListener { onItemClick(item) }
            DecodeAnimationUtils.addPressEffect(binding.root)
        }
    }
}
