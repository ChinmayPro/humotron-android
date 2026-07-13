package com.humotron.app.ui.shop

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopPlanDetailBinding
import com.humotron.app.databinding.ItemShopPlanFeatureBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopPlanDetailFragment : BaseFragment(R.layout.fragment_shop_plan_detail) {

    private lateinit var binding: FragmentShopPlanDetailBinding
    private val viewModel: ShopToolsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopPlanDetailBinding.bind(view)

        setupHeader()
        setupFeatureCards()
        setupCta()
    }

    private fun setupHeader() {
        binding.clHeaderCard.title.text = getString(R.string.plan_title) // "Plan"
        binding.clHeaderCard.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupFeatureCards() {
        val limeColor = requireContext().getColor(R.color.booster_accent_lime)
        val coolColor = requireContext().getColor(R.color.booster_accent_cool)
        val goodColor = requireContext().getColor(R.color.booster_accent_good)

        // 1. AI Insights Card
        val cardInsights = ItemShopPlanFeatureBinding.bind(binding.cardAiInsights.root)
        cardInsights.tvFeatureName.text = "AI Insights"
        cardInsights.tvFeatureDesc.text = "You track your metrics. Let us help explain what they mean, what's changed, and what to do next."
        cardInsights.ivFeatureIcon.setImageResource(R.drawable.ic_spark)
        cardInsights.ivFeatureIcon.setColorFilter(limeColor, android.graphics.PorterDuff.Mode.SRC_IN)
        cardInsights.flFeatureIconWrapper.background = requireContext().getDrawable(R.drawable.bg_feature_icon_wrapper)
        cardInsights.flFeatureIconWrapper.backgroundTintList = ColorStateList.valueOf(getAlphaColor(limeColor))
        cardInsights.root.setOnClickListener {
            navigateToBoosterDetail("insight")
        }

        // 2. AI Chat Card
        val cardChat = ItemShopPlanFeatureBinding.bind(binding.cardAiChat.root)
        cardChat.tvFeatureName.text = "AI Chat"
        cardChat.tvFeatureDesc.text = "You have weeks of health data — but what's it actually saying? Tron AI helps you ask, and understand, what's behind the numbers."
        cardChat.ivFeatureIcon.setImageResource(R.drawable.ic_bot)
        cardChat.ivFeatureIcon.setColorFilter(coolColor, android.graphics.PorterDuff.Mode.SRC_IN)
        cardChat.flFeatureIconWrapper.background = requireContext().getDrawable(R.drawable.bg_feature_icon_wrapper)
        cardChat.flFeatureIconWrapper.backgroundTintList = ColorStateList.valueOf(getAlphaColor(coolColor))
        cardChat.root.setOnClickListener {
            navigateToBoosterDetail("chat")
        }

        // 3. Computed Metrics Card
        val cardMetrics = ItemShopPlanFeatureBinding.bind(binding.cardComputedMetrics.root)
        cardMetrics.tvFeatureName.text = "Computed Metrics"
        cardMetrics.tvFeatureDesc.text = "Metrics are good. Interpreted metrics are better. This booster unlocks research-backed scores on key metrics."
        cardMetrics.ivFeatureIcon.setImageResource(R.drawable.ic_shop_optimize)
        cardMetrics.ivFeatureIcon.setColorFilter(coolColor, android.graphics.PorterDuff.Mode.SRC_IN)
        cardMetrics.flFeatureIconWrapper.background = requireContext().getDrawable(R.drawable.bg_feature_icon_wrapper)
        cardMetrics.flFeatureIconWrapper.backgroundTintList = ColorStateList.valueOf(getAlphaColor(coolColor))
        cardMetrics.root.setOnClickListener {
            navigateToBoosterDetail("computed")
        }

        // 4. Smart Suggestions Card
        val cardSuggestions = ItemShopPlanFeatureBinding.bind(binding.cardSmartSuggestions.root)
        cardSuggestions.tvFeatureName.text = "Smart Suggestions"
        cardSuggestions.tvFeatureDesc.text = "We turn your metrics into daily nudges — what to eat, when to move, when to slow down."
        cardSuggestions.ivFeatureIcon.setImageResource(R.drawable.ic_target)
        cardSuggestions.ivFeatureIcon.setColorFilter(goodColor, android.graphics.PorterDuff.Mode.SRC_IN)
        cardSuggestions.flFeatureIconWrapper.background = requireContext().getDrawable(R.drawable.bg_feature_icon_wrapper)
        cardSuggestions.flFeatureIconWrapper.backgroundTintList = ColorStateList.valueOf(getAlphaColor(goodColor))
        cardSuggestions.root.setOnClickListener {
            navigateToBoosterDetail("suggestion")
        }
    }

    private fun getAlphaColor(color: Int): Int {
        return android.graphics.Color.argb(
            41, // 16% opacity (exactly matches HTML color-mix of 16%)
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color)
        )
    }

    private fun navigateToBoosterDetail(keyword: String) {
        val boosters = viewModel.boostersLiveData.value?.data ?: emptyList()
        val targetBooster = boosters.find {
            it.boosterId?.lowercase()?.contains(keyword) == true ||
                    it.displayName.lowercase().contains(keyword)
        }

        if (targetBooster != null) {
            val bundle = Bundle().apply {
                putParcelable("booster", targetBooster)
            }
            findNavController().navigate(R.id.fragmentShopBoosterDetail, bundle)
        } else {
            Toast.makeText(requireContext(), "Booster detail not found in tools list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCta() {
        binding.btnBuyNow.setOnClickListener {
            Toast.makeText(requireContext(), "Upgrade to Pro plan clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
