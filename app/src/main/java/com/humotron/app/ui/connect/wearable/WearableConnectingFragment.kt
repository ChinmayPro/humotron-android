package com.humotron.app.ui.connect.wearable

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.humotron.app.R
import com.humotron.app.databinding.FragmentWearableConnectingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WearableConnectingFragment : Fragment(R.layout.fragment_wearable_connecting) {

    private lateinit var binding: FragmentWearableConnectingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWearableConnectingBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()
    }

    private fun initViews() {
    }

    private fun initClicks() {
    }

    private fun observeViewModel() {

    }

    companion object {

    }
}