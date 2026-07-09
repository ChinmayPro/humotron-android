package com.humotron.app.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemConfigDeviceListBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.util.getTimeAgo
import java.time.Instant
import android.graphics.Color
import com.humotron.app.R

class DeviceListAdapter(
    private val userDevices: List<UserDevice>,
    private val onItemClick: (UserDevice) -> Unit,
) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemConfigDeviceListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(userDevices[position])
    }

    override fun getItemCount(): Int = userDevices.size

    inner class DeviceViewHolder(private val binding: ItemConfigDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userDevice: UserDevice) {
            binding.tvDeviceName.text = userDevice.deviceFacingName ?: "Unknown Device"

            if (!userDevice.dataSync.isNullOrEmpty()) {
                try {
                    val timeInMillis = Instant.parse(userDevice.dataSync).toEpochMilli()
                    val now = System.currentTimeMillis()
                    val diff = now - timeInMillis
                    val hours = diff / (1000 * 60 * 60)
                    
                    if (hours < 24) {
                        binding.tvLastConnected.text = "Synced Today"
                        binding.vStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#C4F23E")) // Lime
                    } else {
                        binding.tvLastConnected.text = "Last sync ${getTimeAgo(timeInMillis)}"
                        binding.vStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFB340")) // Orange
                    }
                } catch (e: Exception) {
                    binding.tvLastConnected.text = "-"
                    binding.vStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#6E837F"))
                }
            } else {
                binding.tvLastConnected.text = "-"
                binding.vStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#6E837F"))
            }

            // Simple logic to map name to icon based on the HTML reference
            val nameLower = binding.tvDeviceName.text.toString().lowercase()
            when {
                nameLower.contains("wrist") || nameLower.contains("band") -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_wrist_band)
                nameLower.contains("ring") -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_smart_ring)
                nameLower.contains("cuff") -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_smart_cuff)
                nameLower.contains("scale") -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_weight_scale)
                nameLower.contains("urine") -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_scan_droplet)
                nameLower.contains("apple") || nameLower.contains("watch") -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_alarm_24px)
                nameLower.contains("weather") -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_wrist_band) // fallback if exists, or error
                else -> binding.ivDeviceIcon.setImageResource(R.drawable.ic_wrist_band) // default
            }

            binding.root.setOnClickListener {
                onItemClick(userDevice)
            }
        }
    }
}
