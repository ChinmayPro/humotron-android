package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentInsightsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsightsFragment : BaseFragment(R.layout.fragment_insights) {

    private lateinit var binding: FragmentInsightsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInsightsBinding.bind(view)

        // Header Title
        binding.header.title.text = "Insights"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup individual items based on HTML mockup data
        
        // Insight style
        setupStyleChip(binding.chipStyleSimple.root, binding.chipStyleSimple.tvChipTitle, binding.chipStyleSimple.tvChipDesc, "Simple", "Quick, easy summaries", true)
        setupStyleChip(binding.chipStyleBalanced.root, binding.chipStyleBalanced.tvChipTitle, binding.chipStyleBalanced.tvChipDesc, "Balanced", "Key explanations", false)
        setupStyleChip(binding.chipStyleDeep.root, binding.chipStyleDeep.tvChipTitle, binding.chipStyleDeep.tvChipDesc, "Deep", "Deeper reasoning", false)

        // Frequency
        setupChip(binding.chipFreqDaily.root, binding.chipFreqDaily.tvChipTitle, "Daily highlights", false)
        setupChip(binding.chipFreq7.root, binding.chipFreq7.tvChipTitle, "Every 7 days", true)
        setupChip(binding.chipFreq14.root, binding.chipFreq14.tvChipTitle, "Every 14 days", false)
        setupChip(binding.chipFreq21.root, binding.chipFreq21.tvChipTitle, "Every 21 days", false)

        // Focus areas
        setupChip(binding.chipFocusRecovery.root, binding.chipFocusRecovery.tvChipTitle, "Recovery", false)
        setupChip(binding.chipFocusStress.root, binding.chipFocusStress.tvChipTitle, "Stress", true)
        setupChip(binding.chipFocusSleep.root, binding.chipFocusSleep.tvChipTitle, "Sleep", true)
        setupChip(binding.chipFocusActivity.root, binding.chipFocusActivity.tvChipTitle, "Activity", false)

        // Triggers
        setupChip(binding.chipTriggerApp.root, binding.chipTriggerApp.tvChipTitle, "In-App Notification", false)
        setupChip(binding.chipTriggerEmails.root, binding.chipTriggerEmails.tvChipTitle, "Emails", false)
        setupChip(binding.chipTriggerBoth.root, binding.chipTriggerBoth.tvChipTitle, "Both", true)

        binding.btnSaveInsights.setOnClickListener {
            Toast.makeText(context, "Insights saved", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupChip(
        root: View,
        titleView: androidx.appcompat.widget.AppCompatTextView,
        title: String,
        isSelected: Boolean
    ) {
        titleView.text = title
        updateChipState(root, titleView, null, isSelected)

        root.setOnClickListener {
            val currentlySelected = root.tag as? Boolean ?: false
            updateChipState(root, titleView, null, !currentlySelected)
        }
    }

    private fun setupStyleChip(
        root: View,
        titleView: androidx.appcompat.widget.AppCompatTextView,
        descView: androidx.appcompat.widget.AppCompatTextView,
        title: String,
        desc: String,
        isSelected: Boolean
    ) {
        titleView.text = title
        descView.text = desc
        updateChipState(root, titleView, descView, isSelected)

        root.setOnClickListener {
            val currentlySelected = root.tag as? Boolean ?: false
            updateChipState(root, titleView, descView, !currentlySelected)
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
}
