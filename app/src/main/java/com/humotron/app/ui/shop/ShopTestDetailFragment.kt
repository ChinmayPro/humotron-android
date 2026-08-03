package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopTestDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopTestDetailFragment : BaseFragment(R.layout.fragment_shop_test_detail) {

    private lateinit var binding: FragmentShopTestDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopTestDetailBinding.bind(view)

        setupInsets()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnProceedToBooking.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentShopTestDetail_to_fragmentBookingType)
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            binding.layoutFooter.updatePadding(bottom = systemBars.bottom + (20 * density).toInt())
            insets
        }
    }
}
