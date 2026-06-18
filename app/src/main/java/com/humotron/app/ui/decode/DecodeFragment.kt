package com.humotron.app.ui.decode

import android.graphics.Color
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
        val inactiveColor = Color.parseColor("#8a9794")
        val activeColor = Color.WHITE

        // Reset all tabs: text color inactive, hide indicators
        binding.tvInsights.setTextColor(inactiveColor)
        binding.indicatorInsights.visibility = View.INVISIBLE
        binding.tvDeepDives.setTextColor(inactiveColor)
        binding.indicatorDeepDives.visibility = View.INVISIBLE
        binding.tvOptimize.setTextColor(inactiveColor)
        binding.indicatorOptimize.visibility = View.INVISIBLE
        binding.tvChat.setTextColor(inactiveColor)
        binding.indicatorChat.visibility = View.INVISIBLE

        val fragment = when (position) {
            0 -> {
                binding.tvInsights.setTextColor(activeColor)
                binding.indicatorInsights.visibility = View.VISIBLE
                DecodeInsightsFragment()
            }

            1 -> {
                binding.tvDeepDives.setTextColor(activeColor)
                binding.indicatorDeepDives.visibility = View.VISIBLE
                DecodeDeepDivesFragment()
            }

            2 -> {
                binding.tvOptimize.setTextColor(activeColor)
                binding.indicatorOptimize.visibility = View.VISIBLE
                DecodeOptimizeFragment()
            }

            3 -> {
                binding.tvChat.setTextColor(activeColor)
                binding.indicatorChat.visibility = View.VISIBLE
                DecodeChatFragment()
            }

            else -> DecodeInsightsFragment()
        }

        selectedTabPosition = position
        loadFragment(fragment)
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        val existing = childFragmentManager.findFragmentById(R.id.contentContainer)
        if (existing != null && existing::class == fragment::class) {
            // Same fragment type already shown — don't replace, let it restore naturally
            return
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.contentContainer, fragment)
            .commit()
    }
}
