package com.humotron.app.ui.assessment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.humotron.app.R
import com.humotron.app.databinding.FragmentAssessmentBinding
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssessmentFragment : Fragment(R.layout.fragment_assessment) {

    private lateinit var binding: FragmentAssessmentBinding

    private val viewModel: AssessmentViewModel by viewModels()
    private var isNext = true
    private val deviceViewModel: DeviceViewModel by viewModels()
    private var radioAdapter: AssessmentOptionAdapter? = null

    private var mergedAssessment: MergedAssessment? = null
    private var initialJumpDone = false

    @Inject
    lateinit var prefUtils: PrefUtils

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAssessmentBinding.bind(view)
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
        mergedAssessment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(
                NavKeys.ASSESSMENT,
                MergedAssessment::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.ASSESSMENT)
        }
        mergedAssessment?.let {
            viewModel.loadAssessment(
                it.assessmentId
            )
        }
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnNext.setOnClickListener {
            isNext = true
            val currentAnswer = viewModel.getCurrentAnswer()

            if (viewModel.isLastQuestion()) {
                // ✅ Last question — final submit
                submitAnswers()
            } else {
                // ✅ Next click pe current answer API ko bhejo silently
                currentAnswer?.let {
                    viewModel.saveAnswerToApi(it, mergedAssessment)
                }
                viewModel.goNext()
            }
        }

        binding.btnPrevious.setOnClickListener {
            isNext = false
            viewModel.goPrevious()
        }

        binding.tvSkip.setOnClickListener {
            isNext = true
            if (!viewModel.isLastQuestion()) viewModel.goNext()
        }
    }

    private fun observeViewModel() {
        viewModel.questionsReady.observe(viewLifecycleOwner) { isReady ->
            if (isReady) {
                setupObserversAndClicks()
                if (!initialJumpDone) {
                    initialJumpDone = true
                    val startIndex = viewModel.getFirstUnansweredIndex()
                    viewModel.jumpToIndex(startIndex)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.contentContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun setupObserversAndClicks() {
        viewModel.currentIndex.observe(viewLifecycleOwner) {
            renderQuestion()
        }

        viewModel.submitSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                deviceViewModel.refreshUserDeviceData(true)
                deviceViewModel.getMergedAssessmentList()
                findNavController().navigate(R.id.action_assessmentFragment_to_assessmentBuildingFragment)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                android.widget.Toast.makeText(
                    requireContext(),
                    it,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupObserversAndClicks00() {
        // Index change hone par question render karo
        viewModel.currentIndex.observe(viewLifecycleOwner) {
            renderQuestion()
        }

        viewModel.submitSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                android.widget.Toast.makeText(
                    requireContext(),
                    it,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                deviceViewModel.refreshUserDeviceData(true)
                deviceViewModel.getMergedAssessmentList()
            }
        }

        binding.btnNext.setOnClickListener {
            isNext = true
            if (viewModel.isLastQuestion()) {
                submitAnswers()
            } else {
                viewModel.goNext()
            }
        }

        binding.btnPrevious.setOnClickListener {
            isNext = false
            viewModel.goPrevious()
        }

        binding.tvSkip.setOnClickListener {
            isNext = true
            if (!viewModel.isLastQuestion()) viewModel.goNext()
        }
    }

    private fun submitAnswers() {
        viewModel.submitAllAnswers(mergedAssessment)
    }

    private fun renderQuestion() {
        val question = viewModel.currentQuestion ?: return
        val index = viewModel.currentIndex.value ?: 0
        val savedAnswer = viewModel.getCurrentAnswer()

        binding.tvQuestionCounter.text = "Question ${index + 1} of ${viewModel.totalQuestions}"
        binding.tvQuestion.text = question.questionText
        binding.tvHelper.text = question.helperText

        val animationRes = if (isNext) R.anim.slide_in_right else R.anim.slide_in_left
        val slideIn = AnimationUtils.loadAnimation(requireContext(), animationRes)
        binding.contentContainer.startAnimation(slideIn)

        // Sab containers pehle hide karo
        binding.rvRadioList.visibility = View.GONE
        binding.containerYesNo.root.visibility = View.GONE
        binding.containerMultiSelect.root.visibility = View.GONE

        Log.e("TAG", "renderQuestion: ${question.type}")

        when (val type = question.type) {
            is QuestionType.RadioList -> {
                binding.rvRadioList.visibility = View.VISIBLE
                setupRadioList(type, savedAnswer)
            }

            is QuestionType.YesNo -> {
                binding.containerYesNo.root.visibility = View.VISIBLE
                setupYesNo(type, savedAnswer)
            }

            is QuestionType.MultiSelect -> {
                binding.containerMultiSelect.root.visibility = View.VISIBLE
                setupMultiSelect(type, savedAnswer)
            }
        }

        updateNavigationButtons()
    }

    private fun setupRadioList(type: QuestionType.RadioList, savedAnswer: AssessmentAnswer?) {
        if (radioAdapter == null) {
            radioAdapter =
                AssessmentOptionAdapter(type.options, savedAnswer?.selectedIndex) { position ->
                    viewModel.saveAnswer(
                        AssessmentAnswer(
                            questionId = viewModel.currentQuestion?.id
                                ?: return@AssessmentOptionAdapter,
                            selectedIndex = position
                        )
                    )
                    updateNavigationButtons()
                }
            binding.rvRadioList.adapter = radioAdapter
        } else {
            radioAdapter?.updateData(type.options, savedAnswer?.selectedIndex)
        }
    }

    private fun setupYesNo(type: QuestionType.YesNo, savedAnswer: AssessmentAnswer?) {
        val container = binding.containerYesNo
        val isYes = savedAnswer?.selectedIndex == 1
        val isNo = savedAnswer?.selectedIndex == 0

        // Initially both unselected, then apply saved state
        applyUnselectedStyle(container.cardYes, container.tvYes)
        applyUnselectedStyle(container.cardNo, container.tvNo)

        when {
            isYes -> applySelectedStyle(container.cardYes, container.tvYes)
            isNo -> applySelectedStyle(container.cardNo, container.tvNo)
        }

        container.cardNo.setOnClickListener {
            viewModel.saveAnswer(
                AssessmentAnswer(
                    viewModel.currentQuestion?.id ?: return@setOnClickListener, selectedIndex = 0
                )
            )
            applySelectedStyle(container.cardNo, container.tvNo)
            applyUnselectedStyle(container.cardYes, container.tvYes)
            container.layoutConditional.visibility = View.GONE
            updateNavigationButtons()
        }

        container.cardYes.setOnClickListener {
            viewModel.saveAnswer(
                AssessmentAnswer(
                    viewModel.currentQuestion?.id ?: return@setOnClickListener, selectedIndex = 1
                )
            )
            applySelectedStyle(container.cardYes, container.tvYes)
            applyUnselectedStyle(container.cardNo, container.tvNo)

            if (type.conditionalOptions != null) {
                container.layoutConditional.visibility = View.VISIBLE
                container.tvConditionalLabel.text = type.conditionalLabel
                setupConditionalRadioList(type.conditionalOptions)
            }
            updateNavigationButtons()
        }

        // Restore conditional state
        if (isYes && type.conditionalOptions != null) {
            container.layoutConditional.visibility = View.VISIBLE
            container.tvConditionalLabel.text = type.conditionalLabel
            setupConditionalRadioList(type.conditionalOptions)
        } else {
            container.layoutConditional.visibility = View.GONE
        }
    }

    private fun applyUnselectedStyle(card: MaterialCardView, tv: TextView) {
        context?.let {
            card.setCardBackgroundColor(it.getColor(R.color.card))
            card.strokeColor = it.getColor(R.color.transparent)
            tv.setTextColor(it.getColor(R.color.booster_text_benefit))
        }
    }

    private fun applySelectedStyle(card: MaterialCardView, tv: TextView) {
        context?.let {
            card.setCardBackgroundColor(it.getColor(R.color.lime_8))
            card.strokeColor = it.getColor(R.color.lime)
            tv.setTextColor(it.getColor(R.color.lime))
        }
    }

    private fun setupConditionalRadioList(options: List<String>) {
        val container = binding.containerYesNo
        container.radioGroupConditional.removeAllViews()
        options.forEachIndexed { index, option ->
            val radioBtn = layoutInflater.inflate(
                R.layout.item_radio_option,
                container.radioGroupConditional,
                false
            ) as android.widget.RadioButton
            radioBtn.text = option
            radioBtn.id = index
            container.radioGroupConditional.addView(radioBtn)
        }
    }

    private fun setupMultiSelect(type: QuestionType.MultiSelect, savedAnswer: AssessmentAnswer?) {
        val container = binding.containerMultiSelect
        val selectedItems = savedAnswer?.selectedItems?.toMutableList() ?: mutableListOf()

        fun refreshSelectorText() {
            container.tvSelectorValue.text =
                if (selectedItems.isEmpty()) "Select" else selectedItems.joinToString(", ")
        }

        fun refreshChips() {
            container.chipGroup.removeAllViews()
            selectedItems.forEach { item ->
                val chip = layoutInflater.inflate(
                    R.layout.item_chip_selected,
                    container.chipGroup,
                    false
                ) as ViewGroup
                chip.findViewById<TextView>(R.id.tvChipLabel).text = item
                chip.findViewById<View>(R.id.btnChipRemove).setOnClickListener {
                    selectedItems.remove(item)
                    viewModel.saveAnswer(
                        AssessmentAnswer(
                            viewModel.currentQuestion?.id ?: return@setOnClickListener,
                            selectedItems = selectedItems.toList()
                        )
                    )
                    refreshChips()
                    refreshSelectorText()
                    updateNavigationButtons()
                }
                container.chipGroup.addView(chip)
            }
        }

        refreshSelectorText()
        refreshChips()
        container.layoutSelector.setOnClickListener(null)
        container.layoutSelector.setOnClickListener {
            MultiSelectBottomSheet.newInstance(
                options = type.options,
                selectedItems = selectedItems
            ) { newSelection ->
                selectedItems.clear()
                selectedItems.addAll(newSelection)
                viewModel.saveAnswer(
                    AssessmentAnswer(
                        viewModel.currentQuestion?.id ?: return@newInstance,
                        selectedItems = selectedItems.toList()
                    )
                )
                refreshChips()
                refreshSelectorText()
                updateNavigationButtons()
            }.show(childFragmentManager, MultiSelectBottomSheet.TAG)
        }
    }

    private fun updateNavigationButtons() {
        val hasAnswer = viewModel.canGoNext()
        val isLast = viewModel.isLastQuestion()

        binding.btnNext.text = if (isLast) "Save" else "Next"
        binding.btnNext.isEnabled = hasAnswer
        binding.btnNext.alpha = if (hasAnswer) 1f else 0.4f

        binding.btnPrevious.visibility =
            if (viewModel.isFirstQuestion()) View.GONE else View.VISIBLE
        binding.tvSkip.visibility =
            if (viewModel.isLastQuestion()) View.INVISIBLE else View.VISIBLE
    }
}