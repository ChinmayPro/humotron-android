package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentShopDeviceDetailsBinding
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.net.Uri
import com.humotron.app.ui.shop.adapter.ShopMetricAdapter
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.humotron.app.ui.shop.adapter.DeviceGalleryAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopDeviceDetailsFragment : BaseFragment(R.layout.fragment_shop_device_details) {

    private lateinit var binding: FragmentShopDeviceDetailsBinding
    private val viewModel: ShopViewModel by viewModels()
    private var device: GetShopDevicesResponse.Device? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopDeviceDetailsBinding.bind(view)
        
        device = arguments?.getParcelable("device")
        
        setupObservers()
        initViews()

        device?.id?.let { viewModel.fetchDeviceDetail(it) }
    }

    private fun setupObservers() {
        viewModel.getDeviceDetailLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.layoutLoader.root.visibility = android.view.View.GONE
                    binding.nsvContent.visibility = android.view.View.VISIBLE
                    binding.tvNoData.visibility = android.view.View.GONE
                    
                    val detail = resource.data?.data?.firstOrNull()
                    if (detail != null) {
                        binding.tvNoData.visibility = android.view.View.GONE
                        binding.nsvContent.visibility = android.view.View.VISIBLE
                        
                        binding.tvDeviceName.text = detail.deviceFacingName ?: detail.deviceName
                        
                        binding.tvPrice.text = "£${detail.deviceModel?.deviceModelPrice}"
                        binding.tvDescription.text = detail.deviceCategory?.deviceCategoryLongDesc
                        binding.tvWorksWith.text = "Works with: Humotron App"

                        val category = detail.deviceCategoryName ?: ""
                        val subCategory = detail.deviceSubCategory?.deviceSubCategoryName ?: ""
                        binding.tvBreadcrumb.text = if (subCategory.isNotEmpty()) {
                            "$category > $subCategory"
                        } else {
                            category
                        }
                        
                        setupGallery(detail.deviceImage ?: emptyList())
                        
                        // Setup Connect with app text with bold "click here"
                        val connectText = "If you already have a device and\nwant to connect with app, click here >"
                        val spannable = android.text.SpannableStringBuilder(connectText)
                        val clickStart = connectText.indexOf("click here")
                        if (clickStart != -1) {
                            spannable.setSpan(
                                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                clickStart,
                                clickStart + "click here".length,
                                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        binding.tvConnectApp.text = spannable

                        // Setup metrics
                        detail.metrics?.let { metrics ->
                            binding.rvMetrics.adapter = ShopMetricAdapter(metrics)
                        }

                        // Buy Now button
                        binding.btnBuyNow.setOnClickListener { _ ->
                            val url = detail.deviceUrl?.firstOrNull()
                            if (!url.isNullOrEmpty()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        binding.tvNoData.visibility = android.view.View.VISIBLE
                        binding.nsvContent.visibility = android.view.View.GONE
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.layoutLoader.root.visibility = android.view.View.GONE
                    binding.tvNoData.visibility = android.view.View.VISIBLE
                }
                Status.LOADING -> {
                    binding.layoutLoader.root.visibility = android.view.View.VISIBLE
                    binding.nsvContent.visibility = android.view.View.GONE
                    binding.tvNoData.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun setupGallery(images: List<String>) {
        if (images.isEmpty()) return
        
        val adapter = DeviceGalleryAdapter(images)
        binding.vpImageGallery.adapter = adapter
        
        setupIndicators(images.size)
        
        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimensionPixelSize(R.dimen._20dp)))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
            page.alpha = 0.5f + r * 0.5f
        }
        
        binding.vpImageGallery.apply {
            setPageTransformer(compositePageTransformer)
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        
        binding.vpImageGallery.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicator(position)
            }
        })
    }

    private fun setupIndicators(count: Int) {
        binding.llIndicator.removeAllViews()
        if (count <= 1) return
        
        for (i in 0 until count) {
            val dot = android.widget.ImageView(requireContext())
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 8, 0)
            }
            dot.layoutParams = params
            dot.setImageResource(R.drawable.bg_gallery_indicator_inactive)
            binding.llIndicator.addView(dot)
        }
        updateIndicator(0)
    }

    private fun updateIndicator(position: Int) {
        for (i in 0 until binding.llIndicator.childCount) {
            val dot = binding.llIndicator.getChildAt(i) as android.widget.ImageView
            if (i == position) {
                dot.setImageResource(R.drawable.bg_gallery_indicator_active)
            } else {
                dot.setImageResource(R.drawable.bg_gallery_indicator_inactive)
            }
        }
    }

    private fun initViews() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
