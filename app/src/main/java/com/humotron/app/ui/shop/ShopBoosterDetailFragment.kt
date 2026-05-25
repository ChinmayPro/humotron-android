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
        binding.btnBack.setOnClickListener {
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

    private fun bindBooster(booster: BoosterResponse.Booster) {
        this.booster = booster
        binding.tvBoosterName.text = booster.displayName
        binding.tvBoosterTagline.text = booster.displayDescription

        binding.tvDescription.text = booster.heroCopy ?: ""

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
                    topMargin = if (index == 0) (20 * resources.displayMetrics.density).toInt() else (16 * resources.displayMetrics.density).toInt()
                }
                layoutParams = lp
            }

            val bullet = androidx.appcompat.widget.AppCompatTextView(requireContext()).apply {
                setTextAppearance(R.style.Text_16x_Manrope_SemiBold)
                text = "•"
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = (10 * resources.displayMetrics.density).toInt()
                }
                layoutParams = lp
            }

            val text = androidx.appcompat.widget.AppCompatTextView(requireContext()).apply {
                setTextAppearance(R.style.Text_16x_Manrope_Medium)
                this.text = benefitText
                setLineSpacing(0f, 1.1f)
                val lp = android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                layoutParams = lp
            }

            row.addView(bullet)
            row.addView(text)
            binding.llUnlockBenefitsContainer.addView(row)
        }

        com.bumptech.glide.Glide.with(binding.ivPreviewScreen)
            .load(booster.imageUrl)
            .placeholder(R.drawable.shop_deatils_preview_screen)
            .error(R.drawable.shop_deatils_preview_screen)
            .into(binding.ivPreviewScreen)

        binding.tvPreviewTitle.text = booster.bbcTitle ?: ""
        binding.tvPreviewDesc.text = booster.bbcDescription ?: ""

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

        if (isUnlocked) {
            binding.btnBuyNow.text = getString(R.string.unlocked_label)
            binding.btnBuyNow.isEnabled = false
            binding.btnBuyNow.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.unlocked_btn_bg))
            binding.btnBuyNow.setTextColor(requireContext().getColor(R.color.white50))
            binding.btnBuyNow.setOnClickListener(null)
            
            binding.clProPlanSection.visibility = View.VISIBLE
        } else {
            binding.btnBuyNow.text = getString(R.string.upgrade_label)
            binding.btnBuyNow.isEnabled = true
            binding.btnBuyNow.backgroundTintList = requireContext().getColorStateList(R.color.lime_green)
            binding.btnBuyNow.setTextColor(requireContext().getColor(R.color.black))
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
            
            binding.clProPlanSection.visibility = View.VISIBLE
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
        binding.groupHeaderContent.visibility = if (show) View.GONE else View.VISIBLE
        binding.nsvContent.visibility = if (show) View.GONE else View.VISIBLE
    }
}
