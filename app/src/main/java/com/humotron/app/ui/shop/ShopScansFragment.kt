package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShopScansBinding
import com.humotron.app.ui.shop.dialog.CardiacTestDetailsBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopScansFragment : BaseFragment(R.layout.fragment_shop_scans) {

    private lateinit var binding: FragmentShopScansBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopScansBinding.bind(view)

        binding.btnBookNow.setOnClickListener {
            val bottomSheet = CardiacTestDetailsBottomSheet.newInstance()
            bottomSheet.onProceedClicked = {
                // Navigate using parent fragment's navController to reach fragmentBookingType
                // which is defined in nav_graph_shop
                parentFragment?.parentFragment?.findNavController()?.navigate(R.id.action_fragmentShop_to_fragmentBookingType)
            }
            bottomSheet.show(childFragmentManager, "CardiacTestDetailsBottomSheet")
        }
    }
}
