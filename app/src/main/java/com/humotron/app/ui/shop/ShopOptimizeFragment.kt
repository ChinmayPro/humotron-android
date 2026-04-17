package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopOptimizeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopOptimizeFragment : BaseFragment(R.layout.fragment_shop_optimize) {

    private lateinit var binding: FragmentShopOptimizeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopOptimizeBinding.bind(view)
    }
}
