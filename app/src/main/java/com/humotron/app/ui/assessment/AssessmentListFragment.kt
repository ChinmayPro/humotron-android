package com.humotron.app.ui.assessment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentAssessmentListBinding
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.ui.navigation.NavKeys.ASSESSMENT
import com.humotron.app.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentListFragment : Fragment(R.layout.fragment_assessment_list) {

    private lateinit var binding: FragmentAssessmentListBinding
    private val viewModel: AssessmentViewModel by viewModels()
    private var adapter: AssessmentAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAssessmentListBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        setupNudgeText()
        observeViewModel()

        viewModel.getMergedAssessmentList()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.assessments_title)
        adapter = AssessmentAdapter(requireContext(), emptyList()) { assessment ->
            handleAssessmentClick(assessment)
        }
        binding.rvAssessments.adapter = adapter
    }

    private fun setupNudgeText() {
        val fullText = getString(R.string.nudge_deepscan_full)
        val boldPart = "Want hard numbers too?"
        val spannable = SpannableString(fullText)

        val boldStart = fullText.indexOf(boldPart)
        if (boldStart != -1) {
            val boldEnd = boldStart + boldPart.length

            // Bold and White for "Want hard numbers too?"
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                boldStart,
                boldEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white)),
                boldStart,
                boldEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Normal and Ink2 for the rest
            if (boldEnd < fullText.length) {
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.ink2)),
                    boldEnd,
                    fullText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        binding.tvNudgeText.text = spannable
    }

    private fun handleAssessmentClick(assessment: MergedAssessment) {
        when (assessment.status) {
            "Completed" -> {
                /*findNavController().navigate(
                    R.id.action_fragmentAssessmentList_to_fragmentReviewEditAnswers,
                    Bundle().apply {
                        putParcelable(NavKeys.ASSESSMENT, assessment)
                    }
                )*/
                toast("The assessment is complete.")
            }

            "Resume" -> {
                findNavController().navigate(
                    R.id.action_fragmentAssessmentList_to_assessmentFragment,
                    Bundle().apply {
                        putParcelable(NavKeys.ASSESSMENT, assessment)
                    }
                )
            }

            "Start Now" -> {
                //showAssessmentSheet(assessment)
                findNavController().navigate(
                    R.id.action_fragmentAssessmentList_to_fragmentAssessmentInstruction,
                    Bundle().apply {
                        putParcelable(NavKeys.ASSESSMENT, assessment)
                    }
                )
            }
        }
    }

    private fun showAssessmentSheet(assessment: MergedAssessment) {
        val json = Gson().toJson(assessment)
        val sheet = CardiovascularAssessmentBottomSheet.newInstance(json)
        sheet.onProceedClicked = {
            if (isAdded) {
                val intent = Intent(requireContext(), AssessmentActivity::class.java)
                intent.putExtra(ASSESSMENT, json)
                startActivity(intent)
            }
        }
        sheet.show(parentFragmentManager, CardiovascularAssessmentBottomSheet.TAG)
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.mergedAssessmentListLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> { /* show progress if needed */
                }

                Status.SUCCESS -> {
                    val list = resource.data?.data ?: emptyList()
                    adapter?.updateData(list)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    toast(resource.error?.errorMessage ?: "Failed to load assessments")
                }
            }
        }
    }
}
