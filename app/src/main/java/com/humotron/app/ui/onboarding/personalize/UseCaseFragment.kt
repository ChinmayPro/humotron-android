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

        binding.tvTitle.text = getString(R.string.thanks_s, prefUtils.getLoginResponse().let {
            it.firstName ?: it.name?.split(" ")?.firstOrNull() ?: ""
        })

        binding.btnSubmit.setOnClickListener {
            pagerViewModel.moveToPage(3)
        }


        viewModel.getInterests()
        subscribeToObserver()
    }

    private fun subscribeToObserver() {
        viewModel.useCaseData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data ?: return@observe
                    val adapter = UseCaseAdapter()
                    binding.rvUseCase.adapter = adapter
                    binding.rvUseCase.layoutManager = LinearLayoutManager(requireContext())
                    data.data?.interests?.let { adapter.setList(it) }
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

}