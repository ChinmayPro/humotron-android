package com.humotron.app.ui.shop.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.databinding.ItemShopCategoryHeaderBinding
import com.humotron.app.databinding.ItemShopDeviceBinding
import com.humotron.app.databinding.ItemShopDeviceHeaderBinding
import com.humotron.app.domain.modal.response.GetShopDevicesResponse

class ShopDeviceAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var adapterItems: List<Any> = emptyList()

    companion object {
        private const val TYPE_PROMO_HEADER = 0
        private const val TYPE_CATEGORY_HEADER = 1
        private const val TYPE_DEVICE_ITEM = 2
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(response: GetShopDevicesResponse) {
        val items = mutableListOf<Any>()
        items.add(PromoHeader()) // Object to represent the promotional banner at the top
        
        response.data?.forEach { category ->
            items.add(category) // The Category Header (e.g. Wearables)
            category.devices?.forEach { device ->
                items.add(device) // The Device Item
            }
        }
        adapterItems = items
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = adapterItems[position]) {
            is PromoHeader -> TYPE_PROMO_HEADER
            is GetShopDevicesResponse.CategoryData -> TYPE_CATEGORY_HEADER
            is GetShopDevicesResponse.Device -> TYPE_DEVICE_ITEM
            else -> throw IllegalArgumentException("Unknown item type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PROMO_HEADER -> PromoHeaderViewHolder(ItemShopDeviceHeaderBinding.inflate(inflater, parent, false))
            TYPE_CATEGORY_HEADER -> CategoryHeaderViewHolder(ItemShopCategoryHeaderBinding.inflate(inflater, parent, false))
            else -> ItemViewHolder(ItemShopDeviceBinding.inflate(inflater, parent, false), onItemClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = adapterItems[position]
        when (holder) {
            is CategoryHeaderViewHolder -> holder.bind(item as GetShopDevicesResponse.CategoryData)
            is ItemViewHolder -> holder.bind(item as GetShopDevicesResponse.Device)
        }
    }

    override fun getItemCount(): Int = adapterItems.size

    // Dummy object for the top promotional header
    private class PromoHeader

    class PromoHeaderViewHolder(binding: ItemShopDeviceHeaderBinding) : RecyclerView.ViewHolder(binding.root)
    
    class CategoryHeaderViewHolder(private val binding: ItemShopCategoryHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: GetShopDevicesResponse.CategoryData) {
            binding.tvCategoryTitle.text = category.deviceCategoryName
        }
    }

    var onItemClick: ((GetShopDevicesResponse.Device) -> Unit)? = null

    class ItemViewHolder(private val binding: ItemShopDeviceBinding, private val onItemClick: ((GetShopDevicesResponse.Device) -> Unit)?) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: GetShopDevicesResponse.Device) {
            binding.root.setOnClickListener {
                onItemClick?.invoke(device)
            }
            binding.btnExplore.setOnClickListener {
                onItemClick?.invoke(device)
            }
            // Set basic details
            binding.tvDeviceName.text = device.deviceFacingName ?: device.deviceName
            binding.tvDeviceCategory.text = device.deviceName // Using internal name as sub-label or model
            
            // Format metrics list with arrows (Limit to 4)
            val metricsText = device.metrics?.take(4)?.joinToString("\n") { "➔ ${it.metricName}" }
            binding.tvMetrics.text = metricsText

            // Handle price from deviceModel object
            val price = device.deviceModel?.deviceModelPrice
            binding.tvPrice.text = if (!price.isNullOrEmpty()) "£$price" else "£---"

            // Load remote image with Glide, fallback to local generated placeholders
            val imageUrl = device.deviceImage?.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(binding.ivDeviceImage.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_bg_main)
                    .error(R.drawable.ic_bg_main)
                    .into(binding.ivDeviceImage)
            } else {
                val placeholderRes = when {
                    device.deviceFacingName?.contains("Ring", true) == true || 
                    device.deviceName?.contains("Ring", true) == true -> R.drawable.ic_smart_ring
                    device.deviceFacingName?.contains("Band", true) == true || 
                    device.deviceName?.contains("Band", true) == true -> R.drawable.ic_wrist_band
                    else -> R.drawable.ic_bg_main
                }
                binding.ivDeviceImage.setImageResource(placeholderRes)
            }
        }
    }
}
