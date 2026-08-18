package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentInsightsBinding
import com.humotron.app.databinding.ItemInsightChipBinding
import com.humotron.app.databinding.ItemInsightStyleChipBinding
import com.humotron.app.domain.modal.param.InsightConfigRequest
import com.humotron.app.domain.modal.response.InsightConfigResponse.LabelValueItem
import com.humotron.app.domain.modal.response.InsightConfigResponse.StyleItem
import com.humotron.app.ui.profile.dialog.SavingInsightsDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsightsFragment : BaseFragment(R.layout.fragment_insights) {

    private lateinit var binding: FragmentInsightsBinding
    private val viewModel: InsightsViewModel by viewModels()
    private var savingInsightsDialog: SavingInsightsDialog? = null

    private var selectedStyle: String = "Simple"
    private var selectedFrequencyValue: String = "7"
    private val selectedFocusAreas = mutableSetOf<String>()
    private var selectedTriggerValue: String = "inApp"

    // Initial state to track user modifications for save button enable/disable
    private var initialStyle: String = ""
    private var initialFrequencyValue: String = ""
    private val initialFocusAreas = mutableSetOf<String>()
    private var initialTriggerValue: String = ""

    private var configStyles: List<StyleItem> = emptyList()
    private var configFrequencies: List<LabelValueItem> = emptyList()
    private var configFocusAreas: List<String> = emptyList()
    private var configTriggers: List<LabelValueItem> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInsightsBinding.bind(view)

        // Header Title
        binding.header.title.text = "Insights"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        updateSaveButtonState(false)

        binding.btnSaveInsights.setOnClickListener {
            showSavingDialogAndTriggerSave()
        }

        initData()
        observeData()
    }

    private fun updateSaveButtonState(enabled: Boolean) {
        binding.btnSaveInsights.isEnabled = enabled
    }

    private fun checkSelectionChanged() {
        val hasChanged = selectedStyle != initialStyle ||
                selectedFrequencyValue != initialFrequencyValue ||
                selectedFocusAreas != initialFocusAreas ||
                selectedTriggerValue != initialTriggerValue
        updateSaveButtonState(hasChanged)
    }

    private fun initData() {
        viewModel.fetchInsightConfig()
    }

    private fun observeData() {
        viewModel.getInsightConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val config = resource.data?.data?.configuration
                    val prefs = resource.data?.data?.preferences

                    configStyles = config?.insightStyle ?: getDefaultStyles()
                    configFrequencies = config?.frequency ?: getDefaultFrequencies()
                    configFocusAreas = config?.focusAreas ?: getDefaultFocusAreas()
                    configTriggers = config?.triggers ?: getDefaultTriggers()

                    // Parse initial selections from preferences
                    prefs?.let {
                        selectedStyle = it.getSelectedStyleTitle()
                        selectedFrequencyValue = it.getSelectedFrequencyValue()

                        selectedFocusAreas.clear()
                        it.focusAreas?.let { areas -> selectedFocusAreas.addAll(areas) }

                        it.triggers?.let { trg -> selectedTriggerValue = trg }
                    }

                    // Save initial state baseline
                    initialStyle = selectedStyle
                    initialFrequencyValue = selectedFrequencyValue
                    initialFocusAreas.clear()
                    initialFocusAreas.addAll(selectedFocusAreas)
                    initialTriggerValue = selectedTriggerValue

                    renderAllDynamicChips()
                    updateSaveButtonState(false)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    configStyles = getDefaultStyles()
                    configFrequencies = getDefaultFrequencies()
                    configFocusAreas = getDefaultFocusAreas()
                    configTriggers = getDefaultTriggers()

                    renderAllDynamicChips()
                    updateSaveButtonState(false)
                }

                Status.LOADING -> {
                    showProgress()
                }

                else -> {}
            }
        }

        viewModel.getSaveInsightLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    savingInsightsDialog?.onApiSuccess()
                }

                Status.ERROR, Status.EXCEPTION -> {
                    savingInsightsDialog?.onApiError()
                    val msg = resource.error?.errorMessage ?: resource.data?.message ?: "Failed to save insights"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {
                    // Progress animation handled in SavingInsightsDialog
                }

                else -> {}
            }
        }
    }

    private fun showSavingDialogAndTriggerSave() {
        if (savingInsightsDialog?.isShowing == true) return

        savingInsightsDialog = SavingInsightsDialog(requireContext()) {
            findNavController().navigateUp()
        }
        savingInsightsDialog?.show()

        val freqVal: Any = selectedFrequencyValue.toIntOrNull() ?: selectedFrequencyValue
        val param = InsightConfigRequest(
            preferences = InsightConfigRequest.Preferences(
                frequency = freqVal,
                triggers = selectedTriggerValue,
                insightStyle = selectedStyle,
                focusAreas = selectedFocusAreas.toList()
            )
        )
        viewModel.saveInsightConfig(param)
    }

    private fun renderAllDynamicChips() {
        renderStyleChips()
        renderFrequencyChips()
        renderFocusAreaChips()
        renderTriggerChips()
    }

    private fun renderStyleChips() {
        binding.flexInsightStyle.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        configStyles.forEach { item ->
            val styleTitle = item.title ?: item.value ?: "Simple"
            val styleSubtitle = item.subtitle ?: ""

            val chipBinding = ItemInsightStyleChipBinding.inflate(inflater, binding.flexInsightStyle, false)
            chipBinding.tvChipTitle.text = styleTitle
            chipBinding.tvChipDesc.text = styleSubtitle

            val isSelected = selectedStyle.equals(styleTitle, ignoreCase = true) || selectedStyle.equals(item.value, ignoreCase = true)
            updateChipUI(chipBinding.root, chipBinding.tvChipTitle, chipBinding.tvChipDesc, isSelected)

            chipBinding.root.setOnClickListener {
                selectedStyle = styleTitle
                renderStyleChips()
                checkSelectionChanged()
            }

            binding.flexInsightStyle.addView(chipBinding.root)
        }
    }

    private fun renderFrequencyChips() {
        binding.flexFrequency.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        configFrequencies.forEach { item ->
            val label = item.label ?: "Every ${item.value} days"
            val valStr = item.value ?: "7"

            val chipBinding = ItemInsightChipBinding.inflate(inflater, binding.flexFrequency, false)
            chipBinding.tvChipTitle.text = label

            val isSelected = selectedFrequencyValue == valStr
            updateChipUI(chipBinding.root, chipBinding.tvChipTitle, null, isSelected)

            chipBinding.root.setOnClickListener {
                selectedFrequencyValue = valStr
                renderFrequencyChips()
                checkSelectionChanged()
            }

            binding.flexFrequency.addView(chipBinding.root)
        }
    }

    private fun renderFocusAreaChips() {
        binding.flexFocusAreas.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        configFocusAreas.forEach { areaName ->
            val chipBinding = ItemInsightChipBinding.inflate(inflater, binding.flexFocusAreas, false)
            chipBinding.tvChipTitle.text = areaName

            val isSelected = selectedFocusAreas.contains(areaName)
            updateChipUI(chipBinding.root, chipBinding.tvChipTitle, null, isSelected)

            chipBinding.root.setOnClickListener {
                if (selectedFocusAreas.contains(areaName)) {
                    selectedFocusAreas.remove(areaName)
                } else {
                    selectedFocusAreas.add(areaName)
                }
                renderFocusAreaChips()
                checkSelectionChanged()
            }

            binding.flexFocusAreas.addView(chipBinding.root)
        }
    }

    private fun renderTriggerChips() {
        binding.flexTriggers.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        configTriggers.forEach { item ->
            val label = item.label ?: item.value ?: "Both"
            val valStr = item.value ?: "both"

            val chipBinding = ItemInsightChipBinding.inflate(inflater, binding.flexTriggers, false)
            chipBinding.tvChipTitle.text = label

            val isSelected = selectedTriggerValue.equals(valStr, ignoreCase = true)
            updateChipUI(chipBinding.root, chipBinding.tvChipTitle, null, isSelected)

            chipBinding.root.setOnClickListener {
                selectedTriggerValue = valStr
                renderTriggerChips()
                checkSelectionChanged()
            }

            binding.flexTriggers.addView(chipBinding.root)
        }
    }

    private fun updateChipUI(
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

    private fun getDefaultStyles(): List<StyleItem> = listOf(
        StyleItem("Simple", "Quick, easy-to-understand summaries", "Simple"),
        StyleItem("Balanced", "Clear insights with key explanations", "Balanced"),
        StyleItem("Deep", "Detailed analysis with deeper reasoning", "Deep")
    )

    private fun getDefaultFrequencies(): List<LabelValueItem> = listOf(
        LabelValueItem("Daily highlights", "1"),
        LabelValueItem("Every 7 days", "7"),
        LabelValueItem("Every 14 days", "14"),
        LabelValueItem("Every 21 days", "21")
    )

    private fun getDefaultFocusAreas(): List<String> = listOf("Recovery", "Stress", "Sleep", "Activity")

    private fun getDefaultTriggers(): List<LabelValueItem> = listOf(
        LabelValueItem("In-App Notification", "inApp"),
        LabelValueItem("Emails", "email"),
        LabelValueItem("Both", "both")
    )

    override fun onDestroyView() {
        savingInsightsDialog?.dismiss()
        savingInsightsDialog = null
        super.onDestroyView()
    }
}
