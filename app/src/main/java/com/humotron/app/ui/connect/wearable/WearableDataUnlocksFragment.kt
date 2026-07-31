package com.humotron.app.ui.connect.wearable

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.humotron.app.R
import com.humotron.app.databinding.FragmentWearableDataUnlocksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WearableDataUnlocksFragment : Fragment(R.layout.fragment_wearable_data_unlocks) {

    private lateinit var binding: FragmentWearableDataUnlocksBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWearableDataUnlocksBinding.bind(view)
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
        binding.header.tvTitle.text = getString(R.string.what_ur_data_unlocks)
    }

    private fun initClicks() {
        binding.btnChooseDevice.setOnClickListener {

        }
    }

    private fun observeViewModel() {

    }

    companion object {

    }
}