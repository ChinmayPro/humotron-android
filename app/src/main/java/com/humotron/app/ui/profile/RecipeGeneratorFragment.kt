package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentRecipeGeneratorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeGeneratorFragment : BaseFragment(R.layout.fragment_recipe_generator) {

    private lateinit var binding: FragmentRecipeGeneratorBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecipeGeneratorBinding.bind(view)

        // Header Title
        binding.header.title.text = "Recipe Generator"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // How simple should recipes be? (Super Easy is selected)
        setupStyleChip(binding.chipSimpleEasy.root, binding.chipSimpleEasy.tvChipTitle, binding.chipSimpleEasy.tvChipDesc, "Easy", "Simple to prepare", false)
        setupStyleChip(binding.chipSimpleMedium.root, binding.chipSimpleMedium.tvChipTitle, binding.chipSimpleMedium.tvChipDesc, "Medium", "Balanced effort", false)
        setupStyleChip(binding.chipSimpleModerate.root, binding.chipSimpleModerate.tvChipTitle, binding.chipSimpleModerate.tvChipDesc, "Moderate", "Moderate effort", false)
        setupStyleChip(binding.chipSimpleSuperEasy.root, binding.chipSimpleSuperEasy.tvChipTitle, binding.chipSimpleSuperEasy.tvChipDesc, "Super Easy", "Quick & effortless", true)

        // How much time do you have? (20+ min is selected)
        setupChip(binding.chipTime5.root, binding.chipTime5.tvChipTitle, "5 min", false)
        setupChip(binding.chipTime10.root, binding.chipTime10.tvChipTitle, "10 min", false)
        setupChip(binding.chipTime15.root, binding.chipTime15.tvChipTitle, "15 min", false)
        setupChip(binding.chipTime20.root, binding.chipTime20.tvChipTitle, "20+ min", true)

        // Dietary preference (Vegan and Vegan Option are selected)
        setupChip(binding.chipDietNone.root, binding.chipDietNone.tvChipTitle, "None", false)
        setupChip(binding.chipDietVegan.root, binding.chipDietVegan.tvChipTitle, "Vegan", true)
        setupChip(binding.chipDietOmnivore.root, binding.chipDietOmnivore.tvChipTitle, "Omnivore", false)
        setupChip(binding.chipDietVegetarian.root, binding.chipDietVegetarian.tvChipTitle, "Vegetarian", false)
        setupChip(binding.chipDietPescatarian.root, binding.chipDietPescatarian.tvChipTitle, "Pescatarian", false)
        setupChip(binding.chipDietVeganOption.root, binding.chipDietVeganOption.tvChipTitle, "Vegan Option", true)

        // Exclude ingredients (Gluten, Gluten if wrap are selected)
        setupChip(binding.chipExclNut.root, binding.chipExclNut.tvChipTitle, "Nut", false)
        setupChip(binding.chipExclNone.root, binding.chipExclNone.tvChipTitle, "None", false)
        setupChip(binding.chipExclDairy.root, binding.chipExclDairy.tvChipTitle, "Dairy", false)
        setupChip(binding.chipExclGluten.root, binding.chipExclGluten.tvChipTitle, "Gluten", true)
        setupChip(binding.chipExclGlutenOption.root, binding.chipExclGlutenOption.tvChipTitle, "Gluten Option", false)
        setupChip(binding.chipExclGlutenWrap.root, binding.chipExclGlutenWrap.tvChipTitle, "Gluten if wrap", true)
        setupChip(binding.chipExclGlutenBread.root, binding.chipExclGlutenBread.tvChipTitle, "Gluten if bread", false)

        // Cuisine (Indian is selected)
        setupChip(binding.chipCuisineAsian.root, binding.chipCuisineAsian.tvChipTitle, "Asian", false)
        setupChip(binding.chipCuisineIndian.root, binding.chipCuisineIndian.tvChipTitle, "Indian", true)
        setupChip(binding.chipCuisineGlobal.root, binding.chipCuisineGlobal.tvChipTitle, "Global", false)
        setupChip(binding.chipCuisineMediterranean.root, binding.chipCuisineMediterranean.tvChipTitle, "Mediterranean", false)

        // Taste preference (Sweet, Savory are selected)
        setupChip(binding.chipTasteLight.root, binding.chipTasteLight.tvChipTitle, "Light", false)
        setupChip(binding.chipTasteSpicy.root, binding.chipTasteSpicy.tvChipTitle, "Spicy", false)
        setupChip(binding.chipTasteSweet.root, binding.chipTasteSweet.tvChipTitle, "Sweet", true)
        setupChip(binding.chipTasteSavory.root, binding.chipTasteSavory.tvChipTitle, "Savory", true)

        binding.btnSaveRecipeGen.setOnClickListener {
            Toast.makeText(context, "Recipe preferences saved", Toast.LENGTH_SHORT).show()
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
