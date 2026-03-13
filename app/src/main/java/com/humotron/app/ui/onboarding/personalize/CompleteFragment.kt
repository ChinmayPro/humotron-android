package com.humotron.app.ui.onboarding.personalize

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentCompleteBinding
import com.humotron.app.domain.modal.param.CompleteOnboardingParam
import com.humotron.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CompleteFragment : BaseFragment(R.layout.fragment_complete) {

    private lateinit var binding: FragmentCompleteBinding
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCompleteBinding.bind(view)

        binding.btnSubmit.setOnClickListener {
            viewModel.completeOnboarding(CompleteOnboardingParam(true))
        }

        subscriberToObserver()
    }

    private fun subscriberToObserver() {
        viewModel.onBoardingData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data ?: return@observe
                    data.data?.user?.let { prefUtils.setLoginResponse(it) }
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finishAffinity()


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