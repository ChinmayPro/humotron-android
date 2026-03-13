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
import com.humotron.app.databinding.FragmentNuggetsDetailBinding
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.param.NuggetsInteraction
import com.humotron.app.ui.bioHack.adapter.BookAdapter
import com.humotron.app.ui.bioHack.adapter.DeepDiveAdapter
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NuggetsDetailFragment : BaseFragment(R.layout.fragment_nuggets_detail),
    BookAdapter.OnBookItemActions {

    private lateinit var binding: FragmentNuggetsDetailBinding
    private lateinit var adapter: DeepDiveAdapter
    private lateinit var bookAdapter: BookAdapter

    private val viewModel: NuggetsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNuggetsDetailBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }
        binding.header.title.text = ""

        adapter = DeepDiveAdapter()
        binding.rvDeepDives.adapter = adapter
        binding.rvDeepDives.layoutManager = LinearLayoutManager(requireContext())
        bookAdapter = BookAdapter(this)
        binding.rvBooks.adapter = bookAdapter
        binding.rvBooks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        val nuggetId = arguments?.getString("id")
        val anecdote = arguments?.getString("anecdote")

        nuggetId?.let { viewModel.getNuggetDetails(it) }
        subscribeToObservers()

        binding.ivLike.setOnClickListener {
            if (nuggetId != null && anecdote != null) {
                viewModel.nuggetsInteraction(
                    NuggetsInteraction(
                        nuggetId,
                        anecdote,
                        "like",
                        false
                    )
                )
            }
        }

        binding.ivDisLike.setOnClickListener {
            if (nuggetId != null && anecdote != null) {
                viewModel.nuggetsInteraction(
                    NuggetsInteraction(
                        nuggetId,
                        anecdote,
                        "dislike",
                        false
                    )
                )
            }
        }

        binding.ivBookMark.setOnClickListener {
            if (nuggetId != null && anecdote != null) {
                viewModel.nuggetsInteraction(
                    NuggetsInteraction(
                        nuggetId,
                        anecdote,
                        "bookmark",
                        false
                    )
                )
            }
        }

        binding.ivDetail.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun subscribeToObservers() {
        viewModel.nuggetsDetailData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = it.data?.data ?: return@observe
                    binding.viewAnimator.displayedChild = 1
                    binding.apply {
                        tvTitle.text = data.nugget?.nuggetTopic
                        tvOverview.text = data.nugget?.overview

                        if (!data.nugget?.deepDives.isNullOrEmpty()) {
                            adapter.setData(data.nugget.deepDives)
                        }

                        if (!data.nugget?.bookRecommendation.isNullOrEmpty()) {
                            val books =
                                data.nugget.bookRecommendation.filter { it.bookRecommendation != null }
                                    .map { it.bookRecommendation!! }

                            bookAdapter.setData(books.map { it.copy(isCart = false) })
                        }

                        tvCategory.text = data.nugget?.category?.tagName
                        tvPrimaryTag.text = data.nugget?.primaryTag?.tagName
                        tvLearning.text = data.nugget?.learningLevel


                    }

                }

                Status.ERROR -> {
                }

                Status.EXCEPTION -> {
                }

                Status.LOADING -> {
                    binding.viewAnimator.displayedChild = 0
                }
            }
        }

        viewModel.likeBookData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.data?.book?.let { isLiked -> bookAdapter.updateLikeStatus(isLiked) }


                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
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
                    it.data?.data?.id?.let { isLiked ->
                        bookAdapter.updateAddToCartStatus(isLiked)
                    }


                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
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