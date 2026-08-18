package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentAiChatBinding
import com.humotron.app.domain.modal.param.ChatConfigRequest
import com.humotron.app.ui.profile.dialog.SavingAiChatDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiChatFragment : BaseFragment(R.layout.fragment_ai_chat) {

    private lateinit var binding: FragmentAiChatBinding
    private val viewModel: AiChatViewModel by viewModels()
    private var savingAiChatDialog: SavingAiChatDialog? = null

    private var selectedResponseStyle: String = "Deep"
    private var selectedFocus: String = "Understanding"
    private var selectedGuidance: Boolean = true
    private var selectedBoth: Boolean = false
    private var selectedHistoryDays: Int = 90

    private var initialResponseStyle: String = "Deep"
    private var initialFocus: String = "Understanding"
    private var initialGuidance: Boolean = true
    private var initialBoth: Boolean = false
    private var initialHistoryDays: Int = 90

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAiChatBinding.bind(view)

        // Header Title
        binding.header.title.text = "AI Chat"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        updateSaveButtonState(false)

        binding.btnSaveAiChat.setOnClickListener {
            showSavingDialogAndTriggerSave()
        }

        initData()
        observeData()
    }

    private fun updateSaveButtonState(enabled: Boolean) {
        binding.btnSaveAiChat.isEnabled = enabled
    }

    private fun checkSelectionChanged() {
        val hasChanged = selectedResponseStyle != initialResponseStyle ||
                selectedFocus != initialFocus ||
                selectedGuidance != initialGuidance ||
                selectedBoth != initialBoth ||
                selectedHistoryDays != initialHistoryDays
        updateSaveButtonState(hasChanged)
    }

    private fun initData() {
        viewModel.fetchChatConfig()
    }

    private fun observeData() {
        viewModel.getChatConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val prefs = resource.data?.data?.preferences
                    prefs?.let {
                        selectedResponseStyle = it.getSelectedResponseStyle()
                        selectedFocus = it.getSelectedFocus()
                        selectedHistoryDays = it.getSelectedChatHistoryDays()
                        it.context?.let { ctx ->
                            selectedGuidance = ctx.guidance ?: true
                            selectedBoth = ctx.both ?: false
                        }
                    }

                    initialResponseStyle = selectedResponseStyle
                    initialFocus = selectedFocus
                    initialGuidance = selectedGuidance
                    initialBoth = selectedBoth
                    initialHistoryDays = selectedHistoryDays

                    renderAllUI()
                    updateSaveButtonState(false)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    renderAllUI()
                    updateSaveButtonState(false)
                }

                Status.LOADING -> {
                    showProgress()
                }

                else -> {}
            }
        }

        viewModel.getSaveChatLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    savingAiChatDialog?.onApiSuccess()
                }

                Status.ERROR, Status.EXCEPTION -> {
                    savingAiChatDialog?.onApiError()
                    val msg = resource.error?.errorMessage ?: resource.data?.message ?: "Failed to save settings"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {
                    // Progress animation handled in SavingAiChatDialog
                }

                else -> {}
            }
        }
    }

    private fun showSavingDialogAndTriggerSave() {
        if (savingAiChatDialog?.isShowing == true) return

        savingAiChatDialog = SavingAiChatDialog(requireContext()) {
            findNavController().navigateUp()
        }
        savingAiChatDialog?.show()

        saveConfig()
    }

    private fun saveConfig() {
        val param = ChatConfigRequest(
            preferences = ChatConfigRequest.Preferences(
                chatHistoryDays = selectedHistoryDays,
                focus = selectedFocus,
                context = ChatConfigRequest.ContextParam(
                    guidance = selectedGuidance,
                    both = selectedBoth
                ),
                responseStyle = selectedResponseStyle
            )
        )
        viewModel.saveChatConfig(param)
    }

    private fun renderAllUI() {
        renderResponseStyleChips()
        renderFocusChips()
        renderContextSwitches()
        renderHistoryChips()
    }

    private fun renderResponseStyleChips() {
        val styleList = listOf(
            Triple(binding.chipStyleDirect, "Direct", "Short, clear answers"),
            Triple(binding.chipStyleBalanced, "Balanced", "Some context"),
            Triple(binding.chipStyleDeep, "Deep", "Deeper explanations")
        )
        styleList.forEach { (chipBinding, title, desc) ->
            chipBinding.tvChipTitle.text = title
            chipBinding.tvChipDesc.text = desc
            val isSelected = selectedResponseStyle.equals(title, ignoreCase = true)
            updateChipState(chipBinding.root, chipBinding.tvChipTitle, chipBinding.tvChipDesc, isSelected)

            chipBinding.root.setOnClickListener {
                selectedResponseStyle = title
                renderResponseStyleChips()
                checkSelectionChanged()
            }
        }
    }

    private fun renderFocusChips() {
        val focusList = listOf(
            Triple(binding.chipFocusGuidance, "Guidance", "What to do next"),
            Triple(binding.chipFocusBoth, "Both", "Next steps + patterns"),
            Triple(binding.chipFocusUnderstanding, "Understanding", "Explain patterns")
        )
        focusList.forEach { (chipBinding, title, desc) ->
            chipBinding.tvChipTitle.text = title
            chipBinding.tvChipDesc.text = desc
            val isSelected = selectedFocus.equals(title, ignoreCase = true)
            updateChipState(chipBinding.root, chipBinding.tvChipTitle, chipBinding.tvChipDesc, isSelected)

            chipBinding.root.setOnClickListener {
                selectedFocus = title
                renderFocusChips()
                checkSelectionChanged()
            }
        }
    }

    private fun renderContextSwitches() {
        binding.switchGuidance.tvSwitchTitle.text = "Guidance"
        binding.switchGuidance.tvSwitchDesc.text = "Focus on what to do next"
        binding.switchGuidance.switchToggle.setOnCheckedChangeListener(null)
        binding.switchGuidance.switchToggle.isChecked = selectedGuidance
        binding.switchGuidance.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            selectedGuidance = isChecked
            checkSelectionChanged()
        }

        binding.switchPatterns.tvSwitchTitle.text = "Patterns"
        binding.switchPatterns.tvSwitchDesc.text = "Explain the patterns behind the numbers"
        binding.switchPatterns.switchToggle.setOnCheckedChangeListener(null)
        binding.switchPatterns.switchToggle.isChecked = selectedBoth
        binding.switchPatterns.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            selectedBoth = isChecked
            checkSelectionChanged()
        }
    }

    private fun renderHistoryChips() {
        val historyList = listOf(
            Pair(binding.chipHistory60, 60),
            Pair(binding.chipHistory90, 90),
            Pair(binding.chipHistory180, 180)
        )
        historyList.forEach { (chipBinding, days) ->
            chipBinding.tvChipTitle.text = "$days days"
            val isSelected = selectedHistoryDays == days
            updateChipState(chipBinding.root, chipBinding.tvChipTitle, null, isSelected)

            chipBinding.root.setOnClickListener {
                selectedHistoryDays = days
                renderHistoryChips()
                checkSelectionChanged()
            }
        }
    }

    private fun updateChipState(
        root: View,
        titleView: androidx.appcompat.widget.AppCompatTextView,
        descView: androidx.appcompat.widget.AppCompatTextView?,
        isSelected: Boolean
    ) {
        root.tag = isSelected
        if (isSelected) {
            root.setBackgroundResource(R.drawable.bg_insight_chip_selected)
            titleView.setTextColor(android.graphics.Color.parseColor("#C4F23E")) // lime
            descView?.setTextColor(android.graphics.Color.parseColor("#B9C8C6")) // light grey/teal
        } else {
            root.setBackgroundResource(R.drawable.bg_insight_chip_unselected)
            titleView.setTextColor(android.graphics.Color.parseColor("#8EA09E")) // muted grey/teal
            descView?.setTextColor(android.graphics.Color.parseColor("#8EA09E")) // muted grey/teal
        }
    }

    override fun onDestroyView() {
        savingAiChatDialog?.dismiss()
        savingAiChatDialog = null
        super.onDestroyView()
    }
}
