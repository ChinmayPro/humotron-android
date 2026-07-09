package com.humotron.app.ui.connect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.bt.bp.BpDiscoveredDevice
import com.humotron.app.databinding.ItemWeightScaleDeviceBinding

class BpMachineDeviceAdapter(
    private val onDeviceSelected: (BpDiscoveredDevice?) -> Unit,
) : RecyclerView.Adapter<BpMachineDeviceAdapter.ViewHolder>() {

    private val devices = mutableListOf<BpDiscoveredDevice>()
    private var selectedMac: String? = null

    fun submitList(items: List<BpDiscoveredDevice>) {
        devices.clear()
        devices.addAll(items)
        if (selectedMac != null && devices.none { it.macAddress == selectedMac }) {
            selectedMac = null
            onDeviceSelected(null)
        }
        notifyDataSetChanged()
    }

    fun clearData() {
        val hadSelection = selectedMac != null
        val previousSize = devices.size

        if (previousSize == 0 && !hadSelection) {
            return
        }

        devices.clear()
        selectedMac = null

        if (hadSelection) {
            onDeviceSelected(null)
        }

        if (previousSize > 0) {
            notifyItemRangeRemoved(0, previousSize)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWeightScaleDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position], devices[position].macAddress == selectedMac)
    }

    override fun getItemCount(): Int = devices.size

    inner class ViewHolder(
        private val binding: ItemWeightScaleDeviceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BpDiscoveredDevice, isSelected: Boolean) {
            binding.ivDeviceIcon.setImageResource(R.drawable.ic_smart_cuff_vector)
            binding.tvDeviceName.text = device.name
            binding.tvDeviceModel.text = (device.model).toString().ifBlank { "BP Machine" }
            binding.tvDeviceColor.text = "MAC: ${device.macAddress}"
            binding.tvDeviceGeneration.text = "Model: ${device.model}"
            binding.tvDeviceSize.text = ""
            binding.cbDeviceChecked.isChecked = isSelected

            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }
                selectedMac = device.macAddress
                notifyDataSetChanged()
                onDeviceSelected(device)
            }
        }
    }
}
