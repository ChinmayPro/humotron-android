package com.humotron.app.ui.shop

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopFragment : BaseFragment(R.layout.fragment_shop) {

    private val viewModel by viewModels<ShopViewModel>()
    private lateinit var binding: FragmentShopBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopBinding.bind(view)

        // Read navigation argument to determine which tab to select
        arguments?.getInt("selectedTabId")?.let { tabId ->
            if (tabId != 0) {
                viewModel.lastSelectedTabId = tabId
            }
        }

        setupTabs()
        setupNavigationListener()

        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupNavigationListener() {
        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment_shop) as? NavHostFragment
        navHostFragment?.navController?.addOnDestinationChangedListener { _, destination, _ ->
            updateTabUI(destination.id)
        }
    }

    private fun setupTabs() {
        binding.header.tabDevices.setOnClickListener { selectTab(R.id.fragmentShopDevices) }
        binding.header.tabScans.setOnClickListener { selectTab(R.id.fragmentShopScans) }
        binding.header.tabOptimize.setOnClickListener { selectTab(R.id.fragmentShopOptimize) }
        binding.header.tabBooks.setOnClickListener { selectTab(R.id.fragmentShopBooks) }
        binding.header.tabTools.setOnClickListener { selectTab(R.id.fragmentShopTools) }

        // Set default selection from ViewModel
        selectTab(viewModel.lastSelectedTabId)
    }

    private fun selectTab(destinationId: Int) {
        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment_shop) as NavHostFragment
        val navController = navHostFragment.navController

        if (navController.currentDestination?.id != destinationId) {
            val options = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, false)
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build()
            navController.navigate(resId = destinationId, args = null, navOptions = options)
        }
    }

    private fun updateTabUI(destinationId: Int) {
        viewModel.lastSelectedTabId = destinationId
        val selectedColor = requireContext().getColor(R.color.green_1)
        val unselectedColor = requireContext().getColor(R.color.white50)

        // Reset all tabs
        resetTab(binding.header.tabDevices, binding.header.ivDevices, binding.header.tvDevices, unselectedColor)
        resetTab(binding.header.tabScans, binding.header.ivScans, binding.header.tvScans, unselectedColor)
        resetTab(binding.header.tabOptimize, binding.header.ivOptimize, binding.header.tvOptimize, unselectedColor)
        resetTab(binding.header.tabBooks, binding.header.ivBooks, binding.header.tvBooks, unselectedColor)
        resetTab(binding.header.tabTools, binding.header.ivTools, binding.header.tvTools, unselectedColor)

        // Highlight selected tab
        when (destinationId) {
            R.id.fragmentShopDevices -> highlightTab(binding.header.tabDevices, binding.header.ivDevices, binding.header.tvDevices, selectedColor)
            R.id.fragmentShopScans -> highlightTab(binding.header.tabScans, binding.header.ivScans, binding.header.tvScans, selectedColor)
            R.id.fragmentShopOptimize -> highlightTab(binding.header.tabOptimize, binding.header.ivOptimize, binding.header.tvOptimize, selectedColor)
            R.id.fragmentShopBooks -> highlightTab(binding.header.tabBooks, binding.header.ivBooks, binding.header.tvBooks, selectedColor)
            R.id.fragmentShopTools -> highlightTab(binding.header.tabTools, binding.header.ivTools, binding.header.tvTools, selectedColor)
        }
    }

    private fun resetTab(tab: android.view.View, imageView: android.widget.ImageView, textView: android.widget.TextView, color: Int) {
        tab.background = null
        imageView.imageTintList = ColorStateList.valueOf(color)
        textView.setTextColor(color)
    }

    private fun highlightTab(tab: android.view.View, imageView: android.widget.ImageView, textView: android.widget.TextView, color: Int) {
        tab.setBackgroundResource(R.drawable.bg_shop_tab_active)
        imageView.imageTintList = ColorStateList.valueOf(color)
        textView.setTextColor(color)
    }

    fun showTitleShimmer() {
        // Removed shimmer for title
    }

    fun hideTitleShimmer() {
        // Removed shimmer for title
    }


}
