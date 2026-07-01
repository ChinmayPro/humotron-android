package com.humotron.app.ui.onboarding.personalize

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentStartAreasBinding
import com.humotron.app.ui.onboarding.personalize.adapter.StartAreaItem
import com.humotron.app.ui.onboarding.personalize.adapter.StartAreasAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartAreasFragment : BaseFragment(R.layout.fragment_start_areas) {

    private lateinit var binding: FragmentStartAreasBinding
    private val pagerViewModel: PagerViewModel by activityViewModels()
    private lateinit var adapter: StartAreasAdapter
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStartAreasBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        subscriberToObserver()
    }

    private fun setupRecyclerView() {
        val items = listOf(
            StartAreaItem(
                id = "heart",
                title = "Heart",
                iconResId = R.drawable.ic_start_heart,
                isSelected = true,
                description = "Tracks heart rate, resting heart rate, and HRV."
            ),
            StartAreaItem(
                id = "sleep",
                title = "Sleep",
                iconResId = R.drawable.ic_start_sleep,
                isSelected = true,
                description = "Tracks sleep stages, duration, and sleep efficiency."
            ),
            StartAreaItem(
                id = "stress",
                title = "Stress",
                iconResId = R.drawable.ic_start_stress,
                isSelected = true,
                description = "Tracks stress levels, workday load, and daily peaks."
            ),
            StartAreaItem(
                id = "recovery",
                title = "Recovery",
                iconResId = R.drawable.ic_start_recovery,
                isSelected = false,
                description = "Tracks training response, HRV recovery, and biometric reset."
            ),
            StartAreaItem(
                id = "energy",
                title = "Energy",
                iconResId = R.drawable.ic_start_energy,
                isSelected = false,
                description = "Tracks energy patterns, biometrics, and activity burn."
            ),
            StartAreaItem(
                id = "focus",
                title = "Focus",
                iconResId = R.drawable.ic_start_focus,
                isSelected = false,
                description = "Tracks daily focus level, mental engagement, and attention window."
            )
        )

        adapter = StartAreasAdapter()
        binding.rvStartAreas.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvStartAreas.adapter = adapter
        adapter.setList(items)
    }

    private fun setupListeners() {
        binding.btnSubmit.setOnClickListener {
            viewModel.completeOnboarding(com.humotron.app.domain.modal.param.CompleteOnboardingParam(true))
        }
    }

    private fun subscriberToObserver() {
        viewModel.onBoardingData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                com.humotron.app.data.network.Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data ?: return@observe
                    data.data?.user?.let { prefUtils.setLoginResponse(it) }
                    startActivity(android.content.Intent(requireContext(), com.humotron.app.ui.MainActivity::class.java))
                    requireActivity().finish()
                }

                com.humotron.app.data.network.Status.ERROR -> {
                    hideProgress()
                }

                com.humotron.app.data.network.Status.EXCEPTION -> {
                    hideProgress()
                }

                com.humotron.app.data.network.Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }
}
