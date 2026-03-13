package com.humotron.app.ui.onboarding.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.humotron.app.R
import com.humotron.app.data.helperModal.OnboardingItem
import com.humotron.app.databinding.FragmentOnBoardBinding
import com.humotron.app.ui.onboarding.adapter.OnboardingAdapter


class OnBoardFragment : Fragment(R.layout.fragment_on_board) {

    private lateinit var binding: FragmentOnBoardBinding

    val onBoardingItemList = listOf(
        OnboardingItem(R.string.onboard_title_1, R.string.onboard_desc_1),
        OnboardingItem(R.string.onboard_title_2, R.string.onboard_desc_2),
        OnboardingItem(R.string.onboard_title_3, R.string.onboard_desc_3)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnBoardBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        setupViewPager()


    }

    private fun setupViewPager() {
        binding.onBoardViewPager.adapter = OnboardingAdapter(requireContext(), onBoardingItemList)
        binding.onBoardViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tabLayoutIndicator, binding.onBoardViewPager) { _, _ -> }.attach()

        binding.btnNext.setOnClickListener {
            if (binding.onBoardViewPager.currentItem < 2) {
                binding.onBoardViewPager.currentItem += 1
            } else {
                findNavController().navigate(R.id.onBoardPrivacyFragment)
            }
        }

        binding.tvSkip.setOnClickListener {
            findNavController().navigate(R.id.onBoardPrivacyFragment)
        }
    }


}