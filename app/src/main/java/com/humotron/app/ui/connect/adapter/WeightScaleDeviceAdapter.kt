package com.humotron.app.ui.connect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.bt.weight.WeightScaleDeviceSummary
import com.humotron.app.databinding.ItemWeightScaleDeviceBinding

class WeightScaleDeviceAdapter(
    private val onDeviceSelected: (WeightScaleDeviceSummary?) -> Unit,
) : RecyclerView.Adapter<WeightScaleDeviceAdapter.ViewHolder>() {

    private val devices = mutableListOf<WeightScaleDeviceSummary>()
    private var selectedMac: String? = null

    fun submitList(items: List<WeightScaleDeviceSummary>) {
        devices.clear()
        devices.addAll(items)
        if (selectedMac != null && devices.none { it.mac == selectedMac }) {
            selectedMac = null
            onDeviceSelected(null)
        }
        notifyDataSetChanged()
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
        holder.bind(devices[position], devices[position].mac == selectedMac)
    }

    override fun getItemCount(): Int = devices.size

    inner class ViewHolder(
        private val binding: ItemWeightScaleDeviceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: WeightScaleDeviceSummary, isSelected: Boolean) {
            binding.ivDeviceImage.setImageResource(R.drawable.ic_connect_weightscale_logo)
            binding.tvDeviceName.text =
                itemView.context.getString(R.string.humotron_weight_scale)
            binding.tvDeviceModel.text = device.name.ifBlank { device.bluetoothName ?: device.mac }
            binding.cbDeviceChecked.isChecked = isSelected

            binding.root.setOnClickListener {
                selectedMac = device.mac
                notifyDataSetChanged()
                onDeviceSelected(device)
            }
        }
    }
}
