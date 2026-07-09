package com.humotron.app.ui.shop

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentShopBoosterDetailBinding
import com.humotron.app.domain.modal.response.BoosterResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopBoosterDetailFragment : BaseFragment(R.layout.fragment_shop_booster_detail) {

    private lateinit var binding: FragmentShopBoosterDetailBinding
    private val viewModel: ShopToolsViewModel by activityViewModels()
    private var booster: BoosterResponse.Booster? = null

    private var activePurchases = listOf<Purchase>()
    private var playStoreProducts = listOf<ProductDetails>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopBoosterDetailBinding.bind(view)

        booster = arguments?.getParcelable("booster")

        if (booster == null) {
            Toast.makeText(requireContext(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupViews()
        setupObservers()

        viewModel.refreshPurchases()
        booster?.id?.let { boosterId ->
            viewModel.fetchBoosterById(boosterId)
        }
    }

    private fun setupViews() {
        binding.clHeaderCard.title.text = getString(R.string.booster_label)
        binding.clHeaderCard.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.nsvContent) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                systemBars.bottom
            )
            insets
        }

        binding.btnExploreProPlan.setOnClickListener {
            Toast.makeText(requireContext(), "Explore Pro Plan clicked!", Toast.LENGTH_SHORT).show()
        }

        booster?.let { bindBooster(it) }
    }

    private data class BoosterStyle(
        val iconRes: Int,
        val accentColorRes: Int
    )

    private fun getBoosterStyle(boosterId: String?, displayName: String): BoosterStyle {
        val id = boosterId?.lowercase() ?: ""
        val name = displayName.lowercase()
        return when {
            id == "ai" || name.contains("insight") -> {
                BoosterStyle(R.drawable.ic_spark, R.color.booster_accent_lime)
            }
            id == "aichat" || name.contains("chat") -> {
                BoosterStyle(R.drawable.ic_bot, R.color.booster_accent_cool)
            }
            id == "cm" || name.contains("computed") -> {
                BoosterStyle(R.drawable.ic_shop_optimize, R.color.booster_accent_cool)
            }
            id == "ss" || name.contains("suggestion") -> {
                BoosterStyle(R.drawable.ic_target, R.color.booster_accent_good)
            }
            id == "ln" || name.contains("lifestyle") || name.contains("nudge") -> {
                BoosterStyle(R.drawable.ic_shop_scan, R.color.booster_accent_watch)
            }
            id == "md" || name.contains("multiple") || name.contains("device") -> {
                BoosterStyle(R.drawable.ic_shop_device, R.color.booster_accent_cool)
            }
            id == "dt" || name.contains("dependent") || name.contains("track") -> {
                BoosterStyle(R.drawable.ic_shop_tools, R.color.booster_accent_lime)
            }
            id == "rg" || name.contains("recipe") -> {
                BoosterStyle(R.drawable.ic_bowl, R.color.booster_accent_good)
            }
            else -> {
                BoosterStyle(R.drawable.ic_spark, R.color.booster_accent_cool)
            }
        }
    }

    private fun bindBooster(booster: BoosterResponse.Booster) {
        this.booster = booster

        val style = getBoosterStyle(booster.boosterId, booster.displayName)
        val accentColor = requireContext().getColor(style.accentColorRes)

        binding.ivBoosterTagIcon.setImageResource(style.iconRes)
        binding.ivBoosterTagIcon.imageTintList = ColorStateList.valueOf(accentColor)
        binding.tvBoosterTagText.setTextColor(accentColor)
        binding.tvBoosterPrice.setTextColor(accentColor)

        val limeColor = requireContext().getColor(R.color.booster_accent_lime)

        // Set accent colors to what you'll unlock header (always lime green in HTML)
        binding.ivUnlockTitleIcon.imageTintList = ColorStateList.valueOf(limeColor)
        binding.tvUnlockTitleText.setTextColor(limeColor)

        // Dynamically style clUnlockBox background with color-mix 5% bg / 18% border (always lime green in HTML)
        val unlockBg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 16 * resources.displayMetrics.density
            setColor(Color.argb(13, Color.red(limeColor), Color.green(limeColor), Color.blue(limeColor))) // ~5% alpha
            setStroke(
                (1 * resources.displayMetrics.density).toInt(),
                Color.argb(46, Color.red(limeColor), Color.green(limeColor), Color.blue(limeColor)) // ~18% alpha
            )
        }
        binding.clUnlockBox.background = unlockBg

        binding.tvBoosterName.text = booster.displayName
        binding.tvBoosterTagline.text = booster.displayDescription
        binding.tvDescription.text = booster.heroCopy ?: ""

        // Build benefits list dynamically with lime green checkmarks (always lime green in HTML)
        binding.llUnlockBenefitsContainer.removeAllViews()
        val benefitsList = booster.whatUnlock ?: emptyList()
        benefitsList.forEachIndexed { index, benefitText ->
            val row = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.TOP
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (6 * resources.displayMetrics.density).toInt()
                    bottomMargin = (6 * resources.displayMetrics.density).toInt()
                }
                layoutParams = lp
            }

            // Lime Green Checkmark icon
            val checkIcon = androidx.appcompat.widget.AppCompatImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_check_thin)
                imageTintList = ColorStateList.valueOf(limeColor)
                val lp = android.widget.LinearLayout.LayoutParams(
                    (15 * resources.displayMetrics.density).toInt(),
                    (15 * resources.displayMetrics.density).toInt()
                ).apply {
                    marginEnd = (10 * resources.displayMetrics.density).toInt()
                    topMargin = (2 * resources.displayMetrics.density).toInt()
                }
                layoutParams = lp
            }

            val text = androidx.appcompat.widget.AppCompatTextView(requireContext()).apply {
                setTextAppearance(R.style.Text_14x_Manrope_Medium)
                this.text = benefitText
                setTextColor(requireContext().getColor(R.color.booster_text_benefit))
                setLineSpacing(0f, 1.45f)
                val lp = android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                layoutParams = lp
            }

            row.addView(checkIcon)
            row.addView(text)
            binding.llUnlockBenefitsContainer.addView(row)
        }

        // Preview Section visibility and configuration (matches HTML only AI Chat preview card style)
        val isAiChat = booster.boosterId?.lowercase() == "aichat" || booster.displayName.lowercase().contains("chat")
        if (isAiChat) {
            binding.clPreviewMock.visibility = View.VISIBLE
            binding.ivPreviewScreen.visibility = View.GONE

            // Set dynamic bubble bg color tint
            val chatResponseBg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 10 * resources.displayMetrics.density
                setColor(Color.argb(18, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))) // ~7% alpha
            }
            binding.tvChatResponse.background = chatResponseBg
        } else {
            binding.clPreviewMock.visibility = View.GONE
            binding.ivPreviewScreen.visibility = View.GONE
        }

        updatePriceAndCta(booster)
    }

    private fun updatePriceAndCta(booster: BoosterResponse.Booster) {
        val isUnlocked = booster.isSubscribed == true || activePurchases.any { purchase ->
            purchase.products.contains(booster.playStoreProductId)
        }

        val matchedProduct = playStoreProducts.find { it.productId == booster.playStoreProductId }
        val subscriptionOffer = matchedProduct?.subscriptionOfferDetails?.firstOrNull()
        val subscriptionPrice = subscriptionOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        val priceText = subscriptionPrice ?: matchedProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: booster.displayPriceFallback

        binding.tvBoosterPrice.text = priceText
        binding.tvBoosterPeriod.text = booster.priceModel ?: ""

        val isInPro = when (booster.boosterId?.lowercase()) {
            "ai", "aichat", "cm", "ss" -> true
            else -> {
                val name = booster.displayName.lowercase()
                name.contains("insight") || name.contains("chat") || name.contains("computed") || name.contains("suggestion")
            }
        }

        val inProActive = isInPro && isUnlocked

        if (isUnlocked) {
            binding.btnBuyNow.visibility = View.GONE
            binding.layoutOwnedBar.visibility = View.VISIBLE

            if (inProActive) {
                binding.tvOwnedBarText.text = getString(R.string.included_in_pro_plan)
                binding.btnExploreProPlan.visibility = View.VISIBLE
                binding.btnExploreProPlan.text = getString(R.string.explore_pro_plan)
                binding.btnExploreProPlan.setOnClickListener {
                    Toast.makeText(requireContext(), "Explore Pro plan clicked!", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.tvOwnedBarText.text = "${booster.displayName} is active"
                binding.btnExploreProPlan.visibility = View.GONE
            }
        } else {
            binding.layoutOwnedBar.visibility = View.GONE
            binding.btnBuyNow.visibility = View.VISIBLE

            binding.btnBuyNow.text = "Add ${booster.displayName} — $priceText/mo"
            binding.btnBuyNow.isEnabled = true
            binding.btnBuyNow.setOnClickListener {
                val productDetails = viewModel.getProductDetailsForId(booster.playStoreProductId)
                if (productDetails != null) {
                    viewModel.launchBillingFlow(
                        activity = requireActivity(),
                        booster = booster,
                        productDetails = productDetails
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.item_not_available_play_store),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (isInPro) {
                binding.btnExploreProPlan.visibility = View.VISIBLE
                binding.btnExploreProPlan.text = getString(R.string.or_get_in_pro_plan)
                binding.btnExploreProPlan.setOnClickListener {
                    findNavController().navigate(R.id.fragmentShopPlanDetail)
                }
            } else {
                binding.btnExploreProPlan.visibility = View.GONE
            }
        }
    }

    private fun setupObservers() {
        viewModel.boosterDetailLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    toggleLoader(false)
                    resource.data?.data?.booster?.let { fetchedBooster ->
                        bindBooster(fetchedBooster)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    toggleLoader(false)
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.something_went_wrong)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    toggleLoader(true)
                }
            }
        }

        viewModel.activePurchasesLiveData.observe(viewLifecycleOwner) { purchases ->
            activePurchases = purchases
            booster?.let { updatePriceAndCta(it) }
        }

        viewModel.playStoreProductsLiveData.observe(viewLifecycleOwner) { products ->
            playStoreProducts = products
            booster?.let { updatePriceAndCta(it) }
        }

        viewModel.orderResultLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    toggleLoader(false)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.unlocked_success),
                        Toast.LENGTH_LONG
                    ).show()
                    booster?.id?.let { viewModel.fetchBoosterById(it) }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    toggleLoader(false)
                    Toast.makeText(
                        requireContext(),
                        resource.error?.errorMessage ?: getString(R.string.order_activation_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                Status.LOADING -> {
                    toggleLoader(true)
                }
            }
        }

        viewModel.purchaseSuccessEvent.observe(viewLifecycleOwner) { purchase ->
        }

        viewModel.purchaseErrorEvent.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(
                requireContext(),
                getString(R.string.purchase_failed_format, errorMsg),
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.purchaseCancelEvent.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                getString(R.string.transaction_canceled),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleLoader(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.nsvContent.visibility = if (show) View.GONE else View.VISIBLE
        binding.clFooter.visibility = if (show) View.GONE else View.VISIBLE
    }
}
