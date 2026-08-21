package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemTrackingGroupedMetricBinding
import com.humotron.app.domain.modal.response.GroupedMetricData

class TrackingGroupedAdapter(
    private var groupedMetrics: List<GroupedMetricData>,
    private val onItemClick: (GroupedMetricData) -> Unit,
) : RecyclerView.Adapter<TrackingGroupedAdapter.GroupedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupedViewHolder {
        val binding =
            ItemTrackingGroupedMetricBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupedViewHolder, position: Int) {
        holder.bind(groupedMetrics[position])
    }

    override fun getItemCount(): Int = groupedMetrics.size

    fun updateData(newGroupedMetrics: List<GroupedMetricData>) {
        groupedMetrics = newGroupedMetrics
        notifyDataSetChanged()
    }

    inner class GroupedViewHolder(private val binding: ItemTrackingGroupedMetricBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GroupedMetricData) {
            binding.mtvCategoryName.text = data.categoryName ?: "Unknown Category"
            binding.mtvDeviceName.text = data.deviceName ?: ""

            binding.root.setOnClickListener {
                onItemClick(data)
            }
        }
    }
}
