package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
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
        binding.cardWorkdayStress.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentDecode_to_fragmentWorkdayReport)
        }

        binding.cardWeatherResilience.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentDecode_to_fragmentWeatherOverview)
        }
    }

    private fun initObservers() {
    }
}
