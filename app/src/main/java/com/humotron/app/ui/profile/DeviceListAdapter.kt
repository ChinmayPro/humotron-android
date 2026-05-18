package com.humotron.app.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemConfigDeviceListBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.util.getTimeAgo
import java.time.Instant

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
                    binding.tvLastConnected.text = getTimeAgo(timeInMillis)
                } catch (e: Exception) {
                    binding.tvLastConnected.text = "-"
                }
            } else {
                binding.tvLastConnected.text = "-"
            }

            binding.root.setOnClickListener {
                onItemClick(userDevice)
            }
        }
    }
}
