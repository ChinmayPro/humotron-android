package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDecodeQuestionsBinding
import com.humotron.app.domain.modal.response.FeltOffQuestionData
import com.humotron.app.ui.decode.adapter.DecodeQuestionAdapter
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

@AndroidEntryPoint
class DecodeQuestionsFragment : BaseFragment(R.layout.fragment_decode_questions) {

    private lateinit var binding: FragmentDecodeQuestionsBinding
    private val viewModel: DecodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeQuestionsBinding.bind(view)

        initViews()
        initClicks()
        initObservers()

        fetchQuestions()
    }

    private fun fetchQuestions() {
        val type = arguments?.getString("type")
        if (type == "FELT_OFF") {
            viewModel.getFeltOffQuestions()
        } else {
            viewModel.getNutritionIdeaQuestions()
        }
    }

    private fun initViews() {
        binding.tvTitle.text = arguments?.getString("title")
        val type = arguments?.getString("type")
        binding.header.title.text = if (type == "FELT_OFF") "Felt Off" else "Nutrition Ideas"
        binding.rvQuestions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initObservers() {
        viewModel.feltOffQuestionsData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showShimmer(true)
                }
                Status.SUCCESS -> {
                    showShimmer(false)
                    resource.data?.data?.questions?.let { list ->
                        setupRecyclerView(list)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    showShimmer(false)
                }
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        binding.shimmerView.isVisible = show
        if (show) binding.shimmerView.startShimmer() else binding.shimmerView.stopShimmer()
        binding.rvQuestions.isVisible = !show
    }

    private fun setupRecyclerView(items: List<FeltOffQuestionData>) {
        binding.rvQuestions.adapter = DecodeQuestionAdapter(items) { question ->
            // Navigate directly to TronChat instead of popping back
            // This preserves backstack: Decode → Questions → TronChat
            val bundle = Bundle().apply {
                putString("chat_prompt_id", question.id ?: "")
                putString("chat_prompt_title", question.question ?: "")
            }
            findNavController().navigate(R.id.fragmentTronChat, bundle)
        }
        DecodeAnimationUtils.animateCardsIn(binding.rvQuestions)
    }
}
