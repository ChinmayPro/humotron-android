package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentShopDevicesBinding
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.ui.shop.adapter.ShopDeviceAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopDevicesFragment : BaseFragment(R.layout.fragment_shop_devices) {

    private lateinit var binding: FragmentShopDevicesBinding
    private val viewModel: ShopViewModel by viewModels()
    private lateinit var shopAdapter: ShopDeviceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopDevicesBinding.bind(view)

        setupRecyclerView()
        setupObservers()

        viewModel.fetchShopDevices()
    }

    private fun setupRecyclerView() {
        shopAdapter = ShopDeviceAdapter()
        shopAdapter.onItemClick = { device ->
            val bundle = Bundle().apply {
                putParcelable("device", device)
            }
            (parentFragment?.parentFragment as? ShopFragment)?.findNavController()
                ?.navigate(R.id.fragmentShopDeviceDetails, bundle)
        }
        binding.rvDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDevices.adapter = shopAdapter
    }

    private fun setupObservers() {
        viewModel.getDevicesLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    toggleShimmer(false)
                    binding.tvNoData.visibility = View.GONE
                    resource.data?.let {
                        shopAdapter.setItems(it)
                        if (it.data.isNullOrEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                        }
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    toggleShimmer(false)
                    binding.tvNoData.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        resource.error?.errorMessage ?: "Error loading devices",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Status.LOADING -> {
                    toggleShimmer(true)
                    binding.tvNoData.visibility = View.GONE
                }
            }
        }
    }

    private fun toggleShimmer(show: Boolean) {
        val shopFragment = (parentFragment?.parentFragment as? ShopFragment)
        if (show) {
            binding.shimmerView.visibility = View.VISIBLE
            binding.shimmerView.startShimmer()
            binding.rvDevices.visibility = View.GONE
            shopFragment?.showTitleShimmer()
        } else {
            binding.shimmerView.stopShimmer()
            binding.shimmerView.visibility = View.GONE
            binding.rvDevices.visibility = View.VISIBLE
            shopFragment?.hideTitleShimmer()
        }
    }
}
