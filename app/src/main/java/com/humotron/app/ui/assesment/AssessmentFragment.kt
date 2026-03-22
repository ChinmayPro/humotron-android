package com.humotron.app.ui.assesment
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.card.MaterialCardView
import com.humotron.app.R
import com.humotron.app.databinding.FragmentAssessmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        //        window.statusBarColor = android.graphics.Color.TRANSPARENT
//        WindowInsetsControllerCompat(window, window.decorView)
//            .isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel.loadAssessment()

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // ✅ Fix 1: questions ready hone ke baad hi index observe karo
        viewModel.questionsReady.observe(viewLifecycleOwner) { isReady ->
//            if (isReady) {
                setupObserversAndClicks()
//            }
        }

        // ✅ Fix 2: Loading state handle karo
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

    }

    private fun setupObserversAndClicks() {
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
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
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
        viewModel.submitAllAnswers()
    }
    private fun renderQuestion() {
        val question = viewModel.currentQuestion ?: return
        // ✅ Fix 3: Null safety — questions list empty ho sakti hai
        val index = viewModel.currentIndex.value ?: 0
        val savedAnswer = viewModel.getCurrentAnswer()

        binding.tvQuestionCounter.text = "Question ${index + 1} of ${viewModel.totalQuestions}"
        binding.tvQuestion.text = question.questionText
        binding.tvHelper.text = question.helperText

        val animationRes = if (isNext) R.anim.slide_in_right else R.anim.slide_in_left
        val slideIn = AnimationUtils.loadAnimation(requireContext(), animationRes)
        binding.contentContainer.startAnimation(slideIn)

        // Sab containers pehle hide karo
        binding.containerRadioList.root.visibility = View.GONE
        binding.containerYesNo.root.visibility = View.GONE
        binding.containerMultiSelect.root.visibility = View.GONE
        Log.e("TAG", "renderQdwdfffuestion: ${question.type} ", )
        when (val type = question.type) {

            is QuestionType.RadioList -> {
                Log.e("TAG", "renderQdwdfffuestion1: ${question.type} ", )

                binding.containerRadioList.root.visibility = View.VISIBLE
                setupRadioList(type, savedAnswer)
            }
            is QuestionType.YesNo -> {
                Log.e("TAG", "renderQdwdfffuestion2: ${question.type} ", )

                binding.containerYesNo.root.visibility = View.VISIBLE
                setupYesNo(type, savedAnswer)
            }
            is QuestionType.MultiSelect -> {
                Log.e("TAG", "renderQdwdfffuestion3: ${question.type} ", )

                binding.containerMultiSelect.root.visibility = View.VISIBLE
                setupMultiSelect(type, savedAnswer)
            }
        }

        updateNavigationButtons()
    }

    private fun setupRadioList(type: QuestionType.RadioList, savedAnswer: AssessmentAnswer?) {
        val container = binding.containerRadioList

        // ✅ Step 1: Pehle listener remove karo
        container.radioGroup.setOnCheckedChangeListener(null)

        // ✅ Step 2: Pehle RadioGroup ko forcefully clear karo — checked state bhi reset ho
        container.radioGroup.clearCheck()
        container.radioGroup.removeAllViews()

        type.options.forEachIndexed { index, option ->
            val radioBtn = layoutInflater.inflate(
                R.layout.item_radio_option,
                container.radioGroup,
                false
            ) as android.widget.RadioButton

            radioBtn.text = option
            radioBtn.id = index
            container.radioGroup.addView(radioBtn)
        }

        // ✅ Step 3: Pehle views add karo, PHIR saved answer restore karo
        // Agar pehle isChecked=true set karte aur listener baad mein lagaate,
        // to check event miss ho jaata
        savedAnswer?.selectedIndex?.let { savedIndex ->
            container.radioGroup.check(savedIndex)
        }

        // ✅ Step 4: Listener sabse last mein lagao
        container.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId >= 0) {
                viewModel.saveAnswer(
                    AssessmentAnswer(
                        questionId = viewModel.currentQuestion?.id
                            ?: return@setOnCheckedChangeListener,
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
        val isNo = savedAnswer?.selectedIndex == 0

        // ✅ Fix 5: Saved state ke hisaab se card select karo
        when {
            isYes -> selectCard(container.cardYes, container.tvYes, container.cardNo, container.tvNo)
            isNo  -> selectCard(container.cardNo, container.tvNo, container.cardYes, container.tvYes)
            else  -> {
                // Koi bhi selected nahi — dono unselected
                resetCard(container.cardYes, container.tvYes)
                resetCard(container.cardNo, container.tvNo)
            }
        }

        container.cardNo.setOnClickListener {
            viewModel.saveAnswer(AssessmentAnswer(viewModel.currentQuestion?.id ?: return@setOnClickListener, selectedIndex = 0))
            selectCard(container.cardNo, container.tvNo, container.cardYes, container.tvYes)
            container.layoutConditional.visibility = View.GONE
            updateNavigationButtons()
        }

        container.cardYes.setOnClickListener {
            viewModel.saveAnswer(AssessmentAnswer(viewModel.currentQuestion?.id ?: return@setOnClickListener, selectedIndex = 1))
            selectCard(container.cardYes, container.tvYes, container.cardNo, container.tvNo)

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

    // ✅ Fix 6: Reset (unselected) state alag function mein
    private fun resetCard(card: MaterialCardView, tv: TextView) {
        card.setCardBackgroundColor(Color.TRANSPARENT)
        card.setStrokeColor(Color.parseColor("#4DA1A1A1"))
        card.strokeWidth = 1
        context?.let { tv.setTextColor(it.getColor(R.color.d60)) }
    }

    fun selectCard(
        selected: MaterialCardView, selectedTv: TextView,
        unselected: MaterialCardView, unselectedTv: TextView
    ) {
        context?.let { selected.setCardBackgroundColor(it.getColor(R.color.assessmentBnt)) }
        selected.strokeWidth = 0
        context?.getColor(R.color.d900)?.let { selectedTv.setTextColor(it) }

        unselected.setCardBackgroundColor(Color.TRANSPARENT)
        unselected.setStrokeColor(Color.parseColor("#4DA1A1A1"))
        unselected.strokeWidth = 1
        context?.let { unselectedTv.setTextColor(it.getColor(R.color.d60)) }
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

        // ✅ Fix 7: Listener duplicate hone se bachao — naya set karo har baar
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
            if (viewModel.isLastQuestion()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}