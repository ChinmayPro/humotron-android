package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDeviceListBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.ui.device.DeviceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceListFragment : BaseFragment(R.layout.fragment_device_list) {

    private lateinit var binding: FragmentDeviceListBinding
    private val viewModel: DeviceViewModel by viewModels()
    private var adapter: DeviceListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeviceListBinding.bind(view)

        initClicks()
        initData()
        observeData()
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initData() {
        binding.header.title.text = resources.getString(R.string.header_devices)
        viewModel.observeUserDeviceData()
        viewModel.refreshUserDeviceData(false)
    }

    private fun observeData() {
        viewModel.getDeviceListData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val allDevices = mutableListOf<GetAllDeviceResponse.Data.UserDevice>()
                    resource.data?.data?.let { data ->
                        data.connectedDevices?.let { allDevices.addAll(it) }
                        data.health?.let { allDevices.addAll(it) }
                        data.wearables?.let { allDevices.addAll(it) }
                        data.environmentalMetrics?.let { allDevices.addAll(it) }
                    }

                    adapter = DeviceListAdapter(allDevices) { device ->
                        val action =
                            DeviceListFragmentDirections.actionFragmentDeviceListToFragmentDeviceConfig(
                                device
                            )
                        findNavController().navigate(action)
                    }
                    binding.rvDevices.adapter = adapter
                }

                Status.ERROR -> {
                    // Handle error state if needed
                }

                Status.LOADING -> {
                    // Handle loading state if needed
                }

                else -> {}
            }
        }
    }

}
