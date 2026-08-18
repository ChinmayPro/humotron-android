package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentRecipeGeneratorBinding
import com.humotron.app.domain.modal.param.RecipeConfigRequest
import com.humotron.app.ui.profile.dialog.SavingRecipeDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeGeneratorFragment : BaseFragment(R.layout.fragment_recipe_generator) {

    private lateinit var binding: FragmentRecipeGeneratorBinding
    private val viewModel: RecipeGeneratorViewModel by viewModels()
    private var savingRecipeDialog: SavingRecipeDialog? = null

    // Sets tracking current chip selections for each preference category
    private val selectedComplexity = mutableSetOf<String>("Super Easy")
    private val selectedCookingTime = mutableSetOf<String>("20+ min")
    private val selectedDietaryPreference = mutableSetOf<String>("Vegan", "Vegan Option")
    private val selectedExcludeIngredients = mutableSetOf<String>("Gluten", "Gluten if wrap")
    private val selectedCuisine = mutableSetOf<String>("Indian")
    private val selectedTaste = mutableSetOf<String>("Sweet", "Savory")

    // Baseline tracking for save button enable/disable
    private val initialComplexity = mutableSetOf<String>()
    private val initialCookingTime = mutableSetOf<String>()
    private val initialDietaryPreference = mutableSetOf<String>()
    private val initialExcludeIngredients = mutableSetOf<String>()
    private val initialCuisine = mutableSetOf<String>()
    private val initialTaste = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecipeGeneratorBinding.bind(view)

        // Header Title
        binding.header.title.text = "Recipe Generator"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        updateSaveButtonState(false)

        binding.btnSaveRecipeGen.setOnClickListener {
            savePreferences()
        }

        renderAllChips()
        initData()
        observeData()
    }

    private fun updateSaveButtonState(enabled: Boolean) {
        binding.btnSaveRecipeGen.isEnabled = enabled
    }

    private fun checkSelectionChanged() {
        val hasChanged = selectedComplexity != initialComplexity ||
                selectedCookingTime != initialCookingTime ||
                selectedDietaryPreference != initialDietaryPreference ||
                selectedExcludeIngredients != initialExcludeIngredients ||
                selectedCuisine != initialCuisine ||
                selectedTaste != initialTaste
        updateSaveButtonState(hasChanged)
    }

    private fun captureBaseline() {
        initialComplexity.clear()
        initialComplexity.addAll(selectedComplexity)

        initialCookingTime.clear()
        initialCookingTime.addAll(selectedCookingTime)

        initialDietaryPreference.clear()
        initialDietaryPreference.addAll(selectedDietaryPreference)

        initialExcludeIngredients.clear()
        initialExcludeIngredients.addAll(selectedExcludeIngredients)

        initialCuisine.clear()
        initialCuisine.addAll(selectedCuisine)

        initialTaste.clear()
        initialTaste.addAll(selectedTaste)

        updateSaveButtonState(false)
    }

    private fun initData() {
        viewModel.fetchRecipeConfig()
    }

    private fun observeData() {
        viewModel.getRecipeConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val prefs = resource.data?.data?.preferences
                    prefs?.let {
                        it.complexity?.let { list ->
                            selectedComplexity.clear()
                            selectedComplexity.addAll(list)
                        }
                        it.cookingTime?.let { list ->
                            selectedCookingTime.clear()
                            selectedCookingTime.addAll(list)
                        }
                        it.dietaryPreference?.let { list ->
                            selectedDietaryPreference.clear()
                            selectedDietaryPreference.addAll(list)
                        }
                        it.excludeIngredients?.let { list ->
                            selectedExcludeIngredients.clear()
                            selectedExcludeIngredients.addAll(list)
                        }
                        it.cuisine?.let { list ->
                            selectedCuisine.clear()
                            selectedCuisine.addAll(list)
                        }
                        it.taste?.let { list ->
                            selectedTaste.clear()
                            selectedTaste.addAll(list)
                        }
                    }
                    captureBaseline()
                    renderAllChips()
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    captureBaseline()
                    renderAllChips()
                }

                Status.LOADING -> {
                    showProgress()
                }

                else -> {}
            }
        }

        viewModel.getSaveRecipeLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    savingRecipeDialog?.onApiSuccess()
                }

                Status.ERROR, Status.EXCEPTION -> {
                    savingRecipeDialog?.onApiError()
                    val msg = resource.error?.errorMessage ?: resource.data?.message ?: "Failed to save recipe preferences"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {}

                else -> {}
            }
        }
    }

    private fun savePreferences() {
        if (savingRecipeDialog?.isShowing == true) return

        savingRecipeDialog = SavingRecipeDialog(requireContext()) {
            findNavController().navigateUp()
        }
        savingRecipeDialog?.show()

        val param = RecipeConfigRequest(
            preferences = RecipeConfigRequest.Preferences(
                complexity = selectedComplexity.toList(),
                cookingTime = selectedCookingTime.toList(),
                dietaryPreference = selectedDietaryPreference.toList(),
                excludeIngredients = selectedExcludeIngredients.toList(),
                cuisine = selectedCuisine.toList(),
                taste = selectedTaste.toList()
            )
        )
        viewModel.saveRecipeConfig(param)
    }

    private fun renderAllChips() {
        // How simple should recipes be? (Single Selection)
        bindSingleSelectStyleChip(binding.chipSimpleEasy.root, binding.chipSimpleEasy.tvChipTitle, binding.chipSimpleEasy.tvChipDesc, "Easy", "Simple to prepare", selectedComplexity)
        bindSingleSelectStyleChip(binding.chipSimpleMedium.root, binding.chipSimpleMedium.tvChipTitle, binding.chipSimpleMedium.tvChipDesc, "Medium", "Balanced effort", selectedComplexity)
        bindSingleSelectStyleChip(binding.chipSimpleModerate.root, binding.chipSimpleModerate.tvChipTitle, binding.chipSimpleModerate.tvChipDesc, "Moderate", "Moderate effort", selectedComplexity)
        bindSingleSelectStyleChip(binding.chipSimpleSuperEasy.root, binding.chipSimpleSuperEasy.tvChipTitle, binding.chipSimpleSuperEasy.tvChipDesc, "Super Easy", "Quick & effortless", selectedComplexity)

        // How much time do you have? (Single Selection)
        bindSingleSelectChip(binding.chipTime5.root, binding.chipTime5.tvChipTitle, "5 min", selectedCookingTime)
        bindSingleSelectChip(binding.chipTime10.root, binding.chipTime10.tvChipTitle, "10 min", selectedCookingTime)
        bindSingleSelectChip(binding.chipTime15.root, binding.chipTime15.tvChipTitle, "15 min", selectedCookingTime)
        bindSingleSelectChip(binding.chipTime20.root, binding.chipTime20.tvChipTitle, "20+ min", selectedCookingTime)

        // Dietary preference (Multi Selection)
        bindMultiSelectChip(binding.chipDietNone.root, binding.chipDietNone.tvChipTitle, "None", selectedDietaryPreference)
        bindMultiSelectChip(binding.chipDietVegan.root, binding.chipDietVegan.tvChipTitle, "Vegan", selectedDietaryPreference)
        bindMultiSelectChip(binding.chipDietOmnivore.root, binding.chipDietOmnivore.tvChipTitle, "Omnivore", selectedDietaryPreference)
        bindMultiSelectChip(binding.chipDietVegetarian.root, binding.chipDietVegetarian.tvChipTitle, "Vegetarian", selectedDietaryPreference)
        bindMultiSelectChip(binding.chipDietPescatarian.root, binding.chipDietPescatarian.tvChipTitle, "Pescatarian", selectedDietaryPreference)
        bindMultiSelectChip(binding.chipDietVeganOption.root, binding.chipDietVeganOption.tvChipTitle, "Vegan Option", selectedDietaryPreference)

        // Exclude ingredients (Multi Selection)
        bindMultiSelectChip(binding.chipExclNut.root, binding.chipExclNut.tvChipTitle, "Nut", selectedExcludeIngredients)
        bindMultiSelectChip(binding.chipExclNone.root, binding.chipExclNone.tvChipTitle, "None", selectedExcludeIngredients)
        bindMultiSelectChip(binding.chipExclDairy.root, binding.chipExclDairy.tvChipTitle, "Dairy", selectedExcludeIngredients)
        bindMultiSelectChip(binding.chipExclGluten.root, binding.chipExclGluten.tvChipTitle, "Gluten", selectedExcludeIngredients)
        bindMultiSelectChip(binding.chipExclGlutenOption.root, binding.chipExclGlutenOption.tvChipTitle, "Gluten Option", selectedExcludeIngredients)
        bindMultiSelectChip(binding.chipExclGlutenWrap.root, binding.chipExclGlutenWrap.tvChipTitle, "Gluten if wrap", selectedExcludeIngredients)
        bindMultiSelectChip(binding.chipExclGlutenBread.root, binding.chipExclGlutenBread.tvChipTitle, "Gluten if bread", selectedExcludeIngredients)

        // Cuisine (Multi Selection)
        bindMultiSelectChip(binding.chipCuisineAsian.root, binding.chipCuisineAsian.tvChipTitle, "Asian", selectedCuisine)
        bindMultiSelectChip(binding.chipCuisineIndian.root, binding.chipCuisineIndian.tvChipTitle, "Indian", selectedCuisine)
        bindMultiSelectChip(binding.chipCuisineGlobal.root, binding.chipCuisineGlobal.tvChipTitle, "Global", selectedCuisine)
        bindMultiSelectChip(binding.chipCuisineMediterranean.root, binding.chipCuisineMediterranean.tvChipTitle, "Mediterranean", selectedCuisine)

        // Taste preference (Multi Selection)
        bindMultiSelectChip(binding.chipTasteLight.root, binding.chipTasteLight.tvChipTitle, "Light", selectedTaste)
        bindMultiSelectChip(binding.chipTasteSpicy.root, binding.chipTasteSpicy.tvChipTitle, "Spicy", selectedTaste)
        bindMultiSelectChip(binding.chipTasteSweet.root, binding.chipTasteSweet.tvChipTitle, "Sweet", selectedTaste)
        bindMultiSelectChip(binding.chipTasteSavory.root, binding.chipTasteSavory.tvChipTitle, "Savory", selectedTaste)
    }

    private fun bindSingleSelectChip(
        root: View,
        titleView: AppCompatTextView,
        title: String,
        selectedSet: MutableSet<String>
    ) {
        titleView.text = title
        val isSelected = isTitleSelected(title, selectedSet)
        updateChipState(root, titleView, null, isSelected)

        root.setOnClickListener {
            selectedSet.clear()
            selectedSet.add(title)
            renderAllChips()
            checkSelectionChanged()
        }
    }

    private fun bindSingleSelectStyleChip(
        root: View,
        titleView: AppCompatTextView,
        descView: AppCompatTextView,
        title: String,
        desc: String,
        selectedSet: MutableSet<String>
    ) {
        titleView.text = title
        descView.text = desc
        val isSelected = isTitleSelected(title, selectedSet)
        updateChipState(root, titleView, descView, isSelected)

        root.setOnClickListener {
            selectedSet.clear()
            selectedSet.add(title)
            renderAllChips()
            checkSelectionChanged()
        }
    }

    private fun bindMultiSelectChip(
        root: View,
        titleView: AppCompatTextView,
        title: String,
        selectedSet: MutableSet<String>
    ) {
        titleView.text = title
        val isSelected = isTitleSelected(title, selectedSet)
        updateChipState(root, titleView, null, isSelected)

        root.setOnClickListener {
            val currentlySelected = root.tag as? Boolean ?: false
            val newSelected = !currentlySelected
            if (newSelected) {
                selectedSet.add(title)
            } else {
                removeTitleFromSet(title, selectedSet)
            }
            updateChipState(root, titleView, null, newSelected)
            checkSelectionChanged()
        }
    }

    private fun isTitleSelected(title: String, selectedSet: Set<String>): Boolean {
        return selectedSet.any { item ->
            item.equals(title, ignoreCase = true) ||
                    (title.equals("Nut", ignoreCase = true) && item.equals("Nuts", ignoreCase = true)) ||
                    (title.equals("Nuts", ignoreCase = true) && item.equals("Nut", ignoreCase = true))
        }
    }

    private fun removeTitleFromSet(title: String, selectedSet: MutableSet<String>) {
        selectedSet.removeAll { item ->
            item.equals(title, ignoreCase = true) ||
                    (title.equals("Nut", ignoreCase = true) && item.equals("Nuts", ignoreCase = true)) ||
                    (title.equals("Nuts", ignoreCase = true) && item.equals("Nut", ignoreCase = true))
        }
    }

    private fun updateChipState(
        root: View,
        titleView: AppCompatTextView,
        descView: AppCompatTextView?,
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
        savingRecipeDialog?.dismiss()
        savingRecipeDialog = null
        super.onDestroyView()
    }
}
