package com.humotron.app.ui.support.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSupportSearchArticleBinding
import com.humotron.app.databinding.ItemSupportSearchFooterBinding
import com.humotron.app.domain.modal.response.SearchTopicItem
import com.humotron.app.util.loadImage
import java.util.Locale

class SupportSearchArticleAdapter(
    private val onArticleClick: (SearchTopicItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    private val articles = mutableListOf<SearchTopicItem>()
    private var searchQuery: String = ""
    private var subcatIcons = mapOf<String, String>()
    private var catIcons = mapOf<String, String>()

    private var showFooter = false
    private var isFooterLoading = false
    private var isFirstPage = true
    private var totalArticles = 0
    private var loadedArticles = 0
    private var onLoadMoreClick: (() -> Unit)? = null

    fun setData(
        newArticles: List<SearchTopicItem>,
        query: String,
        subcatIcons: Map<String, String> = emptyMap(),
        catIcons: Map<String, String> = emptyMap()
    ) {
        articles.clear()
        articles.addAll(newArticles)
        searchQuery = query
        this.subcatIcons = subcatIcons
        this.catIcons = catIcons
        notifyDataSetChanged()
    }

    fun setFooterState(show: Boolean, loading: Boolean = false, firstPage: Boolean = true, total: Int = 0, loaded: Int = 0) {
        val oldShowFooter = showFooter
        showFooter = show
        isFooterLoading = loading
        isFirstPage = firstPage
        totalArticles = total
        loadedArticles = loaded
        if (oldShowFooter != show) {
            if (show) {
                notifyItemInserted(articles.size)
            } else {
                notifyItemRemoved(articles.size)
            }
        } else if (show) {
            // Just update loading state
            notifyItemChanged(articles.size)
        }
    }

    fun setOnLoadMoreClickListener(listener: () -> Unit) {
        onLoadMoreClick = listener
    }

    override fun getItemViewType(position: Int): Int {
        return if (showFooter && position == articles.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FOOTER) {
            val binding = ItemSupportSearchFooterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FooterViewHolder(binding)
        } else {
            val binding = ItemSupportSearchArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ArticleViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArticleViewHolder -> holder.bind(articles[position], position)
            is FooterViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = articles.size + if (showFooter) 1 else 0

    inner class ArticleViewHolder(
        private val binding: ItemSupportSearchArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchTopicItem, position: Int) {
            binding.tvArticleTitle.text = item.title
            
            val isLastArticle = position == articles.size - 1
            binding.vDivider.visibility = if (isLastArticle && !showFooter) View.GONE else View.VISIBLE

            val category = item.categoryLabel ?: ""
            val metaText = if (category.isNotEmpty()) "$category • Quick read" else "Quick read"
            binding.tvArticleMetaInfo.text = metaText

            val excerpt = item.shortAnswer ?: item.subtitle ?: ""
            binding.tvArticleExcerpt.text = getHighlightedText(excerpt, searchQuery)

            val subcatKey = item.subcategoryKey ?: ""
            val catKey = item.categoryKey ?: ""
            val iconUrl = subcatIcons[subcatKey] ?: catIcons[catKey]

            if (!iconUrl.isNullOrEmpty()) {
                binding.ivArticleIcon.loadImage(iconUrl)
            } else {
                binding.ivArticleIcon.setImageResource(R.drawable.ic_grid)
            }

            binding.root.setOnClickListener {
                onArticleClick(item)
            }
        }

        private fun getHighlightedText(text: String, query: String): CharSequence {
            if (query.trim().isEmpty()) return text
            val spannable = SpannableString(text)
            val lowerText = text.lowercase(Locale.getDefault())
            val lowerQuery = query.trim().lowercase(Locale.getDefault())
            
            val highlightColor = androidx.core.content.ContextCompat.getColor(
                itemView.context,
                R.color.colorBgBtn1
            )
            
            var startPos = lowerText.indexOf(lowerQuery)
            while (startPos >= 0) {
                val endPos = startPos + lowerQuery.length
                spannable.setSpan(
                    ForegroundColorSpan(highlightColor),
                    startPos,
                    endPos,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                startPos = lowerText.indexOf(lowerQuery, endPos)
            }
            return spannable
        }
    }

    inner class FooterViewHolder(
        private val binding: ItemSupportSearchFooterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val ctx = binding.root.context
            if (isFooterLoading) {
                binding.tvFooterText.text = ctx.getString(R.string.support_search_loading_more)
                binding.ivFooterChevron.visibility = View.GONE
                binding.pbFooterLoading.visibility = View.VISIBLE
                binding.root.isClickable = false
            } else if (isFirstPage) {
                // First time: "View all 67 articles"
                binding.tvFooterText.text = ctx.getString(R.string.support_search_view_all, totalArticles)
                binding.ivFooterChevron.visibility = View.VISIBLE
                binding.pbFooterLoading.visibility = View.GONE
                binding.root.isClickable = true
            } else {
                // Subsequent pages: "Load more (40 of 67)"
                binding.tvFooterText.text = ctx.getString(R.string.support_search_load_more, loadedArticles, totalArticles)
                binding.ivFooterChevron.visibility = View.VISIBLE
                binding.pbFooterLoading.visibility = View.GONE
                binding.root.isClickable = true
            }

            binding.root.setOnClickListener {
                if (!isFooterLoading) {
                    onLoadMoreClick?.invoke()
                }
            }
        }
    }
}
