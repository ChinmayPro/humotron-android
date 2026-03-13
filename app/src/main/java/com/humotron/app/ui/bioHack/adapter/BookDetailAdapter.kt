package com.humotron.app.ui.bioHack.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemBookGistBinding
import com.humotron.app.databinding.ItemBookHeaderTagBinding
import com.humotron.app.databinding.ItemBookNuggetBinding
import com.humotron.app.databinding.ItemBookRecommendationBinding
import com.humotron.app.databinding.ItemBookSeeMoreBinding
import com.humotron.app.domain.modal.response.BookModal
import com.humotron.app.domain.modal.response.BookType
import com.humotron.app.ui.bioHack.adapter.BookAdapter.OnBookItemActions

class BookDetailAdapter(val action: OnBookItemActions) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list = arrayListOf<BookModal>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            BookType.TAG.ordinal -> {
                Log.e("TAG", "onCreateViewHolder:TAG ${BookType.TAG.ordinal}")
                TagViewHolder(
                    ItemBookHeaderTagBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            BookType.NUGGET.ordinal -> {
                Log.e("TAG", "onCreateViewHolder:NUGGET ${BookType.NUGGET.ordinal}")
                NuggetViewHolder(
                    ItemBookNuggetBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            BookType.GIST.ordinal -> {
                Log.e("TAG", "onCreateViewHolder:GIST ${BookType.GIST.ordinal}")
                GistViewHolder(
                    ItemBookGistBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            BookType.SEE_MORE.ordinal -> {
                Log.e("TAG", "onCreateViewHolder:SEE_MORE ${BookType.SEE_MORE.ordinal}")
                SeeMoreViewHolder(
                    ItemBookSeeMoreBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            BookType.BOOK.ordinal -> {
                Log.e("TAG", "onCreateViewHolder:BOOK ${BookType.BOOK.ordinal}")
                BookViewHolder(
                    ItemBookRecommendationBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                Log.e("TAG", "onCreateViewHolder: else")
                BookViewHolder(
                    ItemBookRecommendationBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val data = list[position]
        when (holder) {
            is BookViewHolder -> {
                holder.binding.apply {
                    val adapter = holder.bookAdapter ?: BookAdapter(action).also {
                        holder.bookAdapter = it
                        rvBooksRecommendation.adapter = it
                        rvBooksRecommendation.layoutManager = LinearLayoutManager(
                            holder.binding.root.context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    }

                    data.bookRecommendation?.let {
                        adapter.setData(it)
                    }
                }
            }

            is TagViewHolder -> {
                holder.binding.apply {
                    tvPrimaryTag.text = data.primaryTag?.primaryTag
                    tvCategory.text = data.primaryTag?.category
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }

    class BookViewHolder(val binding: ItemBookRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bookAdapter: BookAdapter? = null

    }

    class TagViewHolder(val binding: ItemBookHeaderTagBinding) :
        RecyclerView.ViewHolder(binding.root)

    class NuggetViewHolder(val binding: ItemBookNuggetBinding) :
        RecyclerView.ViewHolder(binding.root)

    class GistViewHolder(val binding: ItemBookGistBinding) :
        RecyclerView.ViewHolder(binding.root)

    class SeeMoreViewHolder(val binding: ItemBookSeeMoreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }

    fun setList(list: ArrayList<BookModal>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun getList(): ArrayList<BookModal> {
        return list
    }


}