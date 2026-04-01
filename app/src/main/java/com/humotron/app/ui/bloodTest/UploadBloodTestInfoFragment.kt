package com.humotron.app.ui.bloodTest

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentUploadBloodtestInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadBloodTestInfoFragment : BaseFragment(R.layout.fragment_upload_bloodtest_info) {

    private lateinit var binding: FragmentUploadBloodtestInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar()
        binding = FragmentUploadBloodtestInfoBinding.bind(view)
        applyInsets()
        initClicks()
    }

    private fun setupStatusBar() {
        requireActivity().window.apply {
            statusBarColor = Color.BLACK
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = false
        }
    }

    private fun initClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnContinue.setOnClickListener {
            // TODO: Navigate to actual upload flow when implemented
        }

        binding.tvFooter.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentUploadBloodTestInfo_to_fragmentBloodTestInfo)
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
