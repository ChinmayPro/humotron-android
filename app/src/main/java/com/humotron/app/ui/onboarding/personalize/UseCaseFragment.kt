package com.humotron.app.ui.onboarding.personalize

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentUseCaseBinding
import com.humotron.app.ui.onboarding.personalize.adapter.UseCaseAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UseCaseFragment : BaseFragment(R.layout.fragment_use_case) {


    private lateinit var binding: FragmentUseCaseBinding
    private val viewModel: OnboardingViewModel by viewModels()
    private val pagerViewModel: PagerViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUseCaseBinding.bind(view)

        binding.btnSubmit.setOnClickListener {
            pagerViewModel.moveToPage(3)
        }

        val interests = listOf(
            com.humotron.app.domain.modal.response.UseCaseInterest(
                id = "1",
                interestCount = 0,
                interestQuestionId = emptyList(),
                title = getString(R.string.goal_option_1_title),
                subtitle = getString(R.string.goal_option_1_desc),
                isChecked = true
            ),
            com.humotron.app.domain.modal.response.UseCaseInterest(
                id = "2",
                interestCount = 0,
                interestQuestionId = emptyList(),
                title = getString(R.string.goal_option_2_title),
                subtitle = getString(R.string.goal_option_2_desc),
                isChecked = false
            ),
            com.humotron.app.domain.modal.response.UseCaseInterest(
                id = "3",
                interestCount = 0,
                interestQuestionId = emptyList(),
                title = getString(R.string.goal_option_3_title),
                subtitle = getString(R.string.goal_option_3_desc),
                isChecked = false
            ),
            com.humotron.app.domain.modal.response.UseCaseInterest(
                id = "4",
                interestCount = 0,
                interestQuestionId = emptyList(),
                title = getString(R.string.goal_option_4_title),
                subtitle = getString(R.string.goal_option_4_desc),
                isChecked = false
            )
        )

        val adapter = UseCaseAdapter { selectedOption ->
            updateFooterText(selectedOption.title)
        }
        binding.rvUseCase.adapter = adapter
        binding.rvUseCase.layoutManager = LinearLayoutManager(requireContext())
        adapter.setList(interests)

        // Initialize footer state for the default selection
        updateFooterText(interests[0].title)
    }

    private fun updateFooterText(title: String?) {
        if (title?.contains("sense of my data", ignoreCase = true) == true) {
            binding.tvFooter.text = androidx.core.text.HtmlCompat.fromHtml(
                getString(R.string.goal_footer_clarity),
                androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        } else {
            binding.tvFooter.text = getString(R.string.goal_footer_default)
        }
    }
}