package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.*
import com.humotron.app.domain.modal.response.BookLikeResponse
import com.humotron.app.domain.modal.response.BookPreferenceResponse
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer

sealed class ShopSectionItem {
    object Header : ShopSectionItem()
    data class CategorySection(val book: BookPreferenceResponse.BookData.Book) : ShopSectionItem()
    object NuggetSection : ShopSectionItem()
    object GistSection : ShopSectionItem()
    object SeeMoreSection : ShopSectionItem()
}

class ShopParentAdapter(
    private val action: ShopBookAdapter.OnBookItemActions,
    private val onNuggetClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<ShopSectionItem>()
    private val childAdapters = mutableMapOf<Int, ShopBookAdapter>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CATEGORY = 1
        private const val VIEW_TYPE_NUGGETS = 2
        private const val VIEW_TYPE_GIST = 3
        private const val VIEW_TYPE_SEE_MORE = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ShopSectionItem.Header -> VIEW_TYPE_HEADER
            is ShopSectionItem.CategorySection -> VIEW_TYPE_CATEGORY
            is ShopSectionItem.NuggetSection -> VIEW_TYPE_NUGGETS
            is ShopSectionItem.GistSection -> VIEW_TYPE_GIST
            is ShopSectionItem.SeeMoreSection -> VIEW_TYPE_SEE_MORE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemShopHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_CATEGORY -> CategoryViewHolder(ItemShopCategoryRowBinding.inflate(inflater, parent, false), action)
            VIEW_TYPE_NUGGETS -> NuggetViewHolder(ItemBookNuggetBinding.inflate(inflater, parent, false), onNuggetClick)
            VIEW_TYPE_GIST -> GistViewHolder(ItemBookGistBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SEE_MORE -> SeeMoreViewHolder(ItemBookSeeMoreBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ShopSectionItem.CategorySection -> {
                (holder as CategoryViewHolder).bind(item.book)
                childAdapters[position] = holder.adapter
            }
            else -> {}
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<ShopSectionItem>) {
        this.items = newItems
        childAdapters.clear()
        notifyDataSetChanged()
    }

    fun updateLikeStatus(book: BookLikeResponse.Data.Book) {
        childAdapters.values.forEach { it.updateLikeStatus(book) }
    }

    fun updateAddToCartStatus(bookId: String, isInCart: Boolean) {
        childAdapters.values.forEach { it.updateAddToCartStatus(bookId, isInCart) }
    }

    class HeaderViewHolder(binding: ItemShopHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    class CategoryViewHolder(
        val binding: ItemShopCategoryRowBinding,
        action: ShopBookAdapter.OnBookItemActions
    ) : RecyclerView.ViewHolder(binding.root) {
        val adapter = ShopBookAdapter(action)

        init {
            binding.dsvBooks.adapter = adapter
            binding.dsvBooks.setItemTransformer(
                ScaleTransformer.Builder()
                    .setMaxScale(1.05f)
                    .setMinScale(0.8f)
                    .setPivotX(Pivot.X.CENTER)
                    .setPivotY(Pivot.Y.CENTER)
                    .build()
            )
        }

        fun bind(book: BookPreferenceResponse.BookData.Book) {
            binding.tvCategory.text = book.primaryTag
            binding.tvTag.text = book.category
            book.bookRecommendation?.let { adapter.setData(it) }
        }
    }

    class NuggetViewHolder(binding: ItemBookNuggetBinding, onNuggetClick: () -> Unit) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvExploreNuggets.setOnClickListener { onNuggetClick() }
        }
    }

    class GistViewHolder(binding: ItemBookGistBinding) : RecyclerView.ViewHolder(binding.root)

    class SeeMoreViewHolder(binding: ItemBookSeeMoreBinding) : RecyclerView.ViewHolder(binding.root)
}
