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
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPurchases()
        if (viewModel.isBillingFlowActive) {
            viewModel.clearBillingFlowActive()
        } else {
            viewModel.fetchBoosters()
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

        viewModel.activePurchasesLiveData.observe(viewLifecycleOwner) { purchases ->
            toolsAdapter.setActivePurchases(purchases)
        }

        viewModel.playStoreProductsLiveData.observe(viewLifecycleOwner) { products ->
            toolsAdapter.setPlayStoreProducts(products)
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
