package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeOptimizeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeOptimizeFragment : BaseFragment(R.layout.fragment_decode_optimize) {

    private lateinit var binding: FragmentDecodeOptimizeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeOptimizeBinding.bind(view)

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
