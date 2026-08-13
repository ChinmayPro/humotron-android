package com.humotron.app.ui.profile

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentAssessmentDetailBinding
import com.humotron.app.ui.assessment.AssessmentActivity
import com.humotron.app.ui.assessment.CardiovascularAssessmentBottomSheet
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Window
import androidx.core.content.ContextCompat
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.DialogDeleteSourceConfirmBinding
import com.humotron.app.databinding.DialogPauseSourceConfirmBinding

@AndroidEntryPoint
class AssessmentDetailFragment : BaseFragment(R.layout.fragment_assessment_detail) {

    private lateinit var binding: FragmentAssessmentDetailBinding
    private val args: AssessmentDetailFragmentArgs by navArgs()
    private val deviceViewModel: DeviceViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var isPausedState = false

    // Questions dictionary matching HTML ASSESS_Q
    private val assessQuestions = mapOf(
        "lifestyle" to listOf(
            "Alcohol intake",
            "Smoking status",
            "Caffeine habits",
            "Sleep schedule"
        ),
        "health_history" to listOf("Family history", "Current medications", "Past conditions"),
        "goals_symptoms" to listOf("Primary goal", "Current symptoms", "Stress level")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAssessmentDetailBinding.bind(view)

        setupInsets()

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

        isPausedState = args.assessmentDate.equals("Paused", ignoreCase = true)
        updatePauseControlUi(isPausedState)

        setupObservers()

        // Fetch detail API (GET /data-sources/{assessmentId})
        profileViewModel.fetchDataSourceDetail(args.assessmentId)

        // Load and populate individual questions
        populateQuestions()

        // Setup Edit answers click
        binding.llEditAnswers.setOnClickListener {
            // Find corresponding MergedAssessment object from LiveData list
            val mergedAssessmentList =
                deviceViewModel.mergedAssessmentListLiveData.value?.data?.data

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
                                val intent = Intent(
                                    requireContext(),
                                    AssessmentActivity::class.java
                                ).apply {
                                    putExtra(NavKeys.ASSESSMENT, json)
                                }

                                startActivity(intent)
                            }
                        }

