package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentGoalsBinding
import com.humotron.app.domain.modal.response.HealthProfileConfigResponse.HealthItem
import com.humotron.app.ui.profile.dialog.SavingGoalsDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoalsFragment : BaseFragment(R.layout.fragment_goals) {

    private lateinit var binding: FragmentGoalsBinding
    private val viewModel: GoalsViewModel by viewModels()
    private var adapter: GoalAdapter? = null
    private val selectedGoals = mutableSetOf<String>()
    private val initialSelectedGoals = mutableSetOf<String>()
    private var savingGoalsDialog: SavingGoalsDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGoalsBinding.bind(view)

        // Header Title
        binding.header.title.text = "Goals"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Disable Save button initially until selection changes
        updateSaveButtonState(false)

        binding.btnSaveGoals.setOnClickListener {
            showSavingDialogAndTriggerSave()
        }

        initData()
        observeData()
    }

    private fun updateSaveButtonState(enabled: Boolean) {
        binding.btnSaveGoals.isEnabled = enabled
    }

    private fun checkSelectionChanged() {
        val hasChanged = selectedGoals != initialSelectedGoals
        updateSaveButtonState(hasChanged)
    }

    private fun showSavingDialogAndTriggerSave() {
        if (savingGoalsDialog?.isShowing == true) return

        savingGoalsDialog = SavingGoalsDialog(requireContext()) {
            findNavController().navigateUp()
        }
        savingGoalsDialog?.show()

        viewModel.saveGoals(selectedGoals.toList())
    }

    private fun initData() {
        viewModel.fetchHealthProfileConfig()
    }

    private fun observeData() {
        viewModel.getHealthProfileConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = resource.data?.data
                    val prefs = data?.preferences
                    val config = data?.configuration

                    selectedGoals.clear()
                    initialSelectedGoals.clear()

                    prefs?.healthGoals?.let {
                        selectedGoals.addAll(it)
                        initialSelectedGoals.addAll(it)
                    }

                    val healthGoals = config?.healthGoals ?: emptyList()
                    
                    // Also collect default selected items from configuration if any
                    healthGoals.forEach { item ->
                        if (item.isSelected == true && item.name != null) {
                            selectedGoals.add(item.name)
                            initialSelectedGoals.add(item.name)
                        }
                    }

                    setupRecyclerView(healthGoals)
                    updateSaveButtonState(false)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    setupRecyclerView(getDefaultFallbackGoals())
                    updateSaveButtonState(false)
                }

                Status.LOADING -> {
                    if (adapter == null || adapter?.itemCount == 0) {
                        showProgress()
                    }
                }

                else -> {}
            }
        }

        viewModel.getSaveGoalsLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    savingGoalsDialog?.onApiSuccess()
                }

                Status.ERROR, Status.EXCEPTION -> {
                    savingGoalsDialog?.onApiError()
                    val msg = resource.error?.errorMessage ?: resource.data?.message ?: "Failed to save goals"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {
                    // Progress animation handled in SavingGoalsDialog
                }

                else -> {}
            }
        }
    }

    private fun setupRecyclerView(items: List<HealthItem>) {
        if (adapter == null || binding.rvGoals.adapter == null) {
            adapter = GoalAdapter(items, selectedGoals) { _, _ ->
                checkSelectionChanged()
            }
            binding.rvGoals.adapter = adapter
        } else {
            adapter?.updateData(items)
        }
    }

    private fun getDefaultFallbackGoals(): List<HealthItem> {
        return listOf(
            HealthItem(name = "Cardiovascular Health", isSelected = true),
            HealthItem(name = "Foot Comfort", isSelected = true),
            HealthItem(name = "Athletic Performance", isSelected = false),
            HealthItem(name = "Recovery", isSelected = false),
            HealthItem(name = "Sleep Improvement", isSelected = false),
            HealthItem(name = "Stress Reduction", isSelected = false)
        )
    }

    override fun onDestroyView() {
        savingGoalsDialog?.dismiss()
        savingGoalsDialog = null
        adapter = null
        super.onDestroyView()
    }
}
