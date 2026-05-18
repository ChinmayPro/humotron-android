package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.databinding.ItemWearablesBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.util.getTimeAgo
import java.time.Instant

class WearableAdapter(
    private val userDevices: List<UserDevice>,
    private val onItemClick: (UserDevice) -> Unit,
) : RecyclerView.Adapter<WearableAdapter.WearableViewHolder>() {

    private val HR_KEYS = setOf("heartRate", "singleHR", "HR", "hr", "HeartRate")
    private val HRV_KEYS = setOf("hrv", "HRV", "variability")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WearableViewHolder {
        val binding =
            ItemWearablesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WearableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WearableViewHolder, position: Int) {
        holder.bind(userDevices[position])
    }

    override fun getItemCount(): Int = userDevices.size

    inner class WearableViewHolder(private val binding: ItemWearablesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userDevice: UserDevice) {
            binding.tvWearableName.text = userDevice.deviceFacingName ?: "Unknown Device"

            val hrvMetric = userDevice.metrics.findMetric(HRV_KEYS)
            if (hrvMetric != null) {
                binding.tvHrv.text = hrvMetric.value
                binding.tvHrvUnit.text = hrvMetric.unit
            } else {
                binding.tvHrv.text = "-"
                binding.tvHrvUnit.text = ""
            }

            val hrMetric = userDevice.metrics.findMetric(HR_KEYS)

            if (hrMetric != null) {
                binding.tvHr.text = hrMetric.value
                binding.tvHrUnit.text = hrMetric.unit
            } else {
                binding.tvHr.text = "-"
                binding.tvHrUnit.text = ""
            }

            if (!userDevice.dataSync.isNullOrEmpty()) {
                try {
                    // Always parses correctly (UTC-aware)
                    val timeInMillis = Instant.parse(userDevice.dataSync).toEpochMilli()
                    binding.tvLastSync.text = getTimeAgo(timeInMillis)
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.tvLastSync.text = "-"
                }
            } else {
                binding.tvLastSync.text = "-"
            }

            if (!userDevice.deviceImage.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(userDevice.deviceImage[0])
                    .into(binding.ivDevice)
            }

            binding.root.setOnClickListener {
                onItemClick(userDevice)
            }
        }
    }

    fun List<UserDevice.Metric>?.findMetric(keys: Set<String>): UserDevice.Metric? {
        return this?.firstOrNull { it.key in keys }
    }
}
