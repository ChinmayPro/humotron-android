package com.humotron.app.ui.device

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentPastScansBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.PastScansAdapter
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PastScansFragment : BaseFragment(R.layout.fragment_past_scans) {

    private lateinit var binding: FragmentPastScansBinding
    private val viewModel: DeviceViewModel by viewModels()
    private lateinit var adapter: PastScansAdapter

    private var healthScanItem: HealthScanItem? = null
    private var wearable: GetAllDeviceResponse.Data.Wearable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPastScansBinding.bind(view)

        initClicks()
        initViews()
        observeData()
    }

    private fun initViews() {
        healthScanItem = arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM)
        wearable = arguments?.getParcelable(NavKeys.WEARABLE)

        binding.header.title.text = "Past ${healthScanItem?.metricName?.replace("\n", " ")} Scans"
        binding.tvTitle.text = "${healthScanItem?.metricName} Levels During Past Scans"

        adapter = PastScansAdapter {
            // Handle item click if needed
        }
        binding.rvPastScans.adapter = adapter

        val type = healthScanItem?.type?.name ?: ""
        val deviceId = wearable?.id ?: ""
        if (type.isNotEmpty() && deviceId.isNotEmpty()) {
            viewModel.getAllScanByType(type, deviceId)
        }
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val onScanClick = View.OnClickListener {
            findNavController().navigate(
                R.id.fragmentHealthScan,
                bundleOf(
                    NavKeys.HEALTH_SCAN_ITEM to healthScanItem,
                    NavKeys.WEARABLE to wearable
                )
            )
        }

        binding.btnScanNow.setOnClickListener(onScanClick)
        binding.btnScanNowEmpty.setOnClickListener(onScanClick)
    }

    private fun observeData() {
        viewModel.pastScanLiveData.observe(viewLifecycleOwner) { state ->
            when (state.status) {
                Status.LOADING -> {
                    binding.progress.isVisible = true
                }

                Status.SUCCESS -> {
                    binding.progress.isVisible = false
                    val data = state.data?.data ?: emptyList()
                    adapter.submitList(data)

                    val isEmpty = data.isEmpty()
                    binding.groupEmpty.isVisible = isEmpty
                    binding.rvPastScans.isVisible = !isEmpty
                    binding.btnScanNow.isVisible = !isEmpty
                    binding.tvTitle.isVisible = !isEmpty

                    if (isEmpty) {
                        binding.tvEmpty.text =
                            "You haven't taken any ${healthScanItem?.metricName?.lowercase()} scan yet. Take one now to see how it works."
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    binding.progress.isVisible = false
                    // Handle error
                }
            }
        }
    }
}
