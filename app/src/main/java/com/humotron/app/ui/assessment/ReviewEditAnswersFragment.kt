package com.humotron.app.ui.assessment

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.databinding.FragmentReviewEditAnswersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewEditAnswersFragment : Fragment(R.layout.fragment_review_edit_answers) {

    private lateinit var binding: FragmentReviewEditAnswersBinding
    private val args: ReviewEditAnswersFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReviewEditAnswersBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.review_answers)
    }

    private fun initClicks() {
        binding.btnFinishGetGuidance.setOnClickListener {

        }
    }

    private fun observeViewModel() {

    }

    companion object {

    }
}