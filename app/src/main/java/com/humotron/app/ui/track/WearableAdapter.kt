package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.databinding.ItemWearablesBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale

class WearableAdapter(
    private val wearables: List<Wearable>,
    private val onItemClick: (Wearable) -> Unit,
) : RecyclerView.Adapter<WearableAdapter.WearableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WearableViewHolder {
        val binding =
            ItemWearablesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WearableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WearableViewHolder, position: Int) {
        holder.bind(wearables[position])
    }

    override fun getItemCount(): Int = wearables.size

    inner class WearableViewHolder(private val binding: ItemWearablesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wearable: Wearable) {
            binding.tvWearableName.text = wearable.deviceFacingName ?: "Unknown Device"

            val hrvMetric = wearable.metrics?.firstOrNull { it.key == "hrv" }
            if (hrvMetric != null) {
                binding.tvHrv.text = hrvMetric.value
                binding.tvHrvUnit.text = hrvMetric.unit
            } else {
                binding.tvHrv.text = "-"
                binding.tvHrvUnit.text = ""
            }

            val hrMetric = wearable.metrics?.firstOrNull { it.key == "heartRate" }
                ?: wearable.metrics?.firstOrNull { it.key == "singleHR" }

            if (hrMetric != null) {
                binding.tvHr.text = hrMetric.value
                binding.tvHrUnit.text = hrMetric.unit
            } else {
                binding.tvHr.text = "-"
                binding.tvHrUnit.text = ""
            }

            if (!wearable.dataSync.isNullOrEmpty()) {
                try {
                    // Always parses correctly (UTC-aware)
                    val timeInMillis = Instant.parse(wearable.dataSync).toEpochMilli()
                    binding.tvLastSync.text = getTimeAgo(timeInMillis)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.tvLastSync.text = "-"
                }
            } else {
                binding.tvLastSync.text = "-"
            }

            if (!wearable.deviceImage.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(wearable.deviceImage[0])
                    .into(binding.ivDevice)
            }

            binding.root.setOnClickListener {
                onItemClick(wearable)
            }
        }

        private fun getTimeAgo(timeInMillis: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timeInMillis

            if (diff < 0) return "just now"

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7

            return when {
                seconds < 5 -> "just now"
                seconds < 60 -> "$seconds seconds ago"
                minutes < 60 -> "$minutes minutes ago"
                hours < 24 -> "$hours hours ago"
                days < 7 -> "$days days ago"
                else -> "$weeks weeks ago"
            }
        }
    }
}
