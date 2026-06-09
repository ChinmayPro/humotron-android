package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSupportViewAllArticleBinding
import com.humotron.app.domain.modal.response.SearchTopicItem

class SupportViewAllArticleAdapter(
    private val onArticleClick: (SearchTopicItem) -> Unit
) : RecyclerView.Adapter<SupportViewAllArticleAdapter.ViewHolder>() {

    private val items = mutableListOf<SearchTopicItem>()

    fun setData(newItems: List<SearchTopicItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportViewAllArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemSupportViewAllArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchTopicItem, position: Int) {
            binding.tvArticleTitle.text = item.title ?: ""
            binding.tvArticleSubtitle.text = item.subtitle ?: ""

            val subcatName = item.subcategoryLabel ?: ""
            val typeLabel = when (item.articleType?.lowercase()) {
                "how_to" -> "How-to"
                "troubleshooting" -> "Troubleshooting"
                "faq" -> "FAQ"
                "policy" -> "Policy"
                else -> item.articleType?.replaceFirstChar { it.uppercase() } ?: ""
            }

            val typeIcon = when (item.articleType?.lowercase()) {
                "how_to" -> R.drawable.ic_menu_book_24px
                "troubleshooting" -> R.drawable.ic_cell_tower_24px
                "faq" -> R.drawable.ic_help_outline_24px
                "policy" -> R.drawable.ic_sheet_document
                else -> R.drawable.ic_sheet_document
            }
            binding.ivArticleIcon.setImageResource(typeIcon)

            val subcategoryPart = if (subcatName.isNotEmpty()) "$subcatName • " else ""
            val metaText = "$subcategoryPart$typeLabel"
            binding.tvArticleMetaInfo.text = metaText

            // Hide divider for the last item in the list
            binding.vDivider.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                onArticleClick(item)
            }
        }
    }
}
