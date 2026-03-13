package com.humotron.app.ui.connect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDeviceListBinding
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

class DeviceListAdapter(val items: ArrayList<DeviceInfo>, val onItemClicked: (DeviceInfo) -> Unit) :
    RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ItemDeviceListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val data = items[position]
        holder.binding.apply {
            ivWearable.setImageResource(data.image)
            tvWearableName.text = data.title
            tvWearableDesc.text = data.desc
            root.setOnClickListener {
                onItemClicked(data)
            }
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(val binding: ItemDeviceListBinding) : RecyclerView.ViewHolder(binding.root)
}

@Parcelize
data class DeviceInfo(
    @DrawableRes
    val image: Int,
    val title: String,
    val desc: String,
) : Parcelable
