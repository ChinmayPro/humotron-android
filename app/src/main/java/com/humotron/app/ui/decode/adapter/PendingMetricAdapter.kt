package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemPendingMetricBinding
import com.humotron.app.domain.modal.ui.PendingMetric

class PendingMetricAdapter(
    private val items: List<PendingMetric>,
    private val onItemClick: (PendingMetric) -> Unit
) : RecyclerView.Adapter<PendingMetricAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingMetricBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemPendingMetricBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PendingMetric) {
            binding.tvLabel.text = item.label
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
