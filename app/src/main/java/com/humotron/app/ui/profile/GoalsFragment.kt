package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentGoalsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalsFragment : BaseFragment(R.layout.fragment_goals) {

    private lateinit var binding: FragmentGoalsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGoalsBinding.bind(view)

        // Header Title
        binding.header.title.text = "Goals"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Default checked state array (mocking goal selection)
        val selectedGoals = mutableSetOf("Recovery")

        // Setup individual items based on HTML mockup data with colored status
        setupItem(binding.goalSbp, "Systolic BP", "128 mmHg · <font color=\"#7BD88F\">improving</font> · 2h ago", selectedGoals.contains("Systolic BP"))
        setupItem(binding.goalHrv, "HRV", "48 ms · <font color=\"#7BD88F\">improving</font> · today", selectedGoals.contains("HRV"))
        setupItem(binding.goalRecovery, "Recovery", "65 % · <font color=\"#7BD88F\">improving</font> · today", selectedGoals.contains("Recovery"))
        setupItem(binding.goalAsym, "Walking Asymmetry", "1.4 % · <font color=\"#7BD88F\">improving</font> · 17 Jun", selectedGoals.contains("Walking Asymmetry"))

        binding.btnSaveGoals.setOnClickListener {
            Toast.makeText(context, "Goals saved", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupItem(
        itemBinding: com.humotron.app.databinding.ItemGoalBinding,
        title: String,
        desc: String,
        isSelected: Boolean
    ) {
        itemBinding.tvGoalName.text = title
        itemBinding.tvGoalDesc.text = android.text.Html.fromHtml(desc, android.text.Html.FROM_HTML_MODE_LEGACY)

        updateSelectionState(itemBinding, isSelected)

        itemBinding.root.setOnClickListener {
            val currentlySelected = itemBinding.ivCheckbox.tag as? Boolean ?: false
            updateSelectionState(itemBinding, !currentlySelected)
        }
    }

    private fun updateSelectionState(
        itemBinding: com.humotron.app.databinding.ItemGoalBinding,
        isSelected: Boolean
    ) {
        itemBinding.ivCheckbox.tag = isSelected
        if (isSelected) {
            itemBinding.ivCheckbox.setImageResource(R.drawable.bg_checkbox_checked_mockup)
            itemBinding.ivCheckbox.imageTintList = null // Use natural colors from mockup checked checkbox drawable
            itemBinding.ivGoalIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C4F23E"))
            itemBinding.llDeviceIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#24C4F23E")) // 14% opacity lime
        } else {
            itemBinding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_unselected)
            itemBinding.ivCheckbox.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4DFFFFFF")) // Translucent white border
            itemBinding.ivGoalIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8EA09E"))
            itemBinding.llDeviceIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0DFFFFFF")) // 5% opacity white
        }
    }
}
