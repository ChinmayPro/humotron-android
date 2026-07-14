package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemOptimizeFilterBinding

data class CategoryFilter(
    val name: String,
    val count: Int
)

class DecodeOptimizeFilterAdapter(
    private var filters: List<CategoryFilter>,
    private var selectedCategory: String = "All",
    private val onCategoryClick: (categoryName: String) -> Unit
) : RecyclerView.Adapter<DecodeOptimizeFilterAdapter.FilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemOptimizeFilterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    override fun getItemCount(): Int = filters.size

    fun updateFilters(newFilters: List<CategoryFilter>, currentSelection: String) {
        filters = newFilters
        selectedCategory = currentSelection
        notifyDataSetChanged()
    }

    inner class FilterViewHolder(private val binding: ItemOptimizeFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(filter: CategoryFilter) {
            val context = binding.root.context
            binding.tvCategoryName.text = filter.name
            binding.tvCategoryCount.text = if (filter.count > 0) filter.count.toString() else ""

            val isSelected = filter.name.equals(selectedCategory, ignoreCase = true)

            if (isSelected) {
                binding.llContainer.setBackgroundResource(R.drawable.bg_filter_on)
                binding.tvCategoryName.setTextColor(context.getColor(R.color.colorBgBtn))
                binding.tvCategoryCount.setTextColor(context.getColor(R.color.colorBgBtn))
            } else {
                binding.llContainer.setBackgroundResource(R.drawable.bg_filter_off)
                binding.tvCategoryName.setTextColor(context.getColor(R.color.textColorMuted))
                binding.tvCategoryCount.setTextColor(context.getColor(R.color.textColorMuted))
            }

            binding.root.setOnClickListener {
                onCategoryClick(filter.name)
            }
        }
    }
}
