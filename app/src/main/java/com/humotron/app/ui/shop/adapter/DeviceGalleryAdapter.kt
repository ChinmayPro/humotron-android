package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.databinding.ItemDeviceGalleryImageBinding

class DeviceGalleryAdapter(private val images: List<String>) :
    RecyclerView.Adapter<DeviceGalleryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDeviceGalleryImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceGalleryImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(images[position])
            .into(holder.binding.ivGalleryItem)
    }

    override fun getItemCount(): Int = images.size
}
