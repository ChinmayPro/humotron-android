package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopOptimizeBinding
import androidx.fragment.app.viewModels
import com.humotron.app.data.network.Status
import com.humotron.app.domain.modal.response.OptimizedRecipeData
import com.humotron.app.ui.shop.adapter.OptimizeAdapter
import com.humotron.app.ui.shop.adapter.OptimizeUIItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopOptimizeFragment : BaseFragment(R.layout.fragment_shop_optimize) {

    private lateinit var binding: FragmentShopOptimizeBinding
    private val viewModel: ShopViewModel by viewModels()

    private lateinit var adapter: OptimizeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopOptimizeBinding.bind(view)
        
        setupAdapter()
        initObservers()
        viewModel.fetchOptimizedRecipe()
    }

    private fun setupAdapter() {
        adapter = OptimizeAdapter(
            items = emptyList(),
            onExploreClick = { id, type ->
                // TODO: Navigate to details based on type if needed
            },
            onChatPromptClick = { promptId, title ->
                // TODO: Handle chat prompt click
            }
        )
        binding.rvOptimize.adapter = adapter
    }

    private fun initObservers() {
        viewModel.getOptimizedRecipeLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    
                    val data = resource.data?.data
                    val uiItems = if (data != null) prepareUIItems(data) else emptyList()
                    
                    if (uiItems.isEmpty()) {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.nsvContent.visibility = View.GONE
                    } else {
                        binding.tvNoData.visibility = View.GONE
                        binding.nsvContent.visibility = View.VISIBLE
                        adapter.updateItems(uiItems)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    binding.nsvContent.visibility = View.GONE
                    binding.tvNoData.visibility = View.VISIBLE
                }
                Status.LOADING -> {
                    binding.layoutLoader.root.visibility = View.VISIBLE
                    binding.layoutLoader.tvLoadingMessage.text = "Analyzing your health metrics..."
                    binding.layoutLoader.lottieLoader.playAnimation()
                    binding.nsvContent.visibility = View.GONE
                    binding.tvNoData.visibility = View.GONE
                }
            }
        }
    }

    private fun prepareUIItems(data: OptimizedRecipeData): List<OptimizeUIItem> {
        val list = mutableListOf<OptimizeUIItem>()
        
        data.supplements?.let { supplements ->
            if (supplements.isNotEmpty()) {
                list.add(OptimizeUIItem.Header(getString(R.string.category_supplements)))
                supplements.forEach { list.add(OptimizeUIItem.Supplement(it)) }
            }
        }

        return list
    }
}
