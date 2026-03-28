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
        binding.rvQuestions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initClicks() {

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
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_question", question)
            findNavController().popBackStack()
        }
    }
}
