package com.humotron.app.ui.bioHack.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemBiohackBookBinding
import com.humotron.app.domain.modal.response.BookLikeResponse
import com.humotron.app.domain.modal.response.BookRecommendation

class BookAdapter(val action: OnBookItemActions) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    var list = arrayListOf<BookRecommendation>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookViewHolder {

        return BookViewHolder(
            ItemBiohackBookBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: BookViewHolder,
        position: Int
    ) {

        val book = list[position]
        val context = holder.binding.root.context
        holder.binding.apply {
            tvTitle.text = book.bookTitle
            tvAuthor.text = book.author1?.uppercase() ?: ""

            val tagText = when {
                !book.primaryTag?.tagName.isNullOrEmpty() && book.primaryTag?.tagName != "Metabolic Science" -> book.primaryTag?.tagName
                !book.category?.tagName.isNullOrEmpty() && book.category?.tagName != "Metabolic Science" -> book.category?.tagName
                !book.primaryTag?.tagName.isNullOrEmpty() -> book.primaryTag?.tagName
                else -> book.category?.tagName ?: ""
            }
            if (!tagText.isNullOrEmpty()) {
                tvTags.text = tagText
                tvTags.visibility = android.view.View.VISIBLE
            } else {
                tvTags.visibility = android.view.View.GONE
            }

            tvPrice.text = "£${book.price}"

            if (book.isLiked == true) {
                llFav.background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_heart_active)
                ivFav.setImageResource(R.drawable.ic_fav_selected)
                ivFav.imageTintList = ColorStateList.valueOf(Color.parseColor("#F0795E"))
            } else {
                llFav.background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_circle_icon)
                ivFav.setImageResource(R.drawable.ic_fav)
                ivFav.imageTintList = ColorStateList.valueOf(Color.parseColor("#7E8E8C"))
            }

            if (book.isCart == true) {
                btnAddToCart.text = context.getString(R.string.remove_cart)
                btnAddToCart.background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_shop_remove_btn)
                btnAddToCart.setTextColor(Color.WHITE)
                btnAddToCart.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
            } else {
                btnAddToCart.text = "Add"
                btnAddToCart.background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_shop_add_btn)
                btnAddToCart.setTextColor(Color.parseColor("#0D1618"))
                btnAddToCart.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor("#0D1618"))
            }

            llFav.setOnClickListener {
                book.id?.let { bookId -> action.likeBooks(bookId) }
            }

            flBookIconContainer.setOnClickListener {
                book.id?.let { bookId -> action.openSummary(bookId) }
            }

            ivBookSummary.setOnClickListener {
                book.id?.let { bookId -> action.openSummary(bookId) }
            }

            btnAddToCart.setOnClickListener {
                if (book.isCart != true) {
                    book.id?.let { bookId -> action.addToCart(bookId) }
                }
            }

        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(recommendations: List<BookRecommendation>) {
        this.list = recommendations as ArrayList<BookRecommendation>
        notifyDataSetChanged()
    }

    fun updateLikeStatus(book: BookLikeResponse.Data.Book) {
        val index = list.indexOfFirst { it.id == book.bookId }
        if (index != -1) {
            list[index] = list[index].copy(isLiked = book.isLiked == true)
            notifyItemChanged(index)
        }
    }

    fun updateAddToCartStatus(cart: String) {
        val index = list.indexOfFirst { it.id == cart }
        if (index != -1) {
            list[index] = list[index].copy(isCart = true)
            notifyItemChanged(index)
        }
    }

    class BookViewHolder(val binding: ItemBiohackBookBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnBookItemActions {
        fun likeBooks(bookId: String)
        fun openSummary(bookId: String)
        fun addToCart(bookId: String)
    }


}