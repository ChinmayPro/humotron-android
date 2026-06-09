package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemSupportCategoryBinding
import com.humotron.app.domain.modal.response.SupportCategory
import com.humotron.app.util.loadImage

class SupportCategoryAdapter(
    private val onCategoryClick: (SupportCategory) -> Unit
) : RecyclerView.Adapter<SupportCategoryAdapter.ViewHolder>() {

    private val items = mutableListOf<SupportCategory>()

    fun setData(newItems: List<SupportCategory>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemSupportCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SupportCategory) {
            binding.tvCategoryTitle.text = item.label ?: ""
            binding.tvCategoryDesc.text = item.description ?: ""
            
            binding.ivCategoryIcon.loadImage(item.icon)

            binding.root.setOnClickListener {
                onCategoryClick(item)
            }
        }
    }
}
