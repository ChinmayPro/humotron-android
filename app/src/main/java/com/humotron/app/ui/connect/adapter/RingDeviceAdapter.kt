package com.humotron.app.ui.connect.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.databinding.ItemRingDeviceBinding
import com.humotron.app.util.toRingColor

class RingDeviceAdapter(private val onDeviceSelected: (RingBleDevice?) -> Unit) :
    RecyclerView.Adapter<RingDeviceAdapter.ViewHolder>() {

    private val devices = mutableListOf<RingBleDevice>()
    private var selectedPosition = -1

    @SuppressLint("NotifyDataSetChanged")
    fun addDevice(device: RingBleDevice) {
        val existingIndex = devices.indexOfFirst { it.device.address == device.device.address }
        if (existingIndex == -1) {
            devices.add(device)
            notifyItemInserted(devices.size - 1)
        } else {
            devices[existingIndex] = device
            notifyItemChanged(existingIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRingDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = devices.size

    inner class ViewHolder(private val binding: ItemRingDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(device: RingBleDevice, isSelected: Boolean) {
            binding.tvDeviceModel.text = device.device.name
            binding.tvDeviceColor.text = itemView.context.toRingColor(device.color)
            binding.tvDeviceGeneration.text = "Gen ${device.generation}"
            binding.tvDeviceSize.text = "Size ${device.size}"
            binding.cbDeviceChecked.isChecked = isSelected

            binding.root.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = bindingAdapterPosition
                if (previousSelected != -1) {
                    notifyItemChanged(previousSelected)
                }
                notifyItemChanged(selectedPosition)
                onDeviceSelected(devices[selectedPosition])
            }
        }
    }
}