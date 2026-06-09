package com.humotron.app.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSupportViewAllArticlesBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.util.loadImage
import com.humotron.app.ui.support.adapter.SupportViewAllSubcategoryAdapter
import com.humotron.app.ui.support.adapter.SupportViewAllArticleAdapter
import com.humotron.app.domain.modal.response.SupportConnectedDevice
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportViewAllArticlesFragment : BaseFragment(R.layout.fragment_support_view_all_articles) {

    private lateinit var binding: FragmentSupportViewAllArticlesBinding
    private val viewModel: SupportViewModel by activityViewModels()

    private var categoryKey: String = ""
    private var categoryLabel: String = ""
    private var device: SupportConnectedDevice? = null

    private val subcategoryAdapter by lazy {
        SupportViewAllSubcategoryAdapter { subcat ->
            if (device != null) {
                // If we are on device page, clicking a category navigates to that category's view
                val bundle = Bundle().apply {
                    putString("categoryKey", subcat.key)
                    putString("categoryLabel", subcat.label)
                    putParcelable("device", null)
                }
                findNavController().navigate(
                    R.id.fragmentSupportViewAllArticles,
                    bundle
                )
            } else if (categoryKey.equals("devices", ignoreCase = true)) {
                // If we are on Devices category, clicking a subcategory (device) navigates to its first article details
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
                        R.id.action_fragmentSupportViewAllArticles_to_fragmentSupportArticleDetail,
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
                        R.id.fragmentSupportViewAllArticles,
                        bundle
                    )
                }
            } else {
                val bundle = Bundle().apply {
                    putString("searchQuery", subcat.label)
                }
                findNavController().navigate(
                    R.id.action_fragmentSupportViewAllArticles_to_fragmentSupportSearch,
                    bundle
                )
            }
        }
    }

    private val articleAdapter by lazy {
        SupportViewAllArticleAdapter { articleItem ->
            val bundle = Bundle().apply {
                putParcelable("article", articleItem)
            }
            findNavController().navigate(
                R.id.action_fragmentSupportViewAllArticles_to_fragmentSupportArticleDetail,
                bundle
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportViewAllArticlesBinding.bind(view)

        categoryKey = arguments?.getString("categoryKey") ?: ""
        categoryLabel = arguments?.getString("categoryLabel") ?: ""
        device = arguments?.getParcelable("device")

        val originalPaddingLeft = binding.contentContainer.paddingLeft
        val originalPaddingTop = binding.contentContainer.paddingTop
        val originalPaddingRight = binding.contentContainer.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentContainer.setPadding(
                originalPaddingLeft,
                originalPaddingTop,
                originalPaddingRight,
                systemBars.bottom
            )
            insets
        }

        initViews()
        initObservers()

        if (categoryKey.isNotEmpty()) {
            viewModel.fetchTopicsByCategory(categoryKey)
        }
    }

    private fun initViews() {
        binding.header.title.text = categoryLabel.ifEmpty { getString(R.string.support) }
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.clSearchBar.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupportViewAllArticles_to_fragmentSupportSearch)
        }

        binding.btnContactSupport.setOnClickListener {
            // Handle contact support
        }

        binding.rvPopularTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPopularTopics.adapter = subcategoryAdapter

        binding.rvTopArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopArticles.adapter = articleAdapter
    }

    private fun initObservers() {
        viewModel.topicsByCategoryData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                    binding.contentContainer.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    hideProgress()
                    binding.contentContainer.visibility = View.VISIBLE
                    val response = resource.data
                    if (response?.status == "success" && response.data != null) {
                        val categoryData = response.data
                        val category = categoryData.category

                        // Set search placeholder hint text
                        val currentDevice = device
                        if (currentDevice != null) {
                            binding.tvSearchPlaceholder.text = "Search in ${currentDevice.deviceLabel ?: categoryLabel} support"
                        } else {
                            binding.tvSearchPlaceholder.text = "Search in $categoryLabel"
                        }

                        // 1. Bind Category / Device Overview Card
                        if (currentDevice != null) {
                            // Hide category views
                            binding.flCategoryIconBg.visibility = View.GONE
                            binding.tvCategoryTitle.visibility = View.GONE
                            binding.tvCategoryDesc.visibility = View.GONE
                            binding.clFeaturesRow.visibility = View.GONE

                            // Show device views
                            binding.clDeviceView.visibility = View.VISIBLE
                            binding.tvDeviceTitle.text = currentDevice.deviceLabel ?: categoryLabel
                            binding.tvDeviceStatus.text = currentDevice.status?.replaceFirstChar { it.uppercase() } ?: "Connected"
                            
                            val isConnected = currentDevice.status.equals("connected", ignoreCase = true)
                            if (isConnected) {
                                binding.ivStatusCheck.setImageResource(R.drawable.ic_checked)
                                binding.ivStatusCheck.clearColorFilter()
                                binding.tvDeviceStatus.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorBgBtn))
                                binding.ivConnectivityIcon.visibility = View.VISIBLE
                                binding.ivConnectivityIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorBgBtn))
                            } else {
                                binding.ivStatusCheck.setImageResource(R.drawable.dot_disconnected)
                                binding.ivStatusCheck.clearColorFilter()
                                binding.tvDeviceStatus.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white50))
                                binding.ivConnectivityIcon.visibility = View.GONE
                            }

                            binding.ivDeviceImage.loadImage(currentDevice.deviceUrl, R.drawable.ic_bg_main)

                            binding.btnViewDeviceDetails.setOnClickListener {
                                val userDevice = GetAllDeviceResponse.Data.UserDevice(
                                    id = currentDevice.deviceId,
                                    deviceName = currentDevice.hardwareType,
                                    deviceFacingName = currentDevice.deviceLabel,
                                    dataSync = currentDevice.connectedAt,
                                    deviceCategoryName = null,
                                    deviceImage = if (currentDevice.deviceUrl != null) listOf(currentDevice.deviceUrl) else null,
                                    deviceModelId = null,
                                    deviceModelName = null,
                                    deviceSubCategoryId = null,
                                    deviceSubCategoryName = null,
                                    deviceTextMessage = null,
                                    deviceType = currentDevice.hardwareType,
                                    deviceUrl = if (currentDevice.deviceUrl != null) listOf(currentDevice.deviceUrl) else null,
                                    metrics = null,
                                    orderStatus = currentDevice.status
                                )
                                val bundle = Bundle().apply {
                                    putParcelable("wearable", userDevice)
                                }
                                findNavController().navigate(
                                    R.id.action_fragmentSupportViewAllArticles_to_fragmentDeviceConfig,
                                    bundle
                                )
                            }
                        } else {
                            // Show category views
                            binding.flCategoryIconBg.visibility = View.VISIBLE
                            binding.tvCategoryTitle.visibility = View.VISIBLE
                            binding.tvCategoryDesc.visibility = View.VISIBLE
                            binding.clFeaturesRow.visibility = View.VISIBLE

                            // Hide device views
                            binding.clDeviceView.visibility = View.GONE

                            if (category != null) {
                                binding.tvCategoryTitle.text = category.label ?: categoryLabel
                                binding.tvCategoryDesc.text = category.description ?: ""
                                binding.ivCategoryIcon.loadImage(category.icon, R.drawable.ic_sheet_document)
                                binding.ivCategoryIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorBgBtn1))
                            } else {
                                binding.tvCategoryTitle.text = categoryLabel
                                binding.tvCategoryDesc.text = ""
                                binding.ivCategoryIcon.setImageResource(R.drawable.ic_sheet_document)
                                binding.ivCategoryIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorBgBtn1))
                            }
                        }

                        // 2. Populate Popular Topics (Subcategories / Categories)
                        if (device != null) {
                            // If we are on device page, popular topics is the list of main categories
                            val categories = viewModel.supportHomeData.value?.data?.data?.categories ?: emptyList()
                            val mappedSubcategories = categories.map { categoryItem ->
                                com.humotron.app.domain.modal.response.SupportSubcategory(
                                    visibility = "visible",
                                    isActive = true,
                                    key = categoryItem.key,
                                    label = categoryItem.label,
                                    description = categoryItem.description,
                                    icon = categoryItem.icon,
                                    displayOrder = categoryItem.displayOrder
                                )
                            }
                            if (mappedSubcategories.isNotEmpty()) {
                                binding.tvPopularTopicsHeader.visibility = View.VISIBLE
                                binding.rvPopularTopics.visibility = View.VISIBLE
                                subcategoryAdapter.setData(mappedSubcategories)
                            } else {
                                binding.tvPopularTopicsHeader.visibility = View.GONE
                                binding.rvPopularTopics.visibility = View.GONE
                            }
                        } else {
                            val subcategories = category?.subcategories
                            if (!subcategories.isNullOrEmpty()) {
                                binding.tvPopularTopicsHeader.visibility = View.VISIBLE
                                binding.rvPopularTopics.visibility = View.VISIBLE
                                subcategoryAdapter.setData(subcategories)
                            } else {
                                binding.tvPopularTopicsHeader.visibility = View.GONE
                                binding.rvPopularTopics.visibility = View.GONE
                            }
                        }

                        // 3. Populate Top Articles
                        val topArticles = categoryData.topArticles
                        if (!topArticles.isNullOrEmpty()) {
                            binding.tvTopArticlesHeader.visibility = View.VISIBLE
                            binding.rvTopArticles.visibility = View.VISIBLE
                            articleAdapter.setData(topArticles)
                        } else {
                            binding.tvTopArticlesHeader.visibility = View.GONE
                            binding.rvTopArticles.visibility = View.GONE
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    binding.contentContainer.visibility = View.VISIBLE
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
                Status.EXCEPTION -> {
                    hideProgress()
                    binding.contentContainer.visibility = View.VISIBLE
                    val exceptionMsg = resource.error?.errorMessage ?: getString(R.string.support_exception_occurred)
                    Toast.makeText(requireContext(), exceptionMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
