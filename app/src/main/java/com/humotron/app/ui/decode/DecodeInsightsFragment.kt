package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeInsightsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeInsightsFragment : BaseFragment(R.layout.fragment_decode_insights) {

    private lateinit var binding: FragmentDecodeInsightsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeInsightsBinding.bind(view)

        initClicks()
        initViews()
        initObservers()
    }

    private fun initViews() {
    }

    private fun initClicks() {
    }

    private fun initObservers() {
    }
}
