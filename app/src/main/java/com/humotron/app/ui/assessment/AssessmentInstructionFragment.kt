package com.humotron.app.ui.assessment

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.databinding.FragmentAssessmentInstructionBinding
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentInstructionFragment : Fragment(R.layout.fragment_assessment_instruction) {

    private lateinit var binding: FragmentAssessmentInstructionBinding
    private val args: AssessmentInstructionFragmentArgs by navArgs()
    private var assessment: MergedAssessment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAssessmentInstructionBinding.bind(view)
        assessment = args.assessment

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
        binding.header.tvTitle.text = getString(R.string.assessments_title)
        assessment?.let {
            binding.tvHeadline.text = it.assessmentName
            binding.tvTrigger.text = it.assessmentDetails?.alertCriteria
            binding.tvUnderstandingDesc.text = it.assessmentDetails?.assessmentIntro
            binding.tvWhyDesc.text = it.assessmentDetails?.assessmentWhy
            binding.tvExploreDesc.text = it.assessmentDetails?.assessmentWhat
        }
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnProceed.setOnClickListener {
            findNavController().navigate(
                R.id.action_fragmentAssessmentInstruction_to_assessmentFragment,
                Bundle().apply {
                    putParcelable(NavKeys.ASSESSMENT, assessment)
                }
            )
        }
    }

    private fun observeViewModel() {

    }

    companion object {

    }
}