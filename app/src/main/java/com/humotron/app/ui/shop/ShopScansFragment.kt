package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopScansBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopScansFragment : BaseFragment(R.layout.fragment_shop_scans) {

    private lateinit var binding: FragmentShopScansBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopScansBinding.bind(view)

        binding.btnBookNow.setOnClickListener {
            // TODO: Implement booking logic
        }
    }
}
