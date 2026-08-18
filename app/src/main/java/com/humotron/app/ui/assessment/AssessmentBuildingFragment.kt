package com.humotron.app.ui.assessment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentAssessmentBuildingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentBuildingFragment : Fragment(R.layout.fragment_assessment_building) {

    private lateinit var binding: FragmentAssessmentBuildingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAssessmentBuildingBinding.bind(view)
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
        binding.tvSubTitle.setOnAllShownListener(1000L) {
            findNavController().popBackStack(R.id.fragmentTrack, false)
        }
    }

    private fun initClicks() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing → Back is disabled for this Fragment
                }
            }
        )
    }

    private fun observeViewModel() {

    }

    companion object {

    }
}