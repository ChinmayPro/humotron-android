package com.humotron.app.ui.decode

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
        val inactiveColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white30)
        val activeColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorBgBtn)

        binding.ivInsights.setColorFilter(inactiveColor)
        binding.tvInsights.setTextColor(inactiveColor)
        binding.ivDeepDives.setColorFilter(inactiveColor)
        binding.tvDeepDives.setTextColor(inactiveColor)
        binding.ivOptimize.setColorFilter(inactiveColor)
        binding.tvOptimize.setTextColor(inactiveColor)
        binding.ivChat.setColorFilter(inactiveColor)
        binding.tvChat.setTextColor(inactiveColor)

        val fragment = when (position) {
            0 -> {
                binding.tvInsightsTitle.text = getString(R.string.insights)
                binding.ivInsights.setColorFilter(activeColor)
                binding.tvInsights.setTextColor(activeColor)
                DecodeInsightsFragment()
            }
            1 -> {
                binding.tvInsightsTitle.text = getString(R.string.deep_dives)
                binding.ivDeepDives.setColorFilter(activeColor)
                binding.tvDeepDives.setTextColor(activeColor)
                DecodeDeepDivesFragment()
            }
            2 -> {
                binding.tvInsightsTitle.text = getString(R.string.optimize)
                binding.ivOptimize.setColorFilter(activeColor)
                binding.tvOptimize.setTextColor(activeColor)
                DecodeOptimizeFragment()
            }
            3 -> {
                binding.tvInsightsTitle.text = getString(R.string.chat)
                binding.ivChat.setColorFilter(activeColor)
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
