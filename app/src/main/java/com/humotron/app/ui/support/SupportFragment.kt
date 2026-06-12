package com.humotron.app.ui.support

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
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

import com.humotron.app.ui.support.adapter.SupportArticleAdapter
import com.humotron.app.ui.support.adapter.SupportCategoryAdapter
import com.humotron.app.ui.support.adapter.SupportDeviceAdapter
import com.humotron.app.ui.support.adapter.PopularSearchAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportFragment : BaseFragment(R.layout.fragment_support) {

    private lateinit var binding: FragmentSupportBinding
    private val viewModel: SupportViewModel by activityViewModels()

    private val popularSearchAdapter by lazy {
        PopularSearchAdapter { keyword ->
            val bundle = Bundle().apply {
                putString("searchQuery", keyword)
            }
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentSupportSearch, bundle)
        }
    }

    private val supportCategoryAdapter by lazy {
        SupportCategoryAdapter { category ->
            val bundle = Bundle().apply {
                putString("categoryKey", category.key)
                putString("categoryLabel", category.label)
            }
            findNavController().navigate(
                R.id.action_fragmentSupport_to_fragmentSupportViewAllArticles,
                bundle
            )
        }
    }

    private val supportDeviceAdapter by lazy {
        SupportDeviceAdapter { device ->
            val bundle = Bundle().apply {
                putString("categoryKey", "devices")
                putString("categoryLabel", device.deviceLabel ?: "Devices")
                putParcelable("device", device)
            }
            findNavController().navigate(
                R.id.action_fragmentSupport_to_fragmentSupportViewAllArticles,
                bundle
            )
        }
    }

    private val popularArticleAdapter by lazy {
        SupportArticleAdapter { article ->
            val mappedArticle = com.humotron.app.domain.modal.response.SearchTopicItem(
                id = article.topicId ?: article.id,
                subcategoryLabel = article.subcategoryLabel,
                subtitle = article.subtitle,
                categoryKey = article.categoryKey,
                topicId = article.topicId,
                articleType = article.articleType,
                contactReasonCode = article.contactReasonCode,
                deviceScope = article.deviceScope,
                priority = article.priority,
                subcategoryKey = article.subcategoryKey,
                tags = null,
                slug = article.slug,
                title = article.title,
                timeToRead = article.timeToRead,
                viewCount = article.viewCount,
                categoryLabel = article.categoryLabel,
                shortAnswer = null
            )
            val bundle = Bundle().apply {
                putParcelable("article", mappedArticle)
            }
            findNavController().navigate(
                R.id.action_fragmentSupport_to_fragmentSupportArticleDetail,
                bundle
            )
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
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentContactSupport)
        }

        binding.btnViewAllDevices.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentSupportDevices)
        }

        binding.btnViewAllArticles.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentSupportArticles)
        }

        binding.clSearchBar.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentSupportSearch)
        }
        binding.etSearchHelp.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentSupportSearch)
        }
        binding.ivSearchLeft.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentSupportSearch)
        }
        binding.ivSearchRight.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupport_to_fragmentSupportSearch)
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
                        
                        val keywords = response.data.popularSearchKeywords
                        if (!keywords.isNullOrEmpty()) {
                            popularSearchAdapter.setData(keywords)
                            binding.tvPopularSearches.visibility = View.VISIBLE
                            binding.rvPopularSearches.visibility = View.VISIBLE
                        } else {
                            binding.tvPopularSearches.visibility = View.GONE
                            binding.rvPopularSearches.visibility = View.GONE
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

