package com.humotron.app.ui.connect.wearable

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentConnectWearableSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectWearableSuccessFragment : Fragment(R.layout.fragment_connect_wearable_success) {

    private lateinit var binding: FragmentConnectWearableSuccessBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConnectWearableSuccessBinding.bind(view)
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
        binding.btnSeeItInTrack.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentTrack, false)
        }
    }

    private fun observeViewModel() {

    }

    companion object {

    }
}