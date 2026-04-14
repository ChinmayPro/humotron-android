package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.databinding.ItemColorVariantBinding
import com.humotron.app.domain.modal.response.ColorVariant

class ColorVariantAdapter(
    private var list: List<ColorVariant>,
    private val onColorSelected: (ColorVariant) -> Unit
) : RecyclerView.Adapter<ColorVariantAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(val binding: ItemColorVariantBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemColorVariantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvColorName.text = item.colorName
        
        // Load the first image for the color
        item.image?.firstOrNull()?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .into(holder.binding.ivColor)
        }

        holder.binding.clMain.isSelected = selectedPosition == position

        holder.binding.root.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onColorSelected(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<ColorVariant>) {
        this.list = newList
        notifyDataSetChanged()
    }

    fun setSelectedPositionByColorName(colorName: String?) {
        if (colorName == null) return
        val index = list.indexOfFirst { it.colorName == colorName }
        if (index != -1) {
            val oldPosition = selectedPosition
            selectedPosition = index
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }
}
