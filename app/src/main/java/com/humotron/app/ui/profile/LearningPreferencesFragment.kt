package com.humotron.app.ui.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentLearningPreferencesBinding
import com.humotron.app.databinding.ItemLearningCardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearningPreferencesFragment : BaseFragment(R.layout.fragment_learning_preferences) {

    private lateinit var binding: FragmentLearningPreferencesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLearningPreferencesBinding.bind(view)

        // Header Title
        binding.header.title.text = "Learning Preferences"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Card 1: Metabolic Science
        val metabolicCardBinding = ItemLearningCardBinding.bind(binding.cardMetabolic.root)
        setupCard(
            binding = metabolicCardBinding,
            title = "Metabolic Science",
            subCount = "5 of 7 sub-categories",
            iconResId = R.drawable.ic_optimize,
            colorHex = "#C4F23E",
            progressRatio = 5f / 7f,
            chips = listOf("Carbohydrate Metabolism", "Energy & Mitochondria"),
            moreCount = 3
        )

        // Setup Card 2: Brain & Cognitive Enhancement
        val brainCardBinding = ItemLearningCardBinding.bind(binding.cardBrain.root)
        setupCard(
            binding = brainCardBinding,
            title = "Brain & Cognitive Enhancement",
            subCount = "5 of 7 sub-categories",
            iconResId = R.drawable.ic_target,
            colorHex = "#5FB7C4",
            progressRatio = 5f / 7f,
            chips = listOf("Attention Deficit & Focus", "Creativity & Innovation"),
            moreCount = 3
        )

        // Setup Card 3: Environmental & Lifestyle Biohacking
        val envCardBinding = ItemLearningCardBinding.bind(binding.cardEnvironment.root)
        setupCard(
            binding = envCardBinding,
            title = "Environmental & Lifestyle Biohacking",
            subCount = "3 of 6 sub-categories",
            iconResId = R.drawable.ic_onboard_moon,
            colorHex = "#E7A93C",
            progressRatio = 3f / 6f,
            chips = listOf("Light & Circadian", "EMFs & Exposure"),
            moreCount = 1
        )
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
        val color = Color.parseColor(colorHex)
        val transparentColor = Color.parseColor(colorHex.replace("#", "#29")) // approx 16% opacity

        // Top Row Setup
        binding.tvCategory.text = title
        binding.tvSubCount.text = subCount
        binding.ivIcon.setImageResource(iconResId)
        
        binding.ivIcon.imageTintList = ColorStateList.valueOf(color)
        binding.llIconBg.backgroundTintList = ColorStateList.valueOf(transparentColor)
        
        binding.tvChangeText.setTextColor(color)
        binding.ivChangeArrow.imageTintList = ColorStateList.valueOf(color)

        // Progress Bar Setup
        binding.vProgress.setBackgroundColor(color)
        val lpProgress = binding.vProgress.layoutParams as LinearLayout.LayoutParams
        lpProgress.weight = progressRatio
        binding.vProgress.layoutParams = lpProgress

        val lpRemaining = binding.vRemaining.layoutParams as LinearLayout.LayoutParams
        lpRemaining.weight = 1f - progressRatio
        binding.vRemaining.layoutParams = lpRemaining

        // Chips Setup
        binding.flexChips.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        // Selected chips
        chips.forEach { chipText ->
            val tvChip = inflater.inflate(R.layout.item_learning_chip, binding.flexChips, false) as TextView
            tvChip.text = chipText
            tvChip.setTextColor(color)
            
            // Generate rounded stroke background dynamically matching the mockup's 10% bg and 25% border opacity
            val background = GradientDrawable()
            background.shape = GradientDrawable.RECTANGLE
            background.cornerRadius = resources.displayMetrics.density * 12
            val chipBgColor = Color.parseColor(colorHex.replace("#", "#1A")) // 10% opacity
            val chipStrokeColor = Color.parseColor(colorHex.replace("#", "#40")) // 25% opacity
            background.setColor(chipBgColor)
            background.setStroke((resources.displayMetrics.density * 1).toInt(), chipStrokeColor)
            tvChip.background = background
            
            binding.flexChips.addView(tvChip)
        }

        // More chip
        if (moreCount > 0) {
            val tvMore = inflater.inflate(R.layout.item_learning_chip, binding.flexChips, false) as TextView
            tvMore.text = "+$moreCount more"
            tvMore.setTextColor(Color.parseColor("#CDD6D5")) // var(--ink2)
            
            val background = GradientDrawable()
            background.shape = GradientDrawable.RECTANGLE
            background.cornerRadius = resources.displayMetrics.density * 12
            background.setColor(Color.parseColor("#0BFFFFFF")) // 4% white
            background.setStroke((resources.displayMetrics.density * 1).toInt(), Color.parseColor("#15FFFFFF")) // 8% white (var(--hair))
            tvMore.background = background
            
            binding.flexChips.addView(tvMore)
        }
    }
}
