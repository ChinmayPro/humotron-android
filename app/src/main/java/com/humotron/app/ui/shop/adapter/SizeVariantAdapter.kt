package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemSizeVariantBinding
import com.humotron.app.domain.modal.response.SizeVariant

class SizeVariantAdapter(
    private var list: List<SizeVariant>,
    private val onSizeSelected: (SizeVariant) -> Unit
) : RecyclerView.Adapter<SizeVariantAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(val binding: ItemSizeVariantBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSizeVariantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvSize.text = item.size

        holder.binding.clMain.isSelected = selectedPosition == position

        holder.binding.root.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onSizeSelected(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<SizeVariant>) {
        this.list = newList
        selectedPosition = 0 // Reset selection on color change
        notifyDataSetChanged()
        if (list.isNotEmpty()) {
            onSizeSelected(list[0])
        }
    }
}
