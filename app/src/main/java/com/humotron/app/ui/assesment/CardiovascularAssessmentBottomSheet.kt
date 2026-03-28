package com.humotron.app.ui.assesment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.humotron.app.R
import com.humotron.app.core.AppConstant.ASSESSMENT
import com.humotron.app.databinding.BottomSheetCardiovascularAssessmentBinding
import com.humotron.app.domain.modal.response.MergedAssessment

class CardiovascularAssessmentBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCardiovascularAssessmentBinding? = null
    private val binding get() = _binding!!
    private var assessmentJson: String? = null


    var onProceedClicked: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        assessmentJson = arguments?.getString(ASSESSMENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCardiovascularAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.skipCollapsed = true
        behavior.isDraggable = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // Wait for the view to be fully measured, THEN animate
        bottomSheet.post {
            val screenHeight = bottomSheet.rootView.height.toFloat()

            // Push fully off-screen using actual screen height
            bottomSheet.translationY = screenHeight

            // Now animate up with iOS spring feel
            bottomSheet.animate()
                .translationY(0.5f)
                .setDuration(700)
                .setInterpolator(OvershootInterpolator(0.8f))
                .start()
        }
    }

    private fun dismissWithAnimation() {
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )

        bottomSheet?.animate()
            ?.translationY(bottomSheet.rootView.height.toFloat())
            ?.setDuration(300)
            ?.setInterpolator(DecelerateInterpolator(1.8f))
            ?.withEndAction { dismiss() }
            ?.start()
            ?: dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val assessment = Gson().fromJson(assessmentJson, MergedAssessment::class.java)
        Log.e("TAG", "bindwsddddsdddd22:  ${assessment}", )

        binding.btnProceed.setOnClickListener {
            dismissWithAnimation()
            view.postDelayed({ onProceedClicked?.invoke() }, 280)
        }

        binding.tvTitle.text = assessment.assessmentName
        binding.tvTotalQuestions.text = assessment.totalQuestions.toString()
        binding.tvDuration.text = assessment.assessmentDetails?.assessmentDuration?:""
        binding.tvUnderstanding.text = assessment.assessmentDetails?.assessmentIntro?:""
        binding.tvAssessmentTrigger.text = assessment.assessmentDetails?.alertCriteria?:""
        binding.tvWhatExplore.text = assessment.assessmentDetails?.assessmentWhat?:""
        binding.whyThisMatter.text = assessment.assessmentDetails?.assessmentWhy?:""
        binding.tvSuggestedSteps.text = assessment.assessmentDetails?.assessmentNextSteps?:""
        binding.btnClose.setOnClickListener {
            dismissWithAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    companion object {
        const val TAG = "CardiovascularAssessmentBottomSheet"

        fun newInstance(assessmentJson: String): CardiovascularAssessmentBottomSheet {
            val fragment = CardiovascularAssessmentBottomSheet()
            val bundle = Bundle()
            bundle.putString(ASSESSMENT, assessmentJson)
            fragment.arguments = bundle
            return fragment
        }
    }

}