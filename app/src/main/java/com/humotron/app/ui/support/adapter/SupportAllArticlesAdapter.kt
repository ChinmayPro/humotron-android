package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSupportArticleBinding
import com.humotron.app.domain.modal.response.SearchTopicItem

class SupportAllArticlesAdapter(
    private val onArticleClick: (SearchTopicItem) -> Unit
) : RecyclerView.Adapter<SupportAllArticlesAdapter.ViewHolder>() {

    private val items = mutableListOf<SearchTopicItem>()

    fun setData(newItems: List<SearchTopicItem>) {
        val oldSize = items.size
        items.clear()
        items.addAll(newItems)
        val newSize = items.size
        if (oldSize == 0) {
            notifyDataSetChanged()
        } else if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportArticleBinding.inflate(
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
        private val binding: ItemSupportArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchTopicItem) {
            binding.tvArticleTitle.text = item.title ?: ""
            
            val subcategory = item.subcategoryLabel ?: ""
            val category = item.categoryLabel ?: ""
            binding.tvArticleSubtitle.text = if (subcategory.isNotEmpty() && category.isNotEmpty()) {
                "$subcategory • $category"
            } else {
                subcategory.ifEmpty { category }
            }

            val typeText = when (item.articleType?.lowercase()) {
                "how_to" -> {
                    binding.ivArticleIcon.setImageResource(R.drawable.ic_menu_book_24px)
                    "How-to"
                }
                "troubleshooting" -> {
                    binding.ivArticleIcon.setImageResource(R.drawable.ic_signal_waves)
                    "Troubleshooting"
                }
                "faq" -> {
                    binding.ivArticleIcon.setImageResource(R.drawable.ic_help_outline_24px)
                    "FAQ"
                }
                "policy" -> {
                    binding.ivArticleIcon.setImageResource(R.drawable.ic_sheet_document)
                    "Policy"
                }
                else -> {
                    binding.ivArticleIcon.setImageResource(R.drawable.ic_sheet_document)
                    item.articleType?.replaceFirstChar { it.uppercase() } ?: ""
                }
            }
            binding.tvArticleTag.text = typeText

            binding.root.setOnClickListener {
                onArticleClick(item)
            }
        }
    }
}
