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
import com.humotron.app.domain.modal.response.DeviceDetailResponse
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.ui.shop.dialog.ShopDeviceFaqBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopDeviceDetailsFragment : BaseFragment(R.layout.fragment_shop_device_details) {

    private lateinit var binding: FragmentShopDeviceDetailsBinding
    private val viewModel: ShopViewModel by viewModels()
    private var device: GetShopDevicesResponse.Device? = null
    private var faqsList: List<DeviceFaqResponse.FaqData> = emptyList()
    private var isDeviceLiked: Boolean = false
    private var galleryPageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopDeviceDetailsBinding.bind(view)
        
        device = arguments?.getParcelable("device")
        
        setupObservers()
        initViews()

        device?.id?.let { 
            viewModel.fetchDeviceDetail(it) 
            viewModel.fetchDeviceFaqs(it)
        }
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
                        binding.tvWorksWith.text = getString(R.string.works_with_app)

                        val category = detail.deviceCategoryName ?: ""
                        val subCategory = detail.deviceSubCategory?.deviceSubCategoryName ?: ""
                        binding.tvBreadcrumb.text = if (subCategory.isNotEmpty()) {
                            "$category > $subCategory"
                        } else {
                            category
                        }
                        
                        setupGallery(detail.deviceImage ?: emptyList())
                        
                        // Setup Connect with app text with bold "click here"
                        val connectText = getString(R.string.connect_app_desc)
                        val spannable = android.text.SpannableStringBuilder(connectText)
                        val clickHere = getString(R.string.click_here)
                        val clickStart = connectText.indexOf(clickHere)
                        if (clickStart != -1) {
                            spannable.setSpan(
                                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                clickStart,
                                clickStart + clickHere.length,
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
//                            val bundle = Bundle().apply {
//                                putString("deviceId", detail.id)
//                            }
//                            findNavController().navigate(R.id.action_fragmentShopDeviceDetails_to_fragmentShopBuyNow, bundle)
                        }

                        // Share button
                        binding.btnShare.setOnClickListener { _ ->
                            val url = detail.deviceUrl?.firstOrNull() ?: ""
                            val shareText = getString(R.string.share_device_text, url)
                            val sendIntent: android.content.Intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        }

                        // Compare button (Open URL in Browser)
                        binding.btnCompare.setOnClickListener { _ ->
                            val url = detail.deviceUrl?.firstOrNull()
                            if (!url.isNullOrEmpty()) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        // Like button
                        isDeviceLiked = detail.isLiked ?: false
                        binding.btnLike.setImageResource(if (isDeviceLiked) R.drawable.ic_fav_selected else R.drawable.ic_fav_checkbox)
                        var lastClickTime: Long = 0
                        binding.btnLike.setOnClickListener {
                            if (android.os.SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return@setOnClickListener
                            }
                            lastClickTime = android.os.SystemClock.elapsedRealtime()
                            device?.id?.let { id ->
                                isDeviceLiked = !isDeviceLiked
                                binding.btnLike.setImageResource(if (isDeviceLiked) R.drawable.ic_fav_selected else R.drawable.ic_fav_checkbox)
                                viewModel.likeDislikeDevice(id)
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

        viewModel.getDeviceFaqsLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    faqsList = resource.data?.data ?: emptyList()
                    val hasFaqs = faqsList.isNotEmpty()
                    binding.tvFaq.visibility = if (hasFaqs) android.view.View.VISIBLE else android.view.View.GONE
                }
                Status.ERROR, Status.EXCEPTION -> {
                    faqsList = emptyList()
                    binding.tvFaq.visibility = android.view.View.GONE
                }
                Status.LOADING -> {
                    binding.tvFaq.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun setupGallery(images: List<String>) {
        if (images.isEmpty()) return
        
        // Only set the adapter if it's currently null or has changed
        if (binding.vpImageGallery.adapter == null) {
            val adapter = DeviceGalleryAdapter(images)
            binding.vpImageGallery.adapter = adapter
            
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
        }
        
        setupIndicators(images.size)
        
        // Manage listeners correctly
        galleryPageChangeCallback?.let { binding.vpImageGallery.unregisterOnPageChangeCallback(it) }
        galleryPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicator(position)
            }
        }
        galleryPageChangeCallback?.let { binding.vpImageGallery.registerOnPageChangeCallback(it) }
        
        // Initial state
        updateIndicator(binding.vpImageGallery.currentItem)
    }

    private fun setupIndicators(count: Int) {
        // Only re-add if count changed
        if (binding.llIndicator.childCount == count) return
        
        binding.llIndicator.removeAllViews()
        if (count <= 1) return
        
        val margin = dpToPx(6)
        for (i in 0 until count) {
            val dot = android.widget.ImageView(requireContext())
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(margin, 0, margin, 0)
            }
            dot.layoutParams = params
            dot.setImageResource(R.drawable.bg_gallery_indicator_inactive)
            binding.llIndicator.addView(dot)
        }
    }

    private fun updateIndicator(position: Int) {
        if (binding.llIndicator.childCount == 0) return
        
        for (i in 0 until binding.llIndicator.childCount) {
            val child = binding.llIndicator.getChildAt(i)
            if (child is android.widget.ImageView) {
                if (i == position) {
                    child.setImageResource(R.drawable.bg_gallery_indicator_active)
                } else {
                    child.setImageResource(R.drawable.bg_gallery_indicator_inactive)
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvFaq.setOnClickListener {
            val dName = device?.deviceName ?: ""
            if (faqsList.isNotEmpty()) {
                val bottomSheet = ShopDeviceFaqBottomSheet.newInstance(dName, faqsList)
                bottomSheet.show(childFragmentManager, "ShopDeviceFaqBottomSheet")
            }
        }
    }
}
