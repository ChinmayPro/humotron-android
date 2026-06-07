package com.humotron.app.ui.support

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSupportDevicesBinding
import com.humotron.app.domain.modal.response.SupportConnectedDevice
import com.humotron.app.ui.support.adapter.SupportViewAllSubcategoryAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportDevicesFragment : BaseFragment(R.layout.fragment_support_devices) {

    private lateinit var binding: FragmentSupportDevicesBinding
    private val viewModel: SupportViewModel by activityViewModels()

    private val devicesAdapter by lazy {
        SupportViewAllSubcategoryAdapter { subcat ->
            val popularSubcat = viewModel.topicsByCategoryData.value?.data?.data?.popularTopics?.find {
                it.subcategoryKey.equals(subcat.key, ignoreCase = true) ||
                it.subcategoryLabel.equals(subcat.label, ignoreCase = true)
            }
            val firstTopic = popularSubcat?.topics?.firstOrNull()
            if (firstTopic != null) {
                val bundle = Bundle().apply {
                    putParcelable("article", firstTopic)
                }
                findNavController().navigate(
                    R.id.action_fragmentSupportDevices_to_fragmentSupportArticleDetail,
                    bundle
                )
            } else {
                val mappedDevice = SupportConnectedDevice(
                    hardwareId = null,
                    deviceLabel = subcat.label,
                    deviceScopeKey = subcat.key,
                    deviceId = null,
                    status = "connected",
                    deviceUrl = subcat.icon,
                    uuid = null,
                    connectedAt = null,
                    hardwareType = null,
                    serialNumber = null
                )
                val bundle = Bundle().apply {
                    putString("categoryKey", "devices")
                    putString("categoryLabel", subcat.label ?: getString(R.string.header_devices))
                    putParcelable("device", mappedDevice)
                }
                findNavController().navigate(
                    R.id.action_fragmentSupportDevices_to_fragmentSupportViewAllArticles,
                    bundle
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportDevicesBinding.bind(view)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }

        initViews()
        initObservers()

        // Fetch support category details for "devices" when screen opens
        viewModel.fetchSupportCategoryByKey("devices")
    }

    private fun initViews() {
        binding.header.title.text = getString(R.string.support_your_devices)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rvDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDevices.adapter = devicesAdapter
    }

    private fun initObservers() {
        viewModel.supportCategoryData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    val response = resource.data
                    if (response?.status == "success" && response.data?.category != null) {
                        val category = response.data.category
                        binding.tvDevicesDesc.text = category.description.orEmpty().ifEmpty { getString(R.string.support_devices_fallback_desc) }

                        val subcategories = category.subcategories
                        if (!subcategories.isNullOrEmpty()) {
                            devicesAdapter.setData(subcategories)
                        } else {
                            devicesAdapter.setData(emptyList())
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                }
                Status.EXCEPTION -> {
                    hideProgress()
                }
            }
        }
    }
}
