package com.humotron.app.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemConfigDeviceListBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.response.WearableProviderResponse
import com.humotron.app.util.getTimeAgo
import java.time.Instant
import java.util.Locale

sealed class DeviceItem {
    abstract val id: String?
    abstract val displayName: String
    abstract val syncTimestamp: String?

    data class UserDeviceItem(val userDevice: GetAllDeviceResponse.Data.UserDevice) : DeviceItem() {
        override val id: String? get() = userDevice.id
        override val displayName: String get() = userDevice.deviceFacingName ?: userDevice.deviceName ?: "Unknown Device"
        override val syncTimestamp: String? get() = userDevice.dataSync
    }

    data class WearableDeviceItem(val wearableDevice: WearableProviderResponse.WearableDevice) : DeviceItem() {
        override val id: String? get() = wearableDevice.id
        override val displayName: String get() = formatProviderName(wearableDevice.provider)
        override val syncTimestamp: String? get() = wearableDevice.lastSyncedAt

        private fun formatProviderName(provider: String?): String {
            if (provider.isNullOrEmpty()) return "Unknown Provider"
            return provider.split("_").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }
    }
}

class DeviceListAdapter(
    private var items: List<DeviceItem>,
    private val onItemClick: (DeviceItem) -> Unit,
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
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<DeviceItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(private val binding: ItemConfigDeviceListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeviceItem) {
            binding.tvDeviceName.text = item.displayName

            val syncTimestamp = item.syncTimestamp
            if (!syncTimestamp.isNullOrEmpty()) {
                try {
                    val timeInMillis = Instant.parse(syncTimestamp).toEpochMilli()
                    val now = System.currentTimeMillis()
                    val diff = now - timeInMillis
                    val hours = diff / (1000 * 60 * 60)
                    val days = hours / 24

                    val syncText = when {
                        hours < 24 -> "Synced Today"
                        hours in 24..47 -> "Synced Yesterday"
                        days in 2..6 -> "Synced $days days ago"
                        else -> "Last sync ${getTimeAgo(timeInMillis)}"
                    }
                    binding.tvLastConnected.text = syncText

                    if (hours < 48) {
                        binding.vStatusDot.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#C4F23E")) // Lime
                    } else {
                        binding.vStatusDot.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#FFB340")) // Orange
                    }
                } catch (e: Exception) {
                    binding.tvLastConnected.text = "-"
                    binding.vStatusDot.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#6E837F"))
                }
            } else {
                binding.tvLastConnected.text = "-"
                binding.vStatusDot.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#6E837F"))
            }

            val nameLower = item.displayName.lowercase()
            when {
                nameLower.contains("wrist") || nameLower.contains("band") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_wrist_band)
                nameLower.contains("ring") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_smart_ring)
                nameLower.contains("cuff") || nameLower.contains("bpmachine") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_smart_cuff)
                nameLower.contains("scale") || nameLower.contains("weight") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_weight_scale)
                nameLower.contains("urine") || nameLower.contains("strip") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_scan_droplet)
                nameLower.contains("apple") || nameLower.contains("watch") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_alarm_24px)
                nameLower.contains("weather") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_weather)
                nameLower.contains("calendar") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_clock)
                nameLower.contains("whoop") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_metrics_pulse)
                nameLower.contains("google") || nameLower.contains("health") ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_google)
                else ->
                    binding.ivDeviceIcon.setImageResource(R.drawable.ic_wrist_band)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
