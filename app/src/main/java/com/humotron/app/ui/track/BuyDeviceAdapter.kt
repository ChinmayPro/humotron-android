package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.databinding.ItemBuyDeviceBinding
import com.humotron.app.domain.modal.response.YetToTrackDevice

class BuyDeviceAdapter(
    private var devices: List<YetToTrackDevice>,
    private val onExploreClick: (YetToTrackDevice) -> Unit,
) : RecyclerView.Adapter<BuyDeviceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBuyDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int = devices.size

    fun updateData(newDevices: List<YetToTrackDevice>) {
        devices = newDevices
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemBuyDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: YetToTrackDevice) {
            binding.tvDeviceName.text = device.deviceName ?: ""
            binding.tvDevicePrice.text = device.deviceModelPrice ?: ""
            binding.tvDeviceDesc.text = device.deviceModelDesc ?: ""

            device.deviceImage?.firstOrNull()?.let { imageUrl ->
                Glide.with(binding.ivDeviceIcon.context)
                    .load(imageUrl)
                    .into(binding.ivDeviceIcon)
            }

            binding.btnDeviceBuy.setOnClickListener {
                onExploreClick(device)
            }
        }
    }
}
