package com.humotron.app.ui.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentLearningPreferencesBinding
import com.humotron.app.databinding.ItemLearningCardBinding
import com.humotron.app.domain.modal.response.Tag
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearningPreferencesFragment : BaseFragment(R.layout.fragment_learning_preferences) {

    private lateinit var binding: FragmentLearningPreferencesBinding
    private val viewModel: LearningPreferencesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLearningPreferencesBinding.bind(view)

        // Header Title
        binding.header.title.text = "Learning Preferences"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        initData()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchLearningPreferences()
    }

    private fun initData() {
        viewModel.fetchLearningPreferences()
    }

    private fun observeData() {
        viewModel.getLearningPreferencesLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val nuggetsData = resource.data?.nuggetsData
                    val contextTagCategoryData = nuggetsData?.firstOrNull {
                        it.tagType.equals("CONTEXT TAG", ignoreCase = true) && it.tagLevel.equals("CATEGORY", ignoreCase = true)
                    } ?: nuggetsData?.firstOrNull()

                    val allCategoryTags = contextTagCategoryData?.tags ?: emptyList()
                    renderCategories(allCategoryTags)
                    binding.btnRerunMasterSetup.setOnClickListener(null)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }

                else -> {}
            }
        }
    }

    private fun renderCategories(categoryTags: List<Tag>) {
        binding.llCardsContainer.removeAllViews()

        // Filter active/selected categories
        val activeCategories = categoryTags.filter { cat ->
            cat.isSelected == true
        }

        var totalSelectedCategoriesCount = activeCategories.size
        var totalSelectedSubCategoriesCount = 0

        activeCategories.forEachIndexed { index, catTag ->
            val catTitle = catTag.tagName ?: "Category"
            val allPrimaryTags = catTag.primaryTag ?: emptyList()
            val selectedPrimaryTags = allPrimaryTags.filter { it.isSelected == true }
            val selectedCount = selectedPrimaryTags.size
            val totalCount = allPrimaryTags.size

            totalSelectedSubCategoriesCount += selectedCount

            val colorHex = getColorHexForCategory(catTitle, index)
            val iconResId = getIconResForCategory(catTitle)

            val cardBinding = ItemLearningCardBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.llCardsContainer,
                false
            )

            val progressRatio = if (totalCount > 0) selectedCount.toFloat() / totalCount.toFloat() else 0f
            val displayedChips = selectedPrimaryTags.take(4).mapNotNull { it.tagName }
            val moreCount = if (selectedCount > 4) selectedCount - 4 else 0

            setupCard(
                binding = cardBinding,
                title = catTitle,
                subCount = "$selectedCount of $totalCount sub-categories",
                iconResId = iconResId,
                colorHex = colorHex,
                progressRatio = progressRatio,
                chips = displayedChips,
                moreCount = moreCount
            )

            val openEditCategoryScreen = View.OnClickListener {
                val bundle = Bundle().apply {
                    putParcelable(EditCategoryPreferencesFragment.ARG_TARGET_CATEGORY, catTag)
                    putParcelableArrayList(EditCategoryPreferencesFragment.ARG_ALL_CATEGORIES, ArrayList(categoryTags))
                    putInt(EditCategoryPreferencesFragment.ARG_CATEGORY_INDEX, index)
                }
                findNavController().navigate(
                    R.id.action_fragmentLearningPreferences_to_fragmentEditCategoryPreferences,
                    bundle
                )
            }
            cardBinding.btnChange.setOnClickListener(openEditCategoryScreen)
            cardBinding.root.setOnClickListener(openEditCategoryScreen)

            binding.llCardsContainer.addView(cardBinding.root)
        }

        // Fallback count if no active categories found
        if (totalSelectedCategoriesCount == 0) {
            totalSelectedCategoriesCount = 3
            totalSelectedSubCategoriesCount = 12
        }

        binding.tvSubtitle.text = "$totalSelectedCategoriesCount categories, $totalSelectedSubCategoriesCount sub-categories. Your books, nuggets and tests are catalogued from these — view and tune what they're drawn from."
    }

    private fun setupCard(
        binding: ItemLearningCardBinding,
        title: String,
        subCount: String,
        iconResId: Int,
        colorHex: String,
        progressRatio: Float,
        chips: List<String>,
        moreCount: Int
    ) {
        val limeColor = Color.parseColor("#C4F23E")
        val categoryColor = Color.parseColor(colorHex)
        val transparentCategoryColor = Color.parseColor(colorHex.replace("#", "#29")) // approx 16% opacity

        // Top Row Setup
        binding.tvCategory.text = title
        binding.tvSubCount.text = subCount
        binding.ivIcon.setImageResource(iconResId)

        // Icon & Icon BG use Category-specific color
        binding.ivIcon.imageTintList = ColorStateList.valueOf(categoryColor)
        binding.llIconBg.backgroundTintList = ColorStateList.valueOf(transparentCategoryColor)

        // Change > text and arrow ALWAYS use Lime Green (#C4F23E)
        binding.tvChangeText.setTextColor(limeColor)
        binding.ivChangeArrow.imageTintList = ColorStateList.valueOf(limeColor)

        // Progress Bar Line uses Category-specific color
        binding.vProgress.setBackgroundColor(categoryColor)
        val lpProgress = binding.vProgress.layoutParams as LinearLayout.LayoutParams
        lpProgress.weight = progressRatio.coerceAtLeast(0.02f)
        binding.vProgress.layoutParams = lpProgress

        val lpRemaining = binding.vRemaining.layoutParams as LinearLayout.LayoutParams
        lpRemaining.weight = (1f - progressRatio).coerceAtLeast(0f)
        binding.vRemaining.layoutParams = lpRemaining

        // Tag Chips ALWAYS use Lime Green (#C4F23E)
        binding.flexChips.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        // Selected chips
        chips.forEach { chipText ->
            val tvChip = inflater.inflate(R.layout.item_learning_chip, binding.flexChips, false) as TextView
            tvChip.text = chipText
            tvChip.setTextColor(limeColor)

            val background = GradientDrawable()
            background.shape = GradientDrawable.RECTANGLE
            background.cornerRadius = resources.displayMetrics.density * 12
            val chipBgColor = Color.parseColor("#1AC4F23E") // 10% lime opacity
            val chipStrokeColor = Color.parseColor("#40C4F23E") // 25% lime opacity
            background.setColor(chipBgColor)
            background.setStroke((resources.displayMetrics.density * 1).toInt(), chipStrokeColor)
            tvChip.background = background

            binding.flexChips.addView(tvChip)
        }

        // More chip
        if (moreCount > 0) {
            val tvMore = inflater.inflate(R.layout.item_learning_chip, binding.flexChips, false) as TextView
            tvMore.text = "+$moreCount more"
            tvMore.setTextColor(Color.parseColor("#CDD6D5"))

            val background = GradientDrawable()
            background.shape = GradientDrawable.RECTANGLE
            background.cornerRadius = resources.displayMetrics.density * 12
            background.setColor(Color.parseColor("#0BFFFFFF")) // 4% white
            background.setStroke((resources.displayMetrics.density * 1).toInt(), Color.parseColor("#15FFFFFF"))
            tvMore.background = background

            binding.flexChips.addView(tvMore)
        }
    }

    private fun getColorHexForCategory(categoryName: String, index: Int): String {
        return when {
            categoryName.contains("Metabolic", ignoreCase = true) -> "#C4F23E"
            categoryName.contains("Brain", ignoreCase = true) || categoryName.contains("Cognitive", ignoreCase = true) -> "#5FB7C4"
            categoryName.contains("Environmental", ignoreCase = true) || categoryName.contains("Lifestyle", ignoreCase = true) -> "#E7A93C"
            else -> {
                val palette = listOf("#C4F23E", "#5FB7C4", "#E7A93C", "#9E7BFF", "#F25F5F")
                palette[index % palette.size]
            }
        }
    }

    private fun getIconResForCategory(categoryName: String): Int {
        return when {
            categoryName.contains("Metabolic", ignoreCase = true) -> R.drawable.ic_optimize
            categoryName.contains("Brain", ignoreCase = true) || categoryName.contains("Cognitive", ignoreCase = true) -> R.drawable.ic_target
            categoryName.contains("Environmental", ignoreCase = true) || categoryName.contains("Lifestyle", ignoreCase = true) -> R.drawable.ic_onboard_moon
            else -> R.drawable.ic_target
        }
    }
}
