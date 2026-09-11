package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBioHackBookBinding
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.response.BookModal
import com.humotron.app.domain.modal.response.BookType
import com.humotron.app.domain.modal.response.TagModal
import com.humotron.app.ui.bioHack.adapter.BookAdapter
import com.humotron.app.ui.bioHack.adapter.BookDetailAdapter
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BioHackBookFragment : BaseFragment(R.layout.fragment_bio_hack_book),
    BookAdapter.OnBookItemActions {

    private lateinit var binding: FragmentBioHackBookBinding
    private val viewModel: NuggetsViewModel by viewModels()
    private lateinit var adapter: BookDetailAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBioHackBookBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 30)
            insets
        }
        viewModel.getBookDetail()

        adapter = BookDetailAdapter(this)
        binding.rvBooks.adapter = adapter
        binding.rvBooks.layoutManager = LinearLayoutManager(requireContext())

        binding.header.tvBooks.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
        binding.header.indicatorBooks.visibility = android.view.View.VISIBLE
        binding.header.tvNuggets.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
        binding.header.indicatorNuggets.visibility = android.view.View.INVISIBLE
        binding.header.tvProgress.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
        binding.header.indicatorProgress.visibility = android.view.View.INVISIBLE

        binding.header.tabNuggets.setOnClickListener {
            findNavController().navigate(R.id.fragmentNuggets)
        }

        binding.header.tabProgress.setOnClickListener {
            findNavController().navigate(R.id.fragmentProgress)
        }


        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.getBookDetailData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = it.data?.data ?: return@observe
                    val list = arrayListOf<BookModal>()
                    if (!data.books.isNullOrEmpty()) {
                        // 1. Gist card at the very top
                        list.add(BookModal(BookType.GIST.ordinal))

                        for ((index, item) in data.books.withIndex()) {
                            list.add(
                                BookModal(
                                    type = BookType.TAG.ordinal,
                                    primaryTag = TagModal(item.primaryTag, item.category)
                                )
                            )
                            list.add(
                                BookModal(
                                    type = BookType.BOOK.ordinal,
                                    bookRecommendation = item.bookRecommendation
                                )
                            )
                        }

                        // 2. Nugget banner card near the end
                        list.add(BookModal(BookType.NUGGET.ordinal))

                        adapter.setList(list)
                    }

                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    showApiError(it.error)
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.likeBookData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.data?.book?.let { book ->
                        val list = adapter.getList()
                        val newList = list.map {
                            if (it.type == BookType.BOOK.ordinal) {
                                it.copy(bookRecommendation = it.bookRecommendation?.map {
                                    if (it.id == book.bookId) {
                                        it.copy(isLiked = book.isLiked == true)
                                    } else {
                                        it
                                    }
                                }
                                )
                            } else {
                                it
                            }
                        }
                        adapter.setList(ArrayList(newList))
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    showApiError(it.error)
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.addToCartData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.data?.cart?.cart.let { book ->
                        val list = adapter.getList()
                        val newList = list.map { bookModal ->
                            if (bookModal.type == BookType.BOOK.ordinal) {
                                bookModal.copy(bookRecommendation = bookModal.bookRecommendation?.map { bookRecommendation ->
                                    if (bookRecommendation.id == it.data?.data?.id) {
                                        bookRecommendation.copy(isCart = true)
                                    } else {
                                        bookRecommendation
                                    }
                                }
                                )
                            } else {
                                bookModal
                            }
                        }
                        adapter.setList(ArrayList(newList))
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    showApiError(it.error)
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    override fun likeBooks(bookId: String) {
        viewModel.likeBook(bookId)
    }

    override fun openSummary(bookId: String) {
        val dialog = BookSummaryFragment()
        val bundle = Bundle()
        bundle.putString("bookId", bookId)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "book_summary")
    }

    override fun addToCart(bookId: String) {
        viewModel.addToCart(
            AddToCartParam(
                productId = bookId,
                quantity = 1,
                variantId = "",
                productType = "book",
                cartItemId = ""
            )
        )
    }
}