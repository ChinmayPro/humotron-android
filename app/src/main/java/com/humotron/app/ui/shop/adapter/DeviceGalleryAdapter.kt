package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDeviceGalleryImageBinding
import com.humotron.app.util.loadImage

class DeviceGalleryAdapter(
    private val images: List<String>,
    private val placeholderRes: Int? = null
) : RecyclerView.Adapter<DeviceGalleryAdapter.ViewHolder>() {

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
        holder.binding.ivGalleryItem.loadImage(images[position], placeholderRes)
    }

    override fun getItemCount(): Int = images.size
}
