package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemDeviceNewBinding
import com.humotron.app.domain.modal.response.WearableProviderResponse.WearableDevice
import com.humotron.app.util.getTimeAgo
import java.time.Instant

class WearableProviderAdapter(
    private var devices: List<WearableDevice>,
    private val onItemClick: (WearableDevice) -> Unit,
) : RecyclerView.Adapter<WearableProviderAdapter.ProviderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val binding =
            ItemDeviceNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProviderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    fun updateData(newDevices: List<WearableDevice>) {
        devices = newDevices
        notifyDataSetChanged()
    }

    private fun formatProviderName(provider: String?): String {
        return provider?.split("_")?.joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        } ?: "Unknown Provider"
    }

    inner class ProviderViewHolder(private val binding: ItemDeviceNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: WearableDevice) {
            binding.tvWearableName.text = formatProviderName(device.provider)
            binding.tvSourceVia.text = device.status?.replaceFirstChar { it.uppercase() } ?: ""
            
            binding.tvHrTitle.isVisible = false
            binding.tvHr.isVisible = false
            binding.tvHrUnit.isVisible = false
            binding.tvHrvTitle.isVisible = false
            binding.tvHrv.isVisible = false
            binding.tvHrvUnit.isVisible = false
            //binding.btnExploreMore.isVisible = false

            if (!device.lastSyncedAt.isNullOrEmpty()) {
                try {
                    val timeInMillis = Instant.parse(device.lastSyncedAt).toEpochMilli()
                    binding.tvLastSync.text = getTimeAgo(timeInMillis)
                    binding.tvLastSync.isVisible = true
                } catch (e: Exception) {
                    binding.tvLastSync.isVisible = false
                }
            } else {
                binding.tvLastSync.isVisible = false
            }

            val drawable = when (device.provider?.lowercase()) {
                "whoop" -> R.drawable.ic_band_vectr // Replace with actual whoop icon if available
                "apple" -> R.drawable.ic_band_vectr
                else -> R.drawable.ic_band_vectr
            }
            binding.ivDevice.setImageResource(drawable)

            binding.root.setOnClickListener {
                onItemClick(device)
            }
        }
    }
}
