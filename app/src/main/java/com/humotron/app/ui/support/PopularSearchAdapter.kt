package com.humotron.app.ui.support

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemPopularSearchBinding
import com.humotron.app.domain.modal.response.PopularSearchKeyword

class PopularSearchAdapter(
    private val onKeywordClick: (String) -> Unit
) : RecyclerView.Adapter<PopularSearchAdapter.ViewHolder>() {

    private val items = mutableListOf<PopularSearchKeyword>()

    fun setData(newItems: List<PopularSearchKeyword>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPopularSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemPopularSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PopularSearchKeyword) {
            val keyword = item.keyword ?: ""
            binding.tvKeyword.text = keyword
            binding.root.setOnClickListener {
                if (keyword.isNotEmpty()) {
                    onKeywordClick(keyword)
                }
            }
        }
    }
}
