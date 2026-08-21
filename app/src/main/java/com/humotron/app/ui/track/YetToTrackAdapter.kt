package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemYetToTrackBinding
import com.humotron.app.domain.modal.response.UntrackedMetricData
import com.humotron.app.util.MetricIconMapper

class YetToTrackAdapter(
    private var metrics: List<UntrackedMetricData>,
    private val onItemClick: (UntrackedMetricData) -> Unit,
) : RecyclerView.Adapter<YetToTrackAdapter.YetToTrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YetToTrackViewHolder {
        val itemBinding = ItemYetToTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YetToTrackViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: YetToTrackViewHolder, position: Int) {
        holder.bind(metrics[position])
    }

    override fun getItemCount(): Int = metrics.size

    fun updateData(newMetrics: List<UntrackedMetricData>) {
        metrics = newMetrics
        notifyDataSetChanged()
    }

    inner class YetToTrackViewHolder(private val itemBinding: ItemYetToTrackBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(metric: UntrackedMetricData) {
            itemBinding.tvSleepTitle.text = metric.metricName ?: "Unknown Metric"
            itemBinding.tvSleepDesc.text = metric.metricWhat ?: ""
            itemBinding.btnSleepTrack.text = "Track with ${metric.deviceName ?: "Device"}"
            itemBinding.ivYetToTrack.setImageResource(MetricIconMapper.getIconResource(metric.metricName))

            itemBinding.root.setOnClickListener {
                onItemClick(metric)
            }
        }
    }
}
