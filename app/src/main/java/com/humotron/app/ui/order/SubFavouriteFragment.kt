package com.humotron.app.ui.order

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSubFavouriteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubFavouriteFragment : BaseFragment(R.layout.fragment_sub_favourite) {
    private lateinit var binding: FragmentSubFavouriteBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubFavouriteBinding.bind(view)
    }
}
