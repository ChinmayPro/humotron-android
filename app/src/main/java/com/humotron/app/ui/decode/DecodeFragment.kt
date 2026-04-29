package com.humotron.app.ui.decode

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeFragment : BaseFragment(R.layout.fragment_decode) {

    private lateinit var binding: FragmentDecodeBinding

    companion object {
        var selectedTabPosition = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeBinding.bind(view)

        initClicks()
        initViews()
        initObservers()
    }

    private fun initViews() {
        // Restore selection
        selectTab(selectedTabPosition)
    }

    private fun initClicks() {
        binding.tabInsights.setOnClickListener { selectTab(0) }
        binding.tabDeepDives.setOnClickListener { selectTab(1) }
        binding.tabOptimize.setOnClickListener { selectTab(2) }
        binding.tabChat.setOnClickListener { selectTab(3) }
    }

    private fun initObservers() {
    }

    fun selectTab(position: Int) {
        // Reset all tabs
        val inactiveColor =
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white30)
        val activeColor =
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorBgBtn)

        binding.ivInsights.imageTintList = ColorStateList.valueOf(inactiveColor)
        binding.tvInsights.setTextColor(inactiveColor)
        binding.ivDeepDives.imageTintList = ColorStateList.valueOf(inactiveColor)
        binding.tvDeepDives.setTextColor(inactiveColor)
        binding.ivOptimize.imageTintList = ColorStateList.valueOf(inactiveColor)
        binding.tvOptimize.setTextColor(inactiveColor)
        binding.ivChat.imageTintList = ColorStateList.valueOf(inactiveColor)
        binding.tvChat.setTextColor(inactiveColor)

        val fragment = when (position) {
            0 -> {
                binding.tvInsightsTitle.text = getString(R.string.insights)
                binding.ivInsights.imageTintList = ColorStateList.valueOf(activeColor)
                binding.tvInsights.setTextColor(activeColor)
                DecodeInsightsFragment()
            }

            1 -> {
                binding.tvInsightsTitle.text = getString(R.string.deep_dives)
                binding.ivDeepDives.imageTintList = ColorStateList.valueOf(activeColor)
                binding.tvDeepDives.setTextColor(activeColor)
                DecodeDeepDivesFragment()
            }

            2 -> {
                binding.tvInsightsTitle.text = getString(R.string.optimize)
                binding.ivOptimize.imageTintList = ColorStateList.valueOf(activeColor)
                binding.tvOptimize.setTextColor(activeColor)
                DecodeOptimizeFragment()
            }

            3 -> {
                binding.tvInsightsTitle.text = getString(R.string.chat)
                binding.ivChat.imageTintList = ColorStateList.valueOf(activeColor)
                binding.tvChat.setTextColor(activeColor)
                DecodeChatFragment()
            }

            else -> DecodeInsightsFragment()
        }

        selectedTabPosition = position
        loadFragment(fragment)
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.contentContainer, fragment)
            .commit()
    }
}
