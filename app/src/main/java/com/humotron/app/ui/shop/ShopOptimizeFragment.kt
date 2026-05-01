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
import androidx.navigation.fragment.findNavController
import com.humotron.app.domain.modal.response.GetShopDevicesResponse

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
            onExploreClick = { item ->
                val bundle = Bundle()
                when (item) {
                    is OptimizeUIItem.Supplement -> {
                        bundle.putParcelable("supplement", item.data)
                    }
                    is OptimizeUIItem.Recipe -> {
                        bundle.putParcelable("recipe", item.data)
                    }
                    is OptimizeUIItem.Recommendation -> {
                        // Recommendations might lead elsewhere or to a generic detail
                    }
                    else -> {}
                }
                
                if (!bundle.isEmpty) {
                    (parentFragment?.parentFragment as? ShopFragment)?.findNavController()
                        ?.navigate(R.id.fragmentShopOptimizeDetail, bundle)
                }
            },
            onChatPromptClick = { _, _ ->
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

        data.recipes?.let { recipes ->
            if (recipes.isNotEmpty()) {
                list.add(OptimizeUIItem.Header(getString(R.string.category_recipes)))
                recipes.forEach { list.add(OptimizeUIItem.Recipe(it)) }
            }
        }

        data.recommendations?.let { recommendations ->
            if (recommendations.isNotEmpty()) {
                list.add(OptimizeUIItem.Header(getString(R.string.category_recommendations)))
                recommendations.forEach { list.add(OptimizeUIItem.Recommendation(it)) }
            }
        }

        return list
    }
}