                        sheet.show(
                            parentFragmentManager,
                            CardiovascularAssessmentBottomSheet.TAG
                        )
                    }

                    else -> {
                        val intent = Intent(
                            requireContext(),
                            AssessmentActivity::class.java
                        ).apply {
                            putExtra(NavKeys.ASSESSMENT, json)
                        }

                        startActivity(intent)
                    }
                }
            } else {
                // If not found in API list, show placeholder toast
                ToastUtils.showShort(
                    requireContext(),
                    "Launch questionnaire for ${args.assessmentName}"
                )
            }
        }
    }

    private fun updatePauseControlUi(isPaused: Boolean) {
        binding.switchInclude.setOnCheckedChangeListener(null)
        if (isPaused) {
            binding.tvStatus.text = "Paused"
            binding.vStatusDot.visibility = View.VISIBLE
            binding.vStatusDot.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#A0B3AF"))
            binding.tvStatus.setTextColor(Color.parseColor("#A0B3AF"))

            binding.switchInclude.isChecked = false

            binding.ivPauseIcon.setImageResource(R.drawable.ic_play)
            binding.ivPauseIcon.imageTintList =
                ColorStateList.valueOf(Color.parseColor("#E7A93C"))
            binding.tvPauseTitle.text = "Resume this assessment"
            binding.tvPauseDesc.text = "Start using ${args.assessmentName} answers again"
        } else {
            val rawStatus = args.assessmentDate
            val statusDisplay = if (rawStatus.isNotBlank()
                && !rawStatus.contains("Paused", ignoreCase = true)
                && !rawStatus.contains("excluded", ignoreCase = true)
                && !rawStatus.contains("scan", ignoreCase = true)
                && !rawStatus.contains("report", ignoreCase = true)
            ) {
                rawStatus
            } else {
                "Connected"
            }
            binding.tvStatus.text = statusDisplay
            binding.vStatusDot.visibility = View.VISIBLE
            binding.vStatusDot.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#C4F23E"))
            binding.tvStatus.setTextColor(Color.parseColor("#cfdad8"))

            binding.switchInclude.isChecked = true

            binding.ivPauseIcon.setImageResource(R.drawable.ic_clock)
            binding.ivPauseIcon.imageTintList =
                ColorStateList.valueOf(Color.parseColor("#E7A93C"))
            binding.tvPauseTitle.text = "Pause this assessment"
            binding.tvPauseDesc.text = "Temporarily exclude all answers"
        }

        binding.switchInclude.setOnCheckedChangeListener { _, isChecked ->
            performIncludeInAnalysisApi(isChecked)
        }
    }

    private var lastTargetPaused: Boolean? = null

    private fun performIncludeInAnalysisApi(includeAnalysis: Boolean) {
        lastTargetPaused = null
        val param = com.humotron.app.domain.modal.param.PauseDataSourceParam(
            includeInAnalysis = includeAnalysis
        )
        profileViewModel.pauseDataSource(args.assessmentId, param)
    }

    private fun showPauseConfirmationDialog(targetPaused: Boolean) {
        val context = context ?: return
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogPauseSourceConfirmBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        if (targetPaused) {
            dialogBinding.tvDialogTitle.text = "Pause this assessment?"
            dialogBinding.tvDialogSubTitle.text =
                "We'll stop using it for analysis until you resume."
            dialogBinding.btnConfirm.text = "Pause"
        } else {
            dialogBinding.tvDialogTitle.text = "Resume this assessment?"
            dialogBinding.tvDialogSubTitle.text = "We'll start using it for analysis again."
            dialogBinding.btnConfirm.text = "Resume"
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            startPauseProcess(targetPaused = targetPaused)
        }

        dialog.show()
    }

    private fun startPauseProcess(targetPaused: Boolean) {
        lastTargetPaused = targetPaused
        binding.tvStepperTitle.text = if (targetPaused) "Pausing" else "Resuming"

        context?.let { ctx ->
            binding.tvStep1Badge.background =
                ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_active)
        }
        binding.tvStep1Badge.setTextColor(Color.parseColor("#111827"))
        binding.tvStep1Text.text = "Updating preference"
        binding.tvStep1Text.setTextColor(Color.parseColor("#FFFFFF"))

        context?.let { ctx ->
            binding.tvStep2Badge.background =
                ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_inactive)
        }
        binding.tvStep2Badge.setTextColor(Color.parseColor("#A0B3AF"))
        binding.tvStep2Text.text = "Confirming"
        binding.tvStep2Text.setTextColor(Color.parseColor("#6F7E7D"))

        binding.llStepperContainer.alpha = 1f
        binding.llStepperContainer.visibility = View.VISIBLE

        binding.llSuccessContainer.visibility = View.GONE
        binding.llSuccessContainer.alpha = 0f
        binding.llSuccessContainer.scaleX = 0.85f
        binding.llSuccessContainer.scaleY = 0.85f

        binding.flPauseProgressOverlay.alpha = 0f
        binding.flPauseProgressOverlay.visibility = View.VISIBLE
        binding.flPauseProgressOverlay.animate()
            .alpha(1f)
            .setDuration(250)
            .start()

        val param = com.humotron.app.domain.modal.param.PauseDataSourceParam(
            isPaused = targetPaused
        )
        profileViewModel.pauseDataSource(args.assessmentId, param)
    }

    private fun handlePauseApiSuccess() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            context?.let { ctx ->
                binding.tvStep2Badge.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_active)
            }
            binding.tvStep2Badge.setTextColor(Color.parseColor("#111827"))
            binding.tvStep2Text.setTextColor(Color.parseColor("#FFFFFF"))

            binding.tvStep2Badge.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(120)
                .withEndAction {
                    binding.tvStep2Badge.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        .start()
                }.start()

            handler.postDelayed({
                binding.llStepperContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.llStepperContainer.visibility = View.GONE

                        binding.tvSuccessTitle.text =
                            if (isPausedState) "Assessment paused" else "Assessment resumed"
                        binding.llSuccessContainer.visibility = View.VISIBLE
                        binding.llSuccessContainer.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start()
                    }.start()

                handler.postDelayed({
                    binding.flPauseProgressOverlay.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction {
                            binding.flPauseProgressOverlay.visibility = View.GONE
                            binding.flPauseProgressOverlay.alpha = 1f
                            updatePauseControlUi(isPausedState)
                            profileViewModel.fetchDataSources()
                        }.start()
                }, 1600)

            }, 800)

        }, 350)
    }

    private fun showDeleteConfirmationDialog() {
        val context = context ?: return
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteSourceConfirmBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogBinding.tvDialogTitle.text = "Delete all data?"
        dialogBinding.tvDialogSubTitle.text =
            "This permanently removes all answers from ${args.assessmentName}. This can't be undone."

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirmDelete.setOnClickListener {
            dialog.dismiss()
            startDeleteProcess()
        }

        dialog.show()
    }

    private fun startDeleteProcess() {
        binding.tvStepperTitle.text = "Deleting"

        context?.let { ctx ->
            binding.tvStep1Badge.background =
                ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_active)
        }
        binding.tvStep1Badge.setTextColor(Color.parseColor("#111827"))
        binding.tvStep1Text.text = "Deleting data"
        binding.tvStep1Text.setTextColor(Color.parseColor("#FFFFFF"))

        context?.let { ctx ->
            binding.tvStep2Badge.background =
                ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_inactive)
        }
        binding.tvStep2Badge.setTextColor(Color.parseColor("#A0B3AF"))
        binding.tvStep2Text.text = "Confirming"
        binding.tvStep2Text.setTextColor(Color.parseColor("#6F7E7D"))

        binding.llStepperContainer.alpha = 1f
        binding.llStepperContainer.visibility = View.VISIBLE

        binding.llSuccessContainer.visibility = View.GONE
        binding.llSuccessContainer.alpha = 0f
        binding.llSuccessContainer.scaleX = 0.85f
        binding.llSuccessContainer.scaleY = 0.85f

        binding.flPauseProgressOverlay.alpha = 0f
        binding.flPauseProgressOverlay.visibility = View.VISIBLE
        binding.flPauseProgressOverlay.animate()
            .alpha(1f)
            .setDuration(250)
            .start()

        profileViewModel.deleteDataSourceData(args.assessmentId)
    }

    private fun handleDeleteApiSuccess() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            context?.let { ctx ->
                binding.tvStep2Badge.background =
                    ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_active)
            }
            binding.tvStep2Badge.setTextColor(Color.parseColor("#111827"))
            binding.tvStep2Text.setTextColor(Color.parseColor("#FFFFFF"))

            binding.tvStep2Badge.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(120)
                .withEndAction {
                    binding.tvStep2Badge.animate().scaleX(1f).scaleY(1f).setDuration(120)
                        .start()
                }.start()

            handler.postDelayed({
                binding.llStepperContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.llStepperContainer.visibility = View.GONE

                        binding.tvSuccessTitle.text = "Data deleted"
                        binding.llSuccessContainer.visibility = View.VISIBLE
                        binding.llSuccessContainer.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start()
                    }.start()

                handler.postDelayed({
                    binding.flPauseProgressOverlay.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction {
                            binding.flPauseProgressOverlay.visibility = View.GONE
                            binding.flPauseProgressOverlay.alpha = 1f
                            profileViewModel.fetchDataSources()
                            findNavController().navigateUp()
                        }.start()
                }, 1400)

            }, 800)

        }, 350)
    }

    private fun setupObservers() {
        profileViewModel.getDataSourceDetailLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.data?.let { detail ->
                        detail.isPaused?.let { paused ->
                            isPausedState = paused
                            updatePauseControlUi(isPausedState)
                        }
                        detail.includeInAnalysis?.let { include ->
                            binding.switchInclude.setOnCheckedChangeListener(null)
                            binding.switchInclude.isChecked = include
                            binding.switchInclude.setOnCheckedChangeListener { _, isChecked ->
                                performIncludeInAnalysisApi(isChecked)
                            }
                        }
                        detail.topicToggles?.forEach { toggle ->
                            toggle.key?.let { k ->
                                topicStatesMap[k] = toggle.isEnabled == true
                            }
                        }
                        detail.excludedTopics?.let { excludedList ->
                            topicStatesMap.keys.toList().forEach { k ->
                                topicStatesMap[k] = !excludedList.contains(k)
                            }
                        }
                        if (!detail.topicToggles.isNullOrEmpty() || detail.excludedTopics != null) {
                            populateQuestions()
                        }
                    }
                }

                Status.ERROR, Status.EXCEPTION, Status.LOADING -> {}
            }
        }

        profileViewModel.getPauseDataSourceLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    lastTargetPaused?.let { target ->
                        isPausedState = target
                        handlePauseApiSuccess()
                        lastTargetPaused = null
                    } ?: run {
                        profileViewModel.fetchDataSources()
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    binding.flPauseProgressOverlay.visibility = View.GONE
                    lastTargetPaused?.let {
                        updatePauseControlUi(isPausedState)
                        lastTargetPaused = null
                    }
                    val msg = resource.error?.errorMessage?.takeIf { it.isNotBlank() }
                        ?: resource.error?.error?.takeIf { it.isNotBlank() }
                        ?: "Failed to update assessment"
                    ToastUtils.showShort(requireContext(), msg)
                }

                Status.LOADING -> {}
            }
        }

        profileViewModel.getUpdateDataSourceTopicsLiveData()
            .observe(viewLifecycleOwner) { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        profileViewModel.fetchDataSources()
                    }

                    Status.ERROR, Status.EXCEPTION -> {
                        val msg = resource.error?.errorMessage?.takeIf { it.isNotBlank() }
                            ?: resource.error?.error?.takeIf { it.isNotBlank() }
                            ?: "Failed to update topics"
                        ToastUtils.showShort(requireContext(), msg)
                    }

                    Status.LOADING -> {}
                }
            }

        profileViewModel.getDeleteDataSourceDataLiveData()
            .observe(viewLifecycleOwner) { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        handleDeleteApiSuccess()
                    }

                    Status.ERROR, Status.EXCEPTION -> {
                        binding.flPauseProgressOverlay.visibility = View.GONE
                        val msg = resource.error?.errorMessage?.takeIf { it.isNotBlank() }
                            ?: resource.error?.error?.takeIf { it.isNotBlank() }
                            ?: "Failed to delete data"
                        ToastUtils.showShort(requireContext(), msg)
                    }

                    Status.LOADING -> {}
                }
            }
    }

    private val topicStatesMap = mutableMapOf<String, Boolean>()

    private fun getTopicKey(question: String): String {
        return when (question.trim().lowercase()) {
            "alcohol intake" -> "alcoholIntake"
            "smoking status" -> "smokingStatus"
            "caffeine habits" -> "caffeineHabits"
            "sleep schedule" -> "sleepSchedule"
            "family history" -> "familyHistory"
            "current medications" -> "currentMedications"
            "past conditions" -> "pastConditions"
            "primary goal" -> "primaryGoal"
            "current symptoms" -> "currentSymptoms"
            else -> {
                val words = question.trim().split(" ")
                if (words.isEmpty()) ""
                else words.first().lowercase() + words.drop(1).joinToString("") { word ->
                    word.lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }
    }

    private fun populateQuestions() {
        binding.llQuestionsContainer.removeAllViews()
        val questions = assessQuestions[args.assessmentId] ?: return

        val density = resources.displayMetrics.density
        val padding15 = (15 * density).toInt()
        val padding8 = (8 * density).toInt()

        // Pre-fill topicStatesMap if empty
        questions.forEach { q ->
            val key = getTopicKey(q)
            if (key.isNotBlank() && !topicStatesMap.containsKey(key)) {
                topicStatesMap[key] = true
            }
        }

        questions.forEachIndexed { index, question ->
            val topicKey = getTopicKey(question)

            // Question Row Container
            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(padding15, padding8, padding15, padding8)
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
                setTypeface(this.typeface, android.graphics.Typeface.BOLD)
            }
            row.addView(textView)

            // Switch
            val switchCompat = SwitchCompat(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isChecked = topicStatesMap[topicKey] ?: true
                thumbTintList = ColorStateList.valueOf(Color.WHITE)
                trackTintList = resources.getColorStateList(
                    R.color.switch_track_color,
                    requireContext().theme
                )

                setOnCheckedChangeListener { _, isChecked ->
                    if (topicKey.isNotBlank()) {
                        topicStatesMap[topicKey] = isChecked
                        profileViewModel.updateDataSourceTopics(
                            args.assessmentId,
                            topicStatesMap
                        )
                    }
                }
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

    private fun setupInsets() {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars =
                insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val extraBottomPadding = (10 * resources.displayMetrics.density).toInt()
            androidx.core.view.ViewCompat.setPaddingRelative(
                binding.contentScrollView,
                binding.contentScrollView.paddingStart,
                binding.contentScrollView.paddingTop,
                binding.contentScrollView.paddingEnd,
                systemBars.bottom + extraBottomPadding
            )
            insets
        }
    }
}
