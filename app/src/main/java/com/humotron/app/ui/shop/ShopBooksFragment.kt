package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopBooksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopBooksFragment : BaseFragment(R.layout.fragment_shop_books) {

    private lateinit var binding: FragmentShopBooksBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopBooksBinding.bind(view)
    }
}
