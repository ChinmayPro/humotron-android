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
        holder.binding.apply {
            tvTitle.text = book.bookTitle
            tvAuthor.text = book.author1
            tvTags.text = "${book.category?.tagName}, ${book.primaryTag?.tagName}"
            tvPrice.text = "£${book.price}"
            ivFav.isChecked = book.isLiked == true

            if (book.isCart == true) {
                btnAddToCart.text = holder.binding.root.context.getString(R.string.remove_cart)
                btnAddToCart.backgroundTintList = ColorStateList.valueOf(Color.RED)
            } else {
                btnAddToCart.text = holder.binding.root.context.getString(R.string.buy_now)
                btnAddToCart.backgroundTintList =
                    holder.binding.root.context.getColorStateList(R.color.btn_pink)
            }

            llFav.setOnClickListener {
                book.id?.let { bookId -> action.likeBooks(bookId) }
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