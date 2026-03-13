package com.humotron.app.ui.bioHack

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.humotron.app.R
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBookSummaryBinding
import com.humotron.app.databinding.ItemKeyTakeawayBinding
import com.humotron.app.databinding.ItemUserReviewBinding
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import com.humotron.app.ui.common.CommonAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookSummaryFragment : BaseBottomSheetDialogFragment(R.layout.fragment_book_summary) {


    private lateinit var binding: FragmentBookSummaryBinding
    private val viewModel: NuggetsViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookSummaryBinding.bind(view)
        val bookId = arguments?.getString("bookId")

        bookId?.let { viewModel.getBookSummary(it) }
        
        binding.ivShrink.setOnClickListener { 
            dismiss()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.getBookSummaryData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data?.data ?: return@observe
                    binding.apply {
                        tvBookTitle.text = data.book?.bookTitle
                        tvAuthor.text = data.book?.author1
                        tvShortDesc.text = data.book?.shortDesc
                        tvLongDesc.text = data.book?.longDesc
                        tvEndorsed.text = data.book?.endorseBy

                        if (!data.book?.keyTakeaways.isNullOrEmpty()) {
                            val keyTakeAdapter = CommonAdapter<String>(
                                requireContext(),
                                { li, parent, attach ->
                                    ItemKeyTakeawayBinding.inflate(li, parent, attach)
                                },
                                { m, vb, pos ->
                                    val binding = vb as ItemKeyTakeawayBinding
                                    binding.bullet1.text = m
                                }
                            )

                            rvKeyTakeAway.adapter = keyTakeAdapter
                            keyTakeAdapter.setList(data.book.keyTakeaways)
                        }

                        if (!data.book?.userReview.isNullOrEmpty()) {
                            val userFeedback = CommonAdapter<String>(
                                requireContext(),
                                { li, parent, attach ->
                                    ItemUserReviewBinding.inflate(li, parent, attach)
                                },
                                { m, vb, pos ->
                                    val binding = vb as ItemUserReviewBinding
                                    binding.bullet1.text = m
                                }
                            )

                            rvCrispSummary.adapter = userFeedback
                            userFeedback.setList(data.book.userReview)
                        }

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                // Make it open full but with a top margin
                behavior.isFitToContents = false
                behavior.expandedOffset = dpToPx(60) // 60dp top margin
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                // Set the height of the bottom sheet to MATCH_PARENT
                val layoutParams = it.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.layoutParams = layoutParams

                // Fix for NestedScrollView inside
                behavior.isDraggable = true
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun getTheme(): Int =
        R.style.TransparentBottomSheetDialogTheme



}