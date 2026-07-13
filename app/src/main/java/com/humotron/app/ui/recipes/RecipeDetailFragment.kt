package com.humotron.app.ui.recipes

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.humotron.app.R
import com.humotron.app.databinding.FragmentRecipeDetailBinding
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipesViewModel by activityViewModels()
    private var recipeId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeId = arguments?.getString("recipeId").orEmpty()

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnLike.setOnClickListener {
            viewModel.toggleFavorite(recipeId)
        }

        binding.btnLogMeal.setOnClickListener {
            viewModel.logRecipe(recipeId)
        }
    }

    private fun observeViewModel() {
        viewModel.recipeCategories.observe(viewLifecycleOwner) {
            val recipe = viewModel.getRecipeById(recipeId)
            if (recipe != null) {
                bindRecipeData(recipe)
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun bindRecipeData(recipe: RecipeCard) {
        // Favorite status
        if (recipe.isFavorite) {
            binding.btnLike.setImageResource(R.drawable.ic_fav_selected)
            binding.btnLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.console_danger_red))
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_fav)
            binding.btnLike.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorWhite))
        }

        // Hero Image
        Glide.with(this)
            .load(recipe.imageUrl)
            .into(binding.ivHeroImage)

        // Metric focus pill
        val metric = viewModel.metricCapsules.value?.find { it.title.equals(recipe.metricPillText, ignoreCase = true) }
        val isUpward = metric?.isUpwardTrend ?: true
        val arrowSymbol = if (isUpward) "↑" else "↓"
        val fullText = "$arrowSymbol  ${recipe.metricPillText}"
        val spannable = SpannableString(fullText)

        val isImproving = metric?.isImproving ?: true
        val arrowColor = if (isImproving) {
            ContextCompat.getColor(requireContext(), R.color.insights_green)
        } else {
            ContextCompat.getColor(requireContext(), R.color.console_danger_red)
        }

        spannable.setSpan(
            ForegroundColorSpan(arrowColor),
            0,
            arrowSymbol.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvMetricPill.text = spannable

        // Title and meta
        binding.tvRecipeName.text = recipe.title
        binding.tvRecipeMeta.text = "${recipe.difficulty} · ${recipe.kcalPerServing} kcal per serving"

        // Macros Row
        binding.llMacros.removeAllViews()
        recipe.macros.forEach { (value, title) ->
            val macroView = layoutInflater.inflate(R.layout.item_recipe_macro, binding.llMacros, false)
            macroView.findViewById<TextView>(R.id.tvValue).text = value
            macroView.findViewById<TextView>(R.id.tvTitle).text = title
            binding.llMacros.addView(macroView)
        }

        // Meta Grid
        binding.glMetaGrid.removeAllViews()
        recipe.meta.forEach { (value, title) ->
            val metaView = layoutInflater.inflate(R.layout.item_recipe_meta, binding.glMetaGrid, false)
            metaView.findViewById<TextView>(R.id.tvValue).text = value
            metaView.findViewById<TextView>(R.id.tvTitle).text = title

            // Set layout parameters for 2 columns grid cell
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            metaView.layoutParams = params
            binding.glMetaGrid.addView(metaView)
        }

        // Tags Flexbox
        binding.fbTags.removeAllViews()
        recipe.tags.forEach { tag ->
            val tagView = layoutInflater.inflate(R.layout.view_recipe_tag_chip, binding.fbTags, false)
            tagView.findViewById<TextView>(R.id.tvTag).text = tag
            binding.fbTags.addView(tagView)
        }

        // Ingredients
        val ingredientsAdapter = IngredientAdapter()
        binding.rvIngredients.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvIngredients.adapter = ingredientsAdapter

        // Mock rich ingredients details mapping
        val detailedIngredients = getDetailedIngredients(recipe.id, recipe.ingredients)
        ingredientsAdapter.submitList(detailedIngredients)

        // Method Steps
        binding.llMethodSteps.removeAllViews()
        recipe.steps.forEachIndexed { index, step ->
            val stepView = layoutInflater.inflate(R.layout.item_recipe_step_detail, binding.llMethodSteps, false)
            stepView.findViewById<TextView>(R.id.tvStepNumber).text = (index + 1).toString()
            stepView.findViewById<TextView>(R.id.tvStepText).text = step
            binding.llMethodSteps.addView(stepView)
        }

        // Why This Meal benefits
        binding.llBenefits.removeAllViews()
        recipe.benefits.forEach { (title, description) ->
            val benefitView = layoutInflater.inflate(R.layout.item_recipe_benefit_detail, binding.llBenefits, false)
            benefitView.findViewById<TextView>(R.id.tvBenefitTitle).text = title
            benefitView.findViewById<TextView>(R.id.tvBenefitDescription).text = description
            binding.llBenefits.addView(benefitView)
        }

        // Track impact guidance
        binding.tvTrackGuide.text = "You've logged this meal ${recipe.logsCount}×. Keep replacing your dinner with it to see your ${recipe.metricPillText.lowercase()} respond."

        // Streak circles
        binding.llStreak.removeAllViews()
        val density = resources.displayMetrics.density
        val size = (32 * density).toInt()
        val margin = (4 * density).toInt()

        for (i in 1..5) {
            val textView = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                gravity = android.view.Gravity.CENTER
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                typeface = android.graphics.Typeface.create("sans-serif-bold", android.graphics.Typeface.BOLD)

                if (i <= recipe.logsCount) {
                    setBackgroundResource(R.drawable.bg_recipe_detail_streak_circle_done)
                    text = "✓"
                    setTextColor(ContextCompat.getColor(context, R.color.insights_green))
                } else {
                    setBackgroundResource(R.drawable.bg_recipe_detail_streak_circle_todo)
                    text = i.toString()
                    setTextColor(ContextCompat.getColor(context, R.color.textColorMuted))
                }
            }
            binding.llStreak.addView(textView)
        }
    }

    private fun getDetailedIngredients(recipeId: String, fallbackNames: List<String>): List<Triple<String, String, String>> {
        return when (recipeId) {
            "r1" -> listOf(
                Triple("Berries", "50g", "focus"),
                Triple("Yoghurt", "150g", "flask"),
                Triple("Chia", "1 tbsp", "spark")
            )
            "r2" -> listOf(
                Triple("Spinach", "40g", "focus"),
                Triple("Almonds", "15g", "spark"),
                Triple("Flaxseed", "1 tbsp", "droplet")
            )
            "r3" -> listOf(
                Triple("Quinoa", "80g", "spark"),
                Triple("Broccoli", "50g", "focus"),
                Triple("Carrots", "40g", "flask")
            )
            "d1" -> listOf(
                Triple("Salmon", "120g", "flask"),
                Triple("Lettuce", "50g", "focus"),
                Triple("Lemon", "1 slice", "droplet")
            )
            "d2" -> listOf(
                Triple("Tofu", "100g", "flask"),
                Triple("Bok choi", "80g", "focus"),
                Triple("Miso", "1 tbsp", "spark")
            )
            else -> fallbackNames.map { Triple(it, "1 serving", "spark") }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class IngredientAdapter : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {
        private var list: List<Triple<String, String, String>> = emptyList()

        fun submitList(newList: List<Triple<String, String, String>>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recipe_ingredient_detail, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (name, amount, iconName) = list[position]
            holder.tvName.text = name
            holder.tvAmount.text = amount

            val iconRes = when (iconName) {
                "focus" -> R.drawable.ic_target
                "spark" -> R.drawable.ic_spark
                "flask" -> R.drawable.ic_shop_optimize
                "droplet" -> R.drawable.ic_scan_droplet
                else -> R.drawable.ic_spark
            }
            holder.ivIcon.setImageResource(iconRes)

            // Dynamic color tint matching the mockup's circular badge colors
            val colorRes = when (iconName) {
                "focus" -> R.color.insights_green
                "spark" -> R.color.booster_accent_watch
                "flask" -> R.color.booster_accent_cool
                "droplet" -> R.color.booster_accent_good
                else -> R.color.insights_green
            }
            val color = ContextCompat.getColor(holder.itemView.context, colorRes)
            holder.ivIcon.imageTintList = ColorStateList.valueOf(color)

            // Set container circle tint to 10% opacity version of the color
            val bgOpacityColor = androidx.core.graphics.ColorUtils.setAlphaComponent(color, 26) // ~10%
            holder.ivIconContainer.backgroundTintList = ColorStateList.valueOf(bgOpacityColor)
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
            val ivIconContainer: View = itemView.findViewById(R.id.ivIconContainer)
            val tvName: TextView = itemView.findViewById(R.id.tvName)
            val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        }
    }
}
