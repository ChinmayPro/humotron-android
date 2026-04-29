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

        setupTabs()
        setupNavigationListener()
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
        val selectedColor = requireContext().getColor(R.color.colorBgBtn)
        val unselectedColor = requireContext().getColor(R.color.white30)

        // Reset all tabs
        resetTab(binding.header.ivDevices, binding.header.tvDevices, unselectedColor)
        resetTab(binding.header.ivScans, binding.header.tvScans, unselectedColor)
        resetTab(binding.header.ivOptimize, binding.header.tvOptimize, unselectedColor)
        resetTab(binding.header.ivBooks, binding.header.tvBooks, unselectedColor)
        resetTab(binding.header.ivTools, binding.header.tvTools, unselectedColor)

        // Highlight selected tab & update title
        when (destinationId) {
            R.id.fragmentShopDevices -> {
                highlightTab(binding.header.ivDevices, binding.header.tvDevices, selectedColor)
                binding.header.tvShopTitle.text = getString(R.string.header_devices)
            }
            R.id.fragmentShopScans -> {
                highlightTab(binding.header.ivScans, binding.header.tvScans, selectedColor)
                binding.header.tvShopTitle.text = getString(R.string.header_scans)
            }
            R.id.fragmentShopOptimize -> {
                highlightTab(binding.header.ivOptimize, binding.header.tvOptimize, selectedColor)
                binding.header.tvShopTitle.text = getString(R.string.optimize)
            }
            R.id.fragmentShopBooks -> {
                highlightTab(binding.header.ivBooks, binding.header.tvBooks, selectedColor)
                binding.header.tvShopTitle.text = getString(R.string.header_books)
            }
            R.id.fragmentShopTools -> {
                highlightTab(binding.header.ivTools, binding.header.tvTools, selectedColor)
                binding.header.tvShopTitle.text = getString(R.string.header_tools)
            }
        }
    }

    private fun resetTab(imageView: android.widget.ImageView, textView: android.widget.TextView, color: Int) {
        imageView.imageTintList = ColorStateList.valueOf(color)
        textView.setTextColor(color)
    }

    private fun highlightTab(imageView: android.widget.ImageView, textView: android.widget.TextView, color: Int) {
        imageView.imageTintList = ColorStateList.valueOf(color)
        textView.setTextColor(color)
    }

    fun showTitleShimmer() {
        binding.header.shimmerTitle.startShimmer()
        binding.header.tvShopTitle.visibility = View.INVISIBLE
        binding.header.viewTitlePlaceholder.visibility = View.VISIBLE
    }

    fun hideTitleShimmer() {
        binding.header.shimmerTitle.stopShimmer()
        binding.header.tvShopTitle.visibility = View.VISIBLE
        binding.header.viewTitlePlaceholder.visibility = View.GONE
    }


}
