package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSupportDeviceBinding
import com.humotron.app.domain.modal.response.SupportConnectedDevice
import com.humotron.app.util.loadImage

class SupportDeviceAdapter(
    private val onDeviceClick: (SupportConnectedDevice) -> Unit
) : RecyclerView.Adapter<SupportDeviceAdapter.ViewHolder>() {

    private val items = mutableListOf<SupportConnectedDevice>()

    fun setData(newItems: List<SupportConnectedDevice>) {
        items.clear()
        // Limit to show maximum 4 items
        items.addAll(newItems.take(4))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemSupportDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SupportConnectedDevice, position: Int) {
            binding.tvDeviceName.text = item.deviceLabel ?: ""
            
            val statusText = item.status ?: ""
            val capitalizedStatus = statusText.replaceFirstChar { it.uppercase() }
            binding.tvDeviceStatus.text = capitalizedStatus
            
            val dotColorRes = if (statusText.equals("connected", ignoreCase = true)) {
                R.color.colorBgBtn1
            } else {
                R.color.disconnected
            }
            binding.vStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(binding.root.context, dotColorRes)
            )

            // Load device image (automatically handles PNG or SVG using loadImage extension)
            binding.ivDeviceIcon.loadImage(item.deviceUrl)
            binding.ivDeviceIcon.setColorFilter(
                ContextCompat.getColor(binding.root.context, R.color.ink2)
            )

            binding.root.setOnClickListener {
                onDeviceClick(item)
            }
        }
    }
}
