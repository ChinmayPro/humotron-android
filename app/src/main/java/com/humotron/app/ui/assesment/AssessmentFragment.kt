package com.humotron.app.ui.assesment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import com.humotron.app.R
import com.humotron.app.databinding.FragmentAssessmentBinding

class AssessmentFragment : Fragment() {

    private var _binding: FragmentAssessmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AssessmentViewModel by viewModels()
    private var isNext = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        viewModel.currentIndex.observe(viewLifecycleOwner) { renderQuestion() }

        binding.btnNext.setOnClickListener {
            isNext = true
            if (viewModel.isLastQuestion()) {
                // Submit
                val answers = viewModel.getAllAnswers()
                // navigate or callback
            } else {
                viewModel.goNext()
            }
        }

        binding.btnPrevious.setOnClickListener {
            isNext = false

            viewModel.goPrevious() }

        binding.tvSkip.setOnClickListener {
            isNext = true
            if (!viewModel.isLastQuestion()) viewModel.goNext()
        }
    }

    private fun renderQuestion() {
        val question = viewModel.currentQuestion
        val index = viewModel.currentIndex.value ?: 0
        val savedAnswer = viewModel.getCurrentAnswer()

        // Header
        binding.tvQuestionCounter.text = "Question ${index + 1} of ${viewModel.totalQuestions}"
        binding.tvQuestion.text = question.questionText
        binding.tvHelper.text = question.helperText

        // Slide-in animation
//        val slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
//        binding.contentContainer.startAnimation(slideIn)
        val animationRes = if (isNext) {
            R.anim.slide_in_right
        } else {
            R.anim.slide_in_left
        }

        val slideIn = AnimationUtils.loadAnimation(requireContext(), animationRes)
        binding.contentContainer.startAnimation(slideIn)


        // Hide all question containers
        binding.containerRadioList.root.visibility = View.GONE
        binding.containerYesNo.root.visibility = View.GONE
        binding.containerMultiSelect.root.visibility = View.GONE

        when (val type = question.type) {
            is QuestionType.RadioList -> {
                binding.containerRadioList.root.visibility = View.VISIBLE
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
        val container = binding.containerRadioList
        container.radioGroup.removeAllViews()

        type.options.forEachIndexed { index, option ->
            val radioBtn = layoutInflater.inflate(
                R.layout.item_radio_option,
                container.radioGroup,
                false
            ) as android.widget.RadioButton

            radioBtn.text = option
            radioBtn.id = index
            radioBtn.isChecked = savedAnswer?.selectedIndex == index
            container.radioGroup.addView(radioBtn)
        }

        container.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId >= 0) {
                viewModel.saveAnswer(
                    AssessmentAnswer(
                        questionId = viewModel.currentQuestion.id,
                        selectedIndex = checkedId
                    )
                )
                updateNavigationButtons()
            }
        }
    }

    private fun setupYesNo(type: QuestionType.YesNo, savedAnswer: AssessmentAnswer?) {
        val container = binding.containerYesNo
        val isYes = savedAnswer?.selectedIndex == 1

//        updateYesNoButtons(isYes)
        selectCard(container.cardNo, container.tvNo, container.cardYes, container.tvYes)


        container.cardNo.setOnClickListener {
            viewModel.saveAnswer(AssessmentAnswer(viewModel.currentQuestion.id, selectedIndex = 0))
//            updateYesNoButtons(false)
            selectCard(container.cardNo, container.tvNo, container.cardYes, container.tvYes)

            container.layoutConditional.visibility = View.GONE
            updateNavigationButtons()
        }

        container.cardYes.setOnClickListener {
            viewModel.saveAnswer(AssessmentAnswer(viewModel.currentQuestion.id, selectedIndex = 1))
//            updateYesNoButtons(true)
            selectCard(container.cardYes, container.tvYes, container.cardNo, container.tvNo)

            if (type.conditionalOptions != null) {
                container.layoutConditional.visibility = View.VISIBLE
                container.tvConditionalLabel.text = type.conditionalLabel
                setupConditionalRadioList(type.conditionalOptions)
            }
            updateNavigationButtons()
        }

        // Restore conditional if was Yes
        if (isYes && type.conditionalOptions != null) {
            container.layoutConditional.visibility = View.VISIBLE
            container.tvConditionalLabel.text = type.conditionalLabel
            setupConditionalRadioList(type.conditionalOptions)
        } else {
            container.layoutConditional.visibility = View.GONE
        }
    }
    fun selectCard(selected: MaterialCardView, selectedTv: TextView,
                   unselected: MaterialCardView, unselectedTv: TextView) {
        // Selected state
        context?.let { selected.setCardBackgroundColor(it.getColor(R.color.assessmentBnt)) }
        selected.strokeWidth = 0
        context?.getColor(R.color.d900)?.let { selectedTv.setTextColor(it ) }

        // Unselected state
        unselected.setCardBackgroundColor(Color.TRANSPARENT)
        unselected.setStrokeColor(Color.parseColor("#4DA1A1A1"))
        unselected.strokeWidth = 1  // back to border
        context?.let { unselectedTv.setTextColor(it.getColor(R.color.d60)) }
    }
//    private fun updateYesNoButtons(isYes: Boolean) {
//        val container = binding.containerYesNo
//        val activeColor = requireContext().getColor(R.color.lime_green)
//        val inactiveColor = requireContext().getColor(R.color.surface_dark)
//        val activeTextColor = requireContext().getColor(R.color.black)
//        val inactiveTextColor = requireContext().getColor(R.color.text_secondary)
//
//        container.cardNo.setBackgroundColor(if (!isYes) activeColor else inactiveColor)
//        container.tvNo.setTextColor(if (!isYes) activeTextColor else inactiveTextColor)
//        container.cardYes.setBackgroundColor(if (isYes) activeColor else inactiveColor)
//        container.tvYes.setTextColor(if (isYes) activeTextColor else inactiveTextColor)
//    }

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

        // Show first selected item in the selector field, or placeholder
        fun refreshSelectorText() {
            container.tvSelectorValue.text = if (selectedItems.isEmpty()) "Select" else selectedItems.first()
        }
        refreshSelectorText()

        // Show chips for selected items
        fun refreshChips() {
            container.chipGroup.removeAllViews()
            selectedItems.forEach { item ->
                val chip = layoutInflater.inflate(
                    R.layout.item_chip_selected,
                    container.chipGroup,
                    false
                ) as ViewGroup
                chip.findViewById<android.widget.TextView>(R.id.tvChipLabel).text = item
                chip.findViewById<View>(R.id.btnChipRemove).setOnClickListener {
                    selectedItems.remove(item)
                    viewModel.saveAnswer(AssessmentAnswer(viewModel.currentQuestion.id, selectedItems = selectedItems.toList()))
                    refreshChips()
                    refreshSelectorText()
                    updateNavigationButtons()
                }
                container.chipGroup.addView(chip)
            }
        }
        refreshChips()

        container.layoutSelector.setOnClickListener {
            MultiSelectBottomSheet.newInstance(
                options = type.options,
                selectedItems = selectedItems
            ) { newSelection ->
                selectedItems.clear()
                selectedItems.addAll(newSelection)
                viewModel.saveAnswer(AssessmentAnswer(viewModel.currentQuestion.id, selectedItems = selectedItems.toList()))
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

        binding.btnPrevious.visibility = if (viewModel.isFirstQuestion()) View.GONE else View.VISIBLE
        binding.tvSkip.visibility = if (viewModel.isLastQuestion()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}