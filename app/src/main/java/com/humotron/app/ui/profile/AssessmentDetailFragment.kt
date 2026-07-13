package com.humotron.app.ui.profile

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.humotron.app.R
import com.humotron.app.core.AppConstant.ASSESSMENT
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentAssessmentDetailBinding
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.ui.assesment.AssessmentActivity
import com.humotron.app.ui.assesment.CardiovascularAssessmentBottomSheet
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentDetailFragment : BaseFragment(R.layout.fragment_assessment_detail) {

    private lateinit var binding: FragmentAssessmentDetailBinding
    private val args: AssessmentDetailFragmentArgs by navArgs()
    private val deviceViewModel: DeviceViewModel by activityViewModels()

    // Questions dictionary matching HTML ASSESS_Q
    private val assessQuestions = mapOf(
        "lifestyle" to listOf("Alcohol intake", "Smoking status", "Caffeine habits", "Sleep schedule"),
        "health_history" to listOf("Family history", "Current medications", "Past conditions"),
        "goals_symptoms" to listOf("Primary goal", "Current symptoms", "Stress level")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAssessmentDetailBinding.bind(view)

        // Setup Header
        binding.header.title.text = args.assessmentName
        binding.header.title.typeface = resources.getFont(R.font.manrope_bold)
        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Source Info Card
        binding.tvDeviceName.text = args.assessmentName
        binding.tvDeviceDesc.text = args.assessmentDesc
        binding.ivDeviceIcon.setImageResource(args.assessmentIcon)

        // Set colors and opacity tint for icon box
        val colorHex = args.assessmentColor
        try {
            val colorInt = Color.parseColor(colorHex)
            binding.ivDeviceIcon.imageTintList = ColorStateList.valueOf(colorInt)
            val bgTint = Color.argb(
                (255 * 0.22).toInt(),
                Color.red(colorInt),
                Color.green(colorInt),
                Color.blue(colorInt)
            )
            binding.llDeviceIcon.backgroundTintList = ColorStateList.valueOf(bgTint)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Setup Status Badge Pill
        binding.tvStatus.text = args.assessmentDate

        // Load and populate individual questions
        populateQuestions()

        // Setup Edit answers click
        binding.llEditAnswers.setOnClickListener {
            // Find corresponding MergedAssessment object from LiveData list
            val mergedAssessmentList = deviceViewModel.mergedAssessmentListLiveData.value?.data?.data
            val matchedAssessment = mergedAssessmentList?.find {
                it.assessmentName.equals(args.assessmentName, ignoreCase = true) ||
                it.assessmentId.equals(args.assessmentId, ignoreCase = true)
            }

            if (matchedAssessment != null) {
                val json = Gson().toJson(matchedAssessment)
                when (matchedAssessment.status) {
                    "Start Now" -> {
                        val sheet = CardiovascularAssessmentBottomSheet.newInstance(json)
                        sheet.onProceedClicked = {
                            if (isAdded) {
                                val intent = Intent(requireContext(), AssessmentActivity::class.java).apply {
                                    putExtra(ASSESSMENT, json)
                                }
                                startActivity(intent)
                            }
                        }
                        sheet.show(parentFragmentManager, CardiovascularAssessmentBottomSheet.TAG)
                    }
                    else -> {
                        val intent = Intent(requireContext(), AssessmentActivity::class.java).apply {
                            putExtra(ASSESSMENT, json)
                        }
                        startActivity(intent)
                    }
                }
            } else {
                // If not found in API list, show placeholder toast
                ToastUtils.showShort(requireContext(), "Launch questionnaire for ${args.assessmentName}")
            }
        }

        // Setup Pause / Delete buttons
        binding.llPauseAssessment.setOnClickListener {
            ToastUtils.showShort(requireContext(), "Paused ${args.assessmentName}")
        }

        binding.llDeleteAssessment.setOnClickListener {
            ToastUtils.showShort(requireContext(), "Deleted all answers for ${args.assessmentName}")
        }
    }

    private fun populateQuestions() {
        binding.llQuestionsContainer.removeAllViews()
        val questions = assessQuestions[args.assessmentId] ?: return

        val density = resources.displayMetrics.density
        val padding15 = (15 * density).toInt()

        questions.forEachIndexed { index, question ->
            // Question Row Container
            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(padding15, padding15, padding15, padding15)
            }

            // Question Label Text
            val textView = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                text = question
                setTextColor(Color.WHITE)
                textSize = 15.5f
                typeface = resources.getFont(R.font.manrope_bold)
                // Set textStyle to bold for reliable rendering
                setTypeface(this.typeface, android.graphics.Typeface.BOLD)
            }
            row.addView(textView)

            // Switch
            val switchCompat = SwitchCompat(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isChecked = true
                thumbTintList = ColorStateList.valueOf(Color.WHITE)
                trackTintList = resources.getColorStateList(R.color.switch_track_color, requireContext().theme)
            }
            row.addView(switchCompat)

            binding.llQuestionsContainer.addView(row)

            // Divider between questions (except the last one)
            if (index < questions.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (1 * density).toInt()
                    )
                    setBackgroundColor(Color.parseColor("#12FFFFFF"))
                }
                binding.llQuestionsContainer.addView(divider)
            }
        }
    }
}
