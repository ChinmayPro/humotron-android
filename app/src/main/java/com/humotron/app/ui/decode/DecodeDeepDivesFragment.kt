package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeDeepDivesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeDeepDivesFragment : BaseFragment(R.layout.fragment_decode_deep_dives) {

    private lateinit var binding: FragmentDecodeDeepDivesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeDeepDivesBinding.bind(view)

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
