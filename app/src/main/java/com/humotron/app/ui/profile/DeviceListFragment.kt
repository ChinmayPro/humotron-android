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
import com.humotron.app.domain.modal.response.WearableProviderResponse
import com.humotron.app.ui.device.DeviceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceListFragment : BaseFragment(R.layout.fragment_device_list) {

    private lateinit var binding: FragmentDeviceListBinding
    private val viewModel: DeviceViewModel by viewModels()
    private var adapter: DeviceListAdapter? = null

    private var userDevicesList: List<GetAllDeviceResponse.Data.UserDevice> = emptyList()
    private var wearableDevicesList: List<WearableProviderResponse.WearableDevice> = emptyList()

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
        viewModel.refreshUserDeviceData(false)
        viewModel.refreshWearableDeviceData(false)
    }

    private fun observeData() {
        viewModel.getDeviceListData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    updateCombinedList()
                }

                Status.ERROR, Status.EXCEPTION -> {
                    // Handle error state if needed
                }

                Status.LOADING -> {
                    // Keep current items during loading if already available
                }

                else -> {}
            }
        }

        viewModel.getWearableProviderListData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    updateCombinedList()
                }

                Status.ERROR, Status.EXCEPTION -> {
                    // Handle error state if needed
                }

                Status.LOADING -> {
                    // Keep current items during loading if already available
                }

                else -> {}
            }
        }

        // Trigger an initial merge check in case LiveData already holds cached success data
        updateCombinedList()
    }

    private fun updateCombinedList() {
        val deviceListResource = viewModel.getDeviceListData().value
        if (deviceListResource?.status == Status.SUCCESS) {
            val allDevices = mutableListOf<GetAllDeviceResponse.Data.UserDevice>()
            deviceListResource.data?.data?.let { data ->
                data.connectedDevices?.let { allDevices.addAll(it) }
                data.health?.let { allDevices.addAll(it) }
                data.wearables?.let { allDevices.addAll(it) }
                data.environmentalMetrics?.let { allDevices.addAll(it) }
                data.context?.let { allDevices.addAll(it) }
                data.reports?.let { allDevices.addAll(it) }
            }
            userDevicesList = allDevices
        }

        val wearableResource = viewModel.getWearableProviderListData().value
        if (wearableResource?.status == Status.SUCCESS) {
            wearableDevicesList = wearableResource.data?.data?.devices ?: emptyList()
        }

        val combinedList = mutableListOf<DeviceItem>()

        userDevicesList.forEach { userDevice ->
            combinedList.add(DeviceItem.UserDeviceItem(userDevice))
        }

        wearableDevicesList.forEach { wearableDevice ->
            combinedList.add(DeviceItem.WearableDeviceItem(wearableDevice))
        }

        if (combinedList.isEmpty()) return

        if (adapter == null || binding.rvDevices.adapter == null) {
            adapter = DeviceListAdapter(combinedList) { item ->
                val userDevice = when (item) {
                    is DeviceItem.UserDeviceItem -> item.userDevice
                    is DeviceItem.WearableDeviceItem -> GetAllDeviceResponse.Data.UserDevice(
                        id = item.wearableDevice.id,
                        deviceName = item.wearableDevice.provider,
                        deviceFacingName = item.displayName,
                        dataSync = item.wearableDevice.lastSyncedAt,
                        deviceCategoryName = "Wearables",
                        deviceImage = null,
                        deviceModelId = null,
                        deviceModelName = null,
                        deviceSubCategoryId = null,
                        deviceSubCategoryName = null,
                        deviceTextMessage = null,
                        deviceType = "device",
                        deviceUrl = null,
                        metrics = null,
                        orderStatus = null
                    )
                }
                val action =
                    DeviceListFragmentDirections.actionFragmentDeviceListToFragmentDeviceConfig(
                        userDevice
                    )
                findNavController().navigate(action)
            }
            binding.rvDevices.adapter = adapter
        } else {
            adapter?.updateData(combinedList)
        }
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }
}
