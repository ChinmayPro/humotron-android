package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentShopToolsBinding
import androidx.navigation.fragment.findNavController
import com.humotron.app.ui.shop.adapter.ShopToolsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopToolsFragment : BaseFragment(R.layout.fragment_shop_tools) {

    private lateinit var binding: FragmentShopToolsBinding
    private val viewModel: ShopToolsViewModel by activityViewModels()
    private lateinit var toolsAdapter: ShopToolsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopToolsBinding.bind(view)

        setupRecyclerView()
        setupPlanCardClicks()
        setupObservers()
    }

    private var isProSubscribedFromApi: Boolean = false
    private var activePurchases: List<com.android.billingclient.api.Purchase> = emptyList()

    private fun updateProPlanUI() {
        val isProActive = isProSubscribedFromApi || activePurchases.any { purchase ->
            purchase.products.contains(ShopToolsViewModel.PRO_PLAN_PRODUCT_ID)
        }

        if (isProActive) {
            binding.btnBasicPlan.text = "Included"
            binding.btnViewProPlan.text = getString(R.string.active)
            binding.btnViewProPlan.backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.unlocked_btn_bg))
            binding.btnViewProPlan.setTextColor(requireContext().getColor(R.color.white50))
        } else {
            binding.btnBasicPlan.text = getString(R.string.active)
            binding.btnViewProPlan.text = "View plan >"
            binding.btnViewProPlan.backgroundTintList = requireContext().getColorStateList(R.color.lime_green)
            binding.btnViewProPlan.setTextColor(android.graphics.Color.BLACK)
        }
    }

    private fun setupPlanCardClicks() {
        val navigateToPlanDetail = {
            (parentFragment?.parentFragment as? ShopFragment)?.findNavController()
                ?.navigate(R.id.fragmentShopPlanDetail)
        }
        binding.cardProPlan.setOnClickListener { navigateToPlanDetail() }
        binding.btnViewProPlan.setOnClickListener { navigateToPlanDetail() }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isBillingFlowActive) {
            viewModel.clearBillingFlowActive()
        } else {
            viewModel.refreshPurchases()
            viewModel.fetchBoosters()
            viewModel.fetchPlans()
        }
    }

    override fun onDestroyView() {
        hideProgress()
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        toolsAdapter = ShopToolsAdapter(
            onUnlockClick = { booster ->
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
            },
            onItemClick = { booster ->
                val bundle = Bundle().apply {
                    putParcelable("booster", booster)
                }
                (parentFragment?.parentFragment as? ShopFragment)?.findNavController()
                    ?.navigate(R.id.fragmentShopBoosterDetail, bundle)
            }
        )
        binding.rvTools.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTools.adapter = toolsAdapter
    }

    private fun setupObservers() {
        viewModel.boostersLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    toggleShimmer(false)
                    binding.tvNoData.visibility = View.GONE
                    resource.data?.let { list ->
                        toolsAdapter.setItems(list)
                        if (list.isEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                        }
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    toggleShimmer(false)
                    binding.tvNoData.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        resource.error?.errorMessage ?: getString(R.string.error_loading_tools),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Status.LOADING -> {
                    toggleShimmer(true)
                    binding.tvNoData.visibility = View.GONE
                }
            }
        }

        viewModel.plansLiveData.observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                val plans = resource.data ?: emptyList()
                val basicPlan = plans.find { it.planId == "Free_Plan" || it.displayName.equals("Basic", ignoreCase = true) }
                val proPlan = plans.find { it.planId == "Pro_Plan" || it.displayName.equals("Pro", ignoreCase = true) }

                basicPlan?.let { plan ->
                    binding.tvBasicTitle.text = plan.displayName
                    binding.tvBasicDesc.text = plan.displayDescription
                    binding.tvBasicPrice.text = plan.displayPriceFallback
                }

                proPlan?.let { plan ->
                    isProSubscribedFromApi = plan.isActive == true
                    binding.tvProTitle.text = plan.displayName
                    binding.tvProDesc.text = plan.displayDescription
                    // Set fallback price from API if Play Store price isn't loaded yet
                    if (binding.tvProPrice.text == "£19.90") {
                        binding.tvProPrice.text = plan.displayPriceFallback
                    }
                    updateProPlanUI()
                }
            }
        }

        viewModel.activePurchasesLiveData.observe(viewLifecycleOwner) { purchases ->
            activePurchases = purchases
            toolsAdapter.setActivePurchases(purchases)
            updateProPlanUI()
        }

        viewModel.playStoreProductsLiveData.observe(viewLifecycleOwner) { products ->
            toolsAdapter.setPlayStoreProducts(products)
            val proDetails = products.find { it.productId == ShopToolsViewModel.PRO_PLAN_PRODUCT_ID }
            val subscriptionOffer = proDetails?.subscriptionOfferDetails?.firstOrNull()
            val subscriptionPrice = subscriptionOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
            val priceText = subscriptionPrice ?: proDetails?.oneTimePurchaseOfferDetails?.formattedPrice
            if (!priceText.isNullOrEmpty()) {
                binding.tvProPrice.text = priceText
            }
        }

        viewModel.orderResultLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.unlocked_success),
                        Toast.LENGTH_LONG
                    ).show()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    Toast.makeText(
                        requireContext(),
                        resource.error?.errorMessage ?: getString(R.string.order_activation_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                Status.LOADING -> {
                    showProgress()
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

    private fun toggleShimmer(show: Boolean) {
        val shopFragment = (parentFragment?.parentFragment as? ShopFragment)
        if (show) {
            binding.shimmerView.visibility = View.VISIBLE
            binding.shimmerView.startShimmer()
            binding.rvTools.visibility = View.GONE
            shopFragment?.showTitleShimmer()
        } else {
            binding.shimmerView.stopShimmer()
            binding.shimmerView.visibility = View.GONE
            binding.rvTools.visibility = View.VISIBLE
            shopFragment?.hideTitleShimmer()
        }
    }
}
