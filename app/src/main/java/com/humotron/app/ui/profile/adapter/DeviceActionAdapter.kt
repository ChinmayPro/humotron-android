package com.humotron.app.ui.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDeviceActionBinding
import com.humotron.app.domain.modal.ui.DeviceAction

class DeviceActionAdapter(
    private val actions: List<DeviceAction>,
    private val onActionClick: (DeviceAction) -> Unit
) : RecyclerView.Adapter<DeviceActionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceActionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount(): Int = actions.size

    inner class ViewHolder(private val binding: ItemDeviceActionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(action: DeviceAction) {
            binding.ivActionIcon.setImageResource(action.iconRes)
            binding.tvActionTitle.setText(action.titleRes)
            binding.cvAction.setOnClickListener {
                onActionClick(action)
            }
        }
    }
}
