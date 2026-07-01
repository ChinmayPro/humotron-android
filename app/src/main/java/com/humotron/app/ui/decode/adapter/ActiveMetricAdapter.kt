package com.humotron.app.ui.decode.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
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
            binding.tvLabel.text = item.label
            
            val device = item.deviceName
            if (device.isNullOrEmpty()) {
                binding.tvDeviceName.visibility = View.GONE
            } else {
                binding.tvDeviceName.visibility = View.VISIBLE
                binding.tvDeviceName.text = "⌚ ${device.uppercase()}"
            }

            if (item.value.isNullOrEmpty()) {
                binding.tvValue.visibility = View.GONE
                binding.tvBullet.visibility = View.GONE
            } else {
                binding.tvValue.visibility = View.VISIBLE
                binding.tvBullet.visibility = View.VISIBLE
                binding.tvValue.text = item.value
            }

            val isAvail = item.status == "ready"
            if (isAvail) {
                binding.tvStatus.text = "Available to chat"
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#26C4F23E"))
                binding.tvStatus.setTextColor(Color.parseColor("#C4F23E"))
                binding.root.isEnabled = true
                binding.root.alpha = 1.0f
            } else {
                binding.tvStatus.text = "Not enough data"
                binding.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10FFFFFF"))
                binding.tvStatus.setTextColor(Color.parseColor("#7D8A89"))
                binding.root.isEnabled = false
                binding.root.alpha = 0.55f
            }

            binding.root.setOnClickListener {
                if (isAvail) {
                    onItemClick(item)
                }
            }
        }
    }
}
