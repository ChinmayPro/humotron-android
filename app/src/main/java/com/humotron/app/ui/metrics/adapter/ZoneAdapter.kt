package com.humotron.app.ui.metrics.adapter

import android.R.attr.progressDrawable
import android.R.attr.text
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemZoneBinding
import com.humotron.app.domain.modal.response.Zone
import androidx.core.graphics.toColorInt
import com.humotron.app.R

class ZoneAdapter : ListAdapter<Zone, ZoneAdapter.ZoneViewHolder>(ZoneDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val binding = ItemZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ZoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ZoneViewHolder(private val binding: ItemZoneBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(zone: Zone) {
            binding.tvZone.text = "Zone ${zone.zone}"
            val zoneType = zone.type.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            binding.tvIntensityType.text = zoneType
            binding.tvRange.text = zone.range
            binding.tvTimeSpent.text = "${zone.timeSpent} mins"
            binding.progress.progress = ((zone.timeSpent / zone.targetTime) * 100).toInt()

            val color = getZoneColor(zone.zone)
            val progressDrawable = binding.progress.progressDrawable.mutate()
            val progressLayer = (progressDrawable as LayerDrawable)
                .findDrawableByLayerId(android.R.id.progress)
            progressLayer.setTint(color)

        }
    }

    class ZoneDiffCallback : DiffUtil.ItemCallback<Zone>() {
        override fun areItemsTheSame(oldItem: Zone, newItem: Zone): Boolean {
            return oldItem.zone == newItem.zone
        }

        override fun areContentsTheSame(oldItem: Zone, newItem: Zone): Boolean {
            return oldItem == newItem
        }
    }

    fun getZoneColor(zone: Int): Int {
        return when (zone) {
            1 -> "#FFF36C".toColorInt()
            2 -> "#FF9100".toColorInt()
            3 -> "#6563FF".toColorInt()
            4 -> "#B99FFE".toColorInt()
            5 -> "#91D40B".toColorInt()
            else -> Color.TRANSPARENT
        }
    }
}