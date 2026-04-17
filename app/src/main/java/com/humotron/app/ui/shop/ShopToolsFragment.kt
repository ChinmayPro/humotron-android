package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopToolsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopToolsFragment : BaseFragment(R.layout.fragment_shop_tools) {

    private lateinit var binding: FragmentShopToolsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopToolsBinding.bind(view)
    }
}
