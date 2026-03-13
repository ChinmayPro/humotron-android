package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemVpCategoryBinding
import com.humotron.app.domain.modal.response.Tag

class CategoryVpAdapter(val onValueSelected: (Int, Int) -> Unit) :
    RecyclerView.Adapter<CategoryVpAdapter.CategoryViewHolder>() {

    var list = arrayListOf<Tag>()

    // Define your ViewHolder and other necessary methods here
    class CategoryViewHolder(val binding: ItemVpCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Initialize your views here
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Inflate your item layout and return the ViewHolder
        return CategoryViewHolder(
            ItemVpCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // Bind data to your ViewHolder
        val item = list[position]
        holder.binding.apply {
            rvCategoryTags.adapter = PrimaryTagAdapter {
                onValueSelected(it, item.primaryTag?.size ?: 0)
            }.apply {
                this.setData(item.primaryTag ?: emptyList())
            }
        }
    }

    override fun getItemCount(): Int {
        // Return the size of your data set
        return list.size
    }

    fun setData(tags: List<Tag>) {
        list = ArrayList(tags)
        notifyDataSetChanged()
    }
}