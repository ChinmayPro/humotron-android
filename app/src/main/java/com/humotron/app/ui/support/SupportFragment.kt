package com.humotron.app.ui.support

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSupportBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.response.SupportConnectedDevice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportFragment : BaseFragment(R.layout.fragment_support) {

    private lateinit var binding: FragmentSupportBinding
    private val viewModel: SupportViewModel by viewModels()

    private val popularSearchAdapter by lazy {
        PopularSearchAdapter { keyword ->
            binding.etSearchHelp.setText(keyword)
        }
    }

    private val supportCategoryAdapter by lazy {
        SupportCategoryAdapter { _ ->
        }
    }

    private val supportDeviceAdapter by lazy {
        SupportDeviceAdapter { device ->
            // Map SupportConnectedDevice to UserDevice for the config screen
            val userDevice = GetAllDeviceResponse.Data.UserDevice(
                id = device.deviceId,
                deviceName = device.hardwareType,
                deviceFacingName = device.deviceLabel,
                dataSync = device.connectedAt,
                deviceCategoryName = null,
                deviceImage = if (device.deviceUrl != null) listOf(device.deviceUrl) else null,
                deviceModelId = null,
                deviceModelName = null,
                deviceSubCategoryId = null,
                deviceSubCategoryName = null,
                deviceTextMessage = null,
                deviceType = device.hardwareType,
                deviceUrl = if (device.deviceUrl != null) listOf(device.deviceUrl) else null,
                metrics = null,
                orderStatus = device.status
            )
            val bundle = Bundle().apply {
                putParcelable("wearable", userDevice)
            }
            findNavController().navigate(R.id.fragmentDeviceConfig, bundle)
        }
    }

    private val popularArticleAdapter by lazy {
        SupportArticleAdapter { _ ->
            // Do not navigate/open anything for now
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportBinding.bind(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }

        initViews()
        initObservers()
        
        // Fetch Support Home and My Tickets Data
        viewModel.fetchSupportHomeData()
        viewModel.fetchMyTickets()
    }

    private fun initViews() {
        binding.header.title.text = getString(R.string.support)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.rvPopularSearches.layoutManager = flexboxLayoutManager
        binding.rvPopularSearches.adapter = popularSearchAdapter

        binding.rvCategories.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategories.adapter = supportCategoryAdapter

        binding.rvYourDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvYourDevices.adapter = supportDeviceAdapter

        binding.rvPopularArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPopularArticles.adapter = popularArticleAdapter

        binding.clYourDevicesTitle.visibility = View.GONE
        binding.clYourDevices.visibility = View.GONE

        binding.btnContactSupport.setOnClickListener {
        }
    }

    private fun initObservers() {
        viewModel.supportHomeData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                    binding.scrollRoot.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    hideProgress()
                    binding.scrollRoot.visibility = View.VISIBLE
                    val response = resource.data
                    if (response?.status == "success" && response.data != null) {
                        
                        response.data.popularSearchKeywords?.let { keywords ->
                            popularSearchAdapter.setData(keywords)
                        }

                        response.data.categories?.let { categories ->
                            supportCategoryAdapter.setData(categories)
                        }

                        val categories = response.data.categories
                        val devicesCategory = categories?.find { it.key.equals("devices", ignoreCase = true) }
                        val subcategories = devicesCategory?.subcategories

                        if (!subcategories.isNullOrEmpty()) {
                            val devices = subcategories.map { subcat ->
                                SupportConnectedDevice(
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
                            }
                            supportDeviceAdapter.setData(devices)
                            binding.clYourDevicesTitle.visibility = View.VISIBLE
                            binding.clYourDevices.visibility = View.VISIBLE
                        } else {
                            binding.clYourDevicesTitle.visibility = View.GONE
                            binding.clYourDevices.visibility = View.GONE
                        }

                        val articles = response.data.popularArticles
                        if (!articles.isNullOrEmpty()) {
                            popularArticleAdapter.setData(articles)
                            binding.clPopularArticlesTitle.visibility = View.VISIBLE
                            binding.rvPopularArticles.visibility = View.VISIBLE
                        } else {
                            binding.clPopularArticlesTitle.visibility = View.GONE
                            binding.rvPopularArticles.visibility = View.GONE
                        }
                    } else {
                        val msg = response?.message ?: getString(R.string.support_failed_get_details)
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    binding.scrollRoot.visibility = View.VISIBLE
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
                Status.EXCEPTION -> {
                    hideProgress()
                    binding.scrollRoot.visibility = View.VISIBLE
                    val exceptionMsg = resource.error?.errorMessage ?: getString(R.string.support_exception_occurred)
                    Toast.makeText(requireContext(), exceptionMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.myTicketsData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    // Running in background, no need to show global progress dialog
                }
                Status.SUCCESS -> {
                    val response = resource.data
                    if (response?.status == "success" && response.data != null) {
                        
                        val total = response.data.total ?: 0
                        if (total > 0) {
                            binding.tvBadge.text = total.toString()
                            binding.tvBadge.visibility = View.VISIBLE
                        } else {
                            binding.tvBadge.visibility = View.GONE
                        }
                    } else {
                        val msg = response?.message ?: getString(R.string.support_failed_get_tickets)
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        binding.tvBadge.visibility = View.GONE
                    }
                }
                Status.ERROR -> {
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    binding.tvBadge.visibility = View.GONE
                }
                Status.EXCEPTION -> {
                    val exceptionMsg = resource.error?.errorMessage ?: getString(R.string.support_exception_occurred)
                    Toast.makeText(requireContext(), exceptionMsg, Toast.LENGTH_SHORT).show()
                    binding.tvBadge.visibility = View.GONE
                }
            }
        }
    }
}

