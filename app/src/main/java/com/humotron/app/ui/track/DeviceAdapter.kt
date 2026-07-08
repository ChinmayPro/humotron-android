package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.databinding.ItemDeviceNewBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.util.getTimeAgo
import java.time.Instant

class DeviceAdapter(
    private var userDevices: List<UserDevice>,
    private val onItemClick: (UserDevice) -> Unit,
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private val HR_KEYS = setOf("heartRate", "singleHR", "HR", "hr", "HeartRate")
    private val HRV_KEYS = setOf("hrv", "HRV", "variability")
    private val BMI_KEYS = setOf("bmi", "BMI")
    private val WEIGHT_KEYS = setOf("weight", "Weight")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding =
            ItemDeviceNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(userDevices[position])
    }

    override fun getItemCount(): Int = userDevices.size

    fun updateData(newDevices: List<UserDevice>) {
        userDevices = newDevices
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(private val binding: ItemDeviceNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userDevice: UserDevice) {
            binding.tvWearableName.text = userDevice.deviceFacingName ?: "Unknown Device"
            binding.tvSourceVia.text = binding.root.context.getString(R.string.src_humotron)

            val deviceType = DeviceType.from(userDevice.deviceName)
            when (deviceType) {
                DeviceType.BAND, DeviceType.RING -> {
                    binding.tvHrTitle.text = binding.root.context.getString(R.string.hr)
                    binding.tvHrvTitle.text = binding.root.context.getString(R.string.hrv)

                    val hrvMetric = userDevice.metrics.findMetric(HRV_KEYS)
                    if (hrvMetric != null) {
                        binding.tvHrv.text = hrvMetric.value
                        binding.tvHrvUnit.text = hrvMetric.unit ?: ""
                    } else {
                        binding.tvHrv.text = "-"
                        binding.tvHrvUnit.text = ""
                    }

                    val hrMetric = userDevice.metrics.findMetric(HR_KEYS)
                    if (hrMetric != null) {
                        binding.tvHr.text = hrMetric.value
                        binding.tvHrUnit.text = hrMetric.unit ?: ""
                    } else {
                        binding.tvHr.text = "-"
                        binding.tvHrUnit.text = ""
                    }
                }

                DeviceType.WEIGHT_MACHINE -> {
                    binding.tvHrTitle.text = binding.root.context.getString(R.string.bmi)
                    binding.tvHrvTitle.text = binding.root.context.getString(R.string.weight)

                    val bmiMetric = userDevice.metrics.findMetric(BMI_KEYS)
                    if (bmiMetric != null) {
                        binding.tvHr.text = bmiMetric.value
                        binding.tvHrUnit.text = bmiMetric.unit ?: ""
                    } else {
                        binding.tvHr.text = "-"
                        binding.tvHrUnit.text = ""
                    }

                    val weightMetric = userDevice.metrics.findMetric(WEIGHT_KEYS)
                    if (weightMetric != null) {
                        binding.tvHrv.text = weightMetric.value
                        binding.tvHrvUnit.text = weightMetric.unit ?: ""
                    } else {
                        binding.tvHrv.text = "-"
                        binding.tvHrvUnit.text = ""
                    }
                }

                else -> {
                    val metrics = userDevice.metrics
                    if (!metrics.isNullOrEmpty()) {
                        val firstMetric = metrics.getOrNull(0)
                        binding.tvHrTitle.text = firstMetric?.shortMetricName ?: ""
                        binding.tvHr.text = firstMetric?.value ?: "-"
                        binding.tvHrUnit.text = firstMetric?.unit ?: ""

                        val secondMetric = metrics.getOrNull(1)
                        binding.tvHrvTitle.text = secondMetric?.shortMetricName ?: ""
                        binding.tvHrv.text = secondMetric?.value ?: "-"
                        binding.tvHrvUnit.text = secondMetric?.unit ?: ""
                    } else {
                        binding.tvHrTitle.text = ""
                        binding.tvHr.text = "-"
                        binding.tvHrUnit.text = ""
                        binding.tvHrvTitle.text = ""
                        binding.tvHrv.text = "-"
                        binding.tvHrvUnit.text = ""
                    }
                }
            }

            if (!userDevice.dataSync.isNullOrEmpty()) {
                try {
                    val timeInMillis = Instant.parse(userDevice.dataSync).toEpochMilli()
                    binding.tvLastSync.text = getTimeAgo(timeInMillis)
                } catch (e: Exception) {
                    binding.tvLastSync.text = "-"
                }
            } else {
                binding.tvLastSync.text = "-"
            }

            val drawable = when (deviceType) {
                DeviceType.RING -> {
                    R.drawable.ic_ring_vector
                }

                DeviceType.BAND -> {
                    R.drawable.ic_band_vectr
                }

                DeviceType.BP_MACHINE -> {
                    R.drawable.ic_smart_cuff_vector
                }

                DeviceType.WEIGHT_MACHINE -> {
                    R.drawable.ic_smart_scale_vector
                }

                DeviceType.UNKNOWN -> {
                    R.drawable.ic_ring_vector
                }
            }
            binding.ivDevice.setImageResource(drawable)

            /*if (!userDevice.deviceImage.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(userDevice.deviceImage[0])
                    .into(binding.ivDevice)
            }*/

            binding.root.setOnClickListener {
                onItemClick(userDevice)
            }
        }
    }

    private fun List<UserDevice.Metric>?.findMetric(keys: Set<String>): UserDevice.Metric? {
        return this?.firstOrNull { it.key in keys }
    }
}
