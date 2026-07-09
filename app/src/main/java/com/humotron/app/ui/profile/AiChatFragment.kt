package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentAiChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiChatFragment : BaseFragment(R.layout.fragment_ai_chat) {

    private lateinit var binding: FragmentAiChatBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAiChatBinding.bind(view)

        // Header Title
        binding.header.title.text = "AI Chat"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Response style
        setupStyleChip(binding.chipStyleDirect.root, binding.chipStyleDirect.tvChipTitle, binding.chipStyleDirect.tvChipDesc, "Direct", "Short, clear answers", false)
        setupStyleChip(binding.chipStyleBalanced.root, binding.chipStyleBalanced.tvChipTitle, binding.chipStyleBalanced.tvChipDesc, "Balanced", "Some context", false)
        setupStyleChip(binding.chipStyleDeep.root, binding.chipStyleDeep.tvChipTitle, binding.chipStyleDeep.tvChipDesc, "Deep", "Deeper explanations", true)

        // Focus
        setupStyleChip(binding.chipFocusGuidance.root, binding.chipFocusGuidance.tvChipTitle, binding.chipFocusGuidance.tvChipDesc, "Guidance", "What to do next", false)
        setupStyleChip(binding.chipFocusBoth.root, binding.chipFocusBoth.tvChipTitle, binding.chipFocusBoth.tvChipDesc, "Both", "Next steps + patterns", false)
        setupStyleChip(binding.chipFocusUnderstanding.root, binding.chipFocusUnderstanding.tvChipTitle, binding.chipFocusUnderstanding.tvChipDesc, "Understanding", "Explain patterns", true)

        // Context (Switches)
        binding.switchGuidance.tvSwitchTitle.text = "Guidance"
        binding.switchGuidance.tvSwitchDesc.text = "Focus on what to do next"
        binding.switchGuidance.switchToggle.isChecked = true

        binding.switchPatterns.tvSwitchTitle.text = "Patterns"
        binding.switchPatterns.tvSwitchDesc.text = "Explain the patterns behind the numbers"
        binding.switchPatterns.switchToggle.isChecked = false

        // Chat history
        setupChip(binding.chipHistory60.root, binding.chipHistory60.tvChipTitle, "60 days", false)
        setupChip(binding.chipHistory90.root, binding.chipHistory90.tvChipTitle, "90 days", true)
        setupChip(binding.chipHistory180.root, binding.chipHistory180.tvChipTitle, "180 days", false)

        binding.btnSaveAiChat.setOnClickListener {
            Toast.makeText(context, "AI Chat settings saved", Toast.LENGTH_SHORT).show()
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
