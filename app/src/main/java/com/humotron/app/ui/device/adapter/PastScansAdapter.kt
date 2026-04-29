package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemPastScanBinding
import com.humotron.app.domain.modal.response.PastScanData
import com.humotron.app.ui.HRVAnalyzer
import com.humotron.app.ui.HRVState
import com.humotron.app.util.formatDateToMMMddyyyy

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
            val state = HRVAnalyzer.analyze(
                item.baseline ?: 0.0,
                item.current ?: 0.0
            )
            binding.tvStatus.text = state.title
            binding.tvStatus.setTextColor(state.color)
            binding.tvDate.text = formatDateToMMMddyyyy(item.createdAt)
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
