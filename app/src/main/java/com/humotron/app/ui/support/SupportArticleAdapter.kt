package com.humotron.app.ui.support

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSupportArticleBinding
import com.humotron.app.domain.modal.response.PopularArticle

class SupportArticleAdapter(
    private val onArticleClick: (PopularArticle) -> Unit
) : RecyclerView.Adapter<SupportArticleAdapter.ViewHolder>() {

    private val items = mutableListOf<PopularArticle>()

    fun setData(newItems: List<PopularArticle>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
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

        fun bind(item: PopularArticle) {
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
                    binding.ivArticleIcon.setImageResource(R.drawable.ic_cell_tower_24px)
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
