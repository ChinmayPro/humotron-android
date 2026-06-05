package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemSupportSearchFilterBinding

data class SearchFilterChip(
    val label: String,
    val type: FilterType,
    val key: String,
    var isSelected: Boolean = false
)

enum class FilterType {
    ALL, CATEGORY, SUBCATEGORY
}

class SupportSearchFilterAdapter(
    private val onFilterSelected: (SearchFilterChip) -> Unit
) : RecyclerView.Adapter<SupportSearchFilterAdapter.FilterViewHolder>() {

    private val filters = mutableListOf<SearchFilterChip>()

    fun setData(newFilters: List<SearchFilterChip>) {
        filters.clear()
        filters.addAll(newFilters)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemSupportSearchFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    override fun getItemCount(): Int = filters.size

    inner class FilterViewHolder(
        private val binding: ItemSupportSearchFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchFilterChip) {
            binding.tvFilterLabel.text = item.label
            binding.tvFilterLabel.isSelected = item.isSelected

            binding.tvFilterLabel.setOnClickListener {
                if (!item.isSelected) {
                    filters.forEach { it.isSelected = false }
                    item.isSelected = true
                    notifyDataSetChanged()
                    onFilterSelected(item)
                }
            }
        }
    }
}
