package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeOptimizeBinding
import com.humotron.app.data.network.Status
import com.humotron.app.domain.modal.response.RecommendationMetricItem
import com.humotron.app.ui.decode.adapter.DecodeOptimizeAdapter
import com.humotron.app.ui.decode.adapter.DecodeOptimizeFilterAdapter
import com.humotron.app.ui.decode.adapter.CategoryFilter
import com.humotron.app.ui.shop.ShopViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeOptimizeFragment : BaseFragment(R.layout.fragment_decode_optimize) {

    private lateinit var binding: FragmentDecodeOptimizeBinding
    private val viewModel: ShopViewModel by viewModels()

    private lateinit var adapter: DecodeOptimizeAdapter
    private lateinit var filterAdapter: DecodeOptimizeFilterAdapter

    private var rawRecommendations: List<RecommendationMetricItem> = emptyList()
    private var selectedCategory: String = "All"
    private val categories = listOf(
        "All", "Supplements", "Nutrition", "Devices", "Skincare", "Tests", "Apps", "Experts", "Habits"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeOptimizeBinding.bind(view)

        setupSubtitle()
        setupAdapters()
        initObservers()
        viewModel.fetchOptimizedRecommendations()
    }

    private fun setupSubtitle() {
        val greenText = "Three signals"
        val fullText = "Three signals point the same way this week."
        val spannable = android.text.SpannableStringBuilder(fullText)
        val color = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorBgBtn)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(color),
            0,
            greenText.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvHeaderSubtitle.text = spannable
    }

    private fun setupAdapters() {
        // Main list adapter
        adapter = DecodeOptimizeAdapter(
            items = emptyList(),
            onExploreClick = { productId, productType, metricName, metricDelta, metricStatus ->
                val bundle = Bundle().apply {
                    putString("productId", productId)
                    putString("productType", productType)
                    putString("metricName", metricName)
                    putString("metricDelta", metricDelta)
                    putString("metricStatus", metricStatus)
                }
                parentFragment?.findNavController()
                    ?.navigate(R.id.fragmentDecodeOptimizeDetail, bundle)
            }
        )
        binding.rvOptimize.adapter = adapter

        // Category filter adapter
        filterAdapter = DecodeOptimizeFilterAdapter(
            filters = categories.map { CategoryFilter(it, 0) },
            selectedCategory = selectedCategory,
            onCategoryClick = { categoryName ->
                selectedCategory = categoryName
                updateFilterUI()
                filterAndDisplayItems()
            }
        )
        binding.rvFilters.adapter = filterAdapter
    }

    private fun initObservers() {
        viewModel.getOptimizedRecommendationsLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.layoutLoader.root.visibility = View.GONE

                    rawRecommendations = resource.data?.data ?: emptyList()

                    if (rawRecommendations.isEmpty()) {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.nsvContent.visibility = View.GONE
                    } else {
                        binding.tvNoData.visibility = View.GONE
                        binding.nsvContent.visibility = View.VISIBLE

                        updateFilterUI()
                        filterAndDisplayItems()
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

    private fun updateFilterUI() {
        val filterList = categories.map { categoryName ->
            CategoryFilter(categoryName, countForCategory(categoryName, rawRecommendations))
        }
        filterAdapter.updateFilters(filterList, selectedCategory)
    }

    private fun countForCategory(categoryName: String, list: List<RecommendationMetricItem>): Int {
        return when (categoryName) {
            "All" -> list.size
            "Supplements" -> list.count { it.type.equals("supplement", ignoreCase = true) }
            "Nutrition" -> list.count { it.type.equals("recipeBundle", ignoreCase = true) }
            "Habits" -> list.count { it.type.equals("lifestyle", ignoreCase = true) }
            "Devices" -> list.count { it.category?.any { it.contains("device", true) } == true }
            "Skincare" -> list.count { it.category?.any { it.contains("skincare", true) } == true }
            "Tests" -> list.count { it.category?.any { it.contains("test", true) || it.contains("diagnostic", true) } == true }
            "Apps" -> list.count { it.category?.any { it.contains("app", true) } == true }
            "Experts" -> list.count { it.category?.any { it.contains("expert", true) } == true }
            else -> 0
        }
    }

    private fun filterAndDisplayItems() {
        val filteredList = when (selectedCategory) {
            "All" -> rawRecommendations
            "Supplements" -> rawRecommendations.filter { it.type.equals("supplement", ignoreCase = true) }
            "Nutrition" -> rawRecommendations.filter { it.type.equals("recipeBundle", ignoreCase = true) }
            "Habits" -> rawRecommendations.filter { it.type.equals("lifestyle", ignoreCase = true) }
            "Devices" -> rawRecommendations.filter { it.category?.any { it.contains("device", true) } == true }
            "Skincare" -> rawRecommendations.filter { it.category?.any { it.contains("skincare", true) } == true }
            "Tests" -> rawRecommendations.filter { it.category?.any { it.contains("test", true) || it.contains("diagnostic", true) } == true }
            "Apps" -> rawRecommendations.filter { it.category?.any { it.contains("app", true) } == true }
            "Experts" -> rawRecommendations.filter { it.category?.any { it.contains("expert", true) } == true }
            else -> rawRecommendations
        }

        if (filteredList.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.rvOptimize.visibility = View.GONE
        } else {
            binding.tvNoData.visibility = View.GONE
            binding.rvOptimize.visibility = View.VISIBLE
            adapter.updateItems(filteredList)
        }
    }
}
