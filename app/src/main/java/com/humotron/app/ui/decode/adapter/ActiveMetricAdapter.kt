package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemActiveMetricBinding
import com.humotron.app.domain.modal.ui.ActiveMetric

class ActiveMetricAdapter(
    private val items: List<ActiveMetric>,
    private val onItemClick: (ActiveMetric) -> Unit
) : RecyclerView.Adapter<ActiveMetricAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveMetricBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemActiveMetricBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ActiveMetric) {
            binding.tvValue.text = item.value
            binding.tvLabel.text = item.label
            binding.tvDateRange.text = item.dateRange
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
