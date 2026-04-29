package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemHealthScanBinding

class HealthScanAdapter(
    private val onScanNowClick: (HealthScanItem) -> Unit,
    private val onPastScansClick: (HealthScanItem) -> Unit
) : ListAdapter<HealthScanItem, HealthScanAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHealthScanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HealthScanItem) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            
            binding.btnScanNow.setOnClickListener {
                onScanNowClick(item)
            }
            
            binding.tvPastScans.setOnClickListener {
                onPastScansClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HealthScanItem>() {
        override fun areItemsTheSame(oldItem: HealthScanItem, newItem: HealthScanItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: HealthScanItem, newItem: HealthScanItem): Boolean {
            return oldItem == newItem
        }
    }
}