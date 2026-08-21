package com.humotron.app.ui.shop

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentShopDeviceDetailsBinding
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.ui.shop.adapter.DeviceGalleryAdapter
import com.humotron.app.ui.shop.adapter.ShopMetricAdapter
import com.humotron.app.ui.shop.dialog.ShopDeviceFaqBottomSheet
import dagger.hilt.android.AndroidEntryPoint

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@AndroidEntryPoint
class ShopDeviceDetailsFragment : BaseFragment(R.layout.fragment_shop_device_details) {

    private lateinit var binding: FragmentShopDeviceDetailsBinding
    private val viewModel: ShopViewModel by viewModels()
    private var device: GetShopDevicesResponse.Device? = null
    private var faqsList: List<DeviceFaqResponse.FaqData> = emptyList()
    private var isDeviceLiked: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopDeviceDetailsBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.nsvContent) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                systemBars.bottom + dpToPx(24)
            )
            insets
        }

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
                    binding.layoutLoader.root.visibility = View.GONE
                    binding.nsvContent.visibility = View.VISIBLE
                    binding.tvNoData.visibility = View.GONE

                    val detail = resource.data?.data?.firstOrNull()
                    if (detail != null) {
                        binding.tvNoData.visibility = View.GONE
                        binding.nsvContent.visibility = View.VISIBLE

                        val deviceTitle = detail.deviceFacingName ?: detail.deviceName ?: device?.deviceFacingName ?: device?.deviceName ?: ""
                        binding.tvDeviceName.text = deviceTitle

                        val rawPrice = detail.deviceModel?.deviceModelPrice ?: device?.deviceModel?.deviceModelPrice ?: ""
                        val formattedPrice = if (rawPrice.startsWith("£")) rawPrice else "£$rawPrice"
                        binding.tvPrice.text = formattedPrice
                        binding.tvDescription.text = detail.deviceCategory?.deviceCategoryLongDesc ?: detail.deviceTextMessage ?: ""
                        binding.tvWorksWith.text = "Works with the Humotron App"

                        val category = detail.deviceCategoryName ?: detail.deviceCategory?.deviceCategoryName ?: ""
                        val subCategory = detail.deviceSubCategory?.deviceSubCategoryName ?: ""
                        binding.tvBreadcrumb.text = if (subCategory.isNotEmpty()) {
                            "$category › $subCategory"
                        } else {
                            category
                        }

                        // Combine args & API detail attributes (handling backend keys like BPMACHINE, WEIGHTMACHINE, URINESTRIP)
                        val argName = device?.deviceName ?: ""
                        val argFacing = device?.deviceFacingName ?: ""

                        val detailName = detail.deviceName ?: ""
                        val detailFacing = detail.deviceFacingName ?: ""
                        val detailCategory = detail.deviceCategoryName ?: detail.deviceCategory?.deviceCategoryName ?: ""
                        val detailSubCategory = detail.deviceSubCategory?.deviceSubCategoryName ?: ""

                        val combinedText = "$argName $argFacing $detailName $detailFacing $detailCategory $detailSubCategory"
                        val heroIconRes = getDeviceHeroIcon(combinedText)
                        val accentColor = getDeviceAccentColor(combinedText)

                        val images = (detail.deviceImage ?: device?.deviceImage)?.filter { !it.isNullOrBlank() }
                        if (!images.isNullOrEmpty()) {
                            binding.vpImageGallery.adapter = DeviceGalleryAdapter(images, heroIconRes)
                            binding.vpImageGallery.visibility = View.VISIBLE
                            binding.ivDeviceHeroIcon.visibility = View.GONE

                            binding.llIndicator.visibility = View.GONE
                        } else {
                            binding.ivDeviceHeroIcon.setImageResource(heroIconRes)
                            binding.ivDeviceHeroIcon.clearColorFilter() // Preserve native 1.8dp vector line paths
                            binding.ivDeviceHeroIcon.visibility = View.VISIBLE
                            binding.vpImageGallery.visibility = View.GONE
                            binding.llIndicator.visibility = View.GONE
                        }

                        // Setup dynamic radial background glow matching device accent color
                        val r = Color.red(accentColor)
                        val g = Color.green(accentColor)
                        val b = Color.blue(accentColor)
                        val glowDrawable = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            gradientType = GradientDrawable.RADIAL_GRADIENT
                            gradientRadius = dpToPx(85).toFloat()
                            colors = intArrayOf(
                                Color.argb(80, r, g, b),
                                Color.argb(20, r, g, b),
                                Color.argb(0, r, g, b)
                            )
                        }
                        binding.vHeroGlow.background = glowDrawable

                        // Floating Pill is ALWAYS bright lime green matching HTML .phero .pill
                        binding.vHeroPill.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_hero_pill_glowing)
                        binding.vHeroPill.visibility = View.VISIBLE

                        // Setup Connect with app text matching HTML design
                        val fullText = "Already own this device? Connect it to the app ›"
                        val highlightText = "Connect it to the app ›"
                        val spannable = SpannableStringBuilder(fullText)
                        val start = fullText.indexOf(highlightText)
                        if (start != -1) {
                            spannable.setSpan(
                                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.lime)),
                                start,
                                start + highlightText.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                start + highlightText.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        binding.tvConnectApp.text = spannable

                        // Setup metrics
                        detail.metrics?.let { metrics ->
                            binding.rvMetrics.adapter = ShopMetricAdapter(metrics)
                        }

                        // Buy Now button
                        binding.btnBuyNow.setOnClickListener { _ ->
                            val bundle = Bundle().apply {
                                putString("deviceId", detail.id)
                            }
                            findNavController().navigate(R.id.action_fragmentShopDeviceDetails_to_fragmentShopBuyNow, bundle)
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
                        updateLikeButtonState(isDeviceLiked)
                        var lastClickTime: Long = 0
                        binding.btnLike.setOnClickListener {
                            if (android.os.SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return@setOnClickListener
                            }
                            lastClickTime = android.os.SystemClock.elapsedRealtime()
                            device?.id?.let { id ->
                                isDeviceLiked = !isDeviceLiked
                                updateLikeButtonState(isDeviceLiked)
                                viewModel.likeDislikeDevice(id)
                            }
                        }
                    } else {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.nsvContent.visibility = View.GONE
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    binding.tvNoData.visibility = View.VISIBLE
                }
                Status.LOADING -> {
                    binding.layoutLoader.root.visibility = View.VISIBLE
                    binding.nsvContent.visibility = View.GONE
                    binding.tvNoData.visibility = View.GONE
                }
            }
        }

        viewModel.getDeviceFaqsLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    faqsList = resource.data?.data ?: emptyList()
                    val hasFaqs = faqsList.isNotEmpty()
                    binding.llFaqContainer.visibility = if (hasFaqs) View.VISIBLE else View.GONE
                }
                Status.ERROR, Status.EXCEPTION -> {
                    faqsList = emptyList()
                    binding.llFaqContainer.visibility = View.GONE
                }
                Status.LOADING -> {
                    binding.llFaqContainer.visibility = View.GONE
                }
            }
        }
    }

    private fun getDeviceHeroIcon(text: String): Int {
        val name = text.uppercase()
        return when {
            name.contains("BPMACHINE") || name.contains("CUFF") || name.contains("PRESSURE") || name.contains("BLOOD") -> R.drawable.ic_smart_cuff_vector
            name.contains("RING") -> R.drawable.ic_ring_vector
            name.contains("BAND") || name.contains("WRIST") -> R.drawable.ic_band_vectr
            name.contains("URINE") || name.contains("FLASK") || name.contains("TEST") || name.contains("STRIP") -> R.drawable.ic_opt_flask
            name.contains("WEIGHT") || name.contains("SCALE") -> R.drawable.ic_smart_scale_hero
            else -> R.drawable.ic_smart_scale_hero
        }
    }

    private fun getDeviceAccentColor(text: String): Int {
        val name = text.uppercase()
        return when {
            name.contains("BPMACHINE") || name.contains("CUFF") || name.contains("PRESSURE") || name.contains("BLOOD") -> ContextCompat.getColor(requireContext(), R.color.cool) // Cool Teal #5FB7C4
            name.contains("BAND") || name.contains("WRIST") -> Color.parseColor("#E6A2B5") // Pink / Coral
            name.contains("URINE") || name.contains("FLASK") || name.contains("TEST") || name.contains("STRIP") -> ContextCompat.getColor(requireContext(), R.color.watch) // Gold / Yellow
            name.contains("RING") -> ContextCompat.getColor(requireContext(), R.color.lime)
            name.contains("WEIGHT") || name.contains("SCALE") -> ContextCompat.getColor(requireContext(), R.color.lime)
            else -> ContextCompat.getColor(requireContext(), R.color.lime)
        }
    }

    private fun updateLikeButtonState(liked: Boolean) {
        if (liked) {
            binding.btnLike.setImageResource(R.drawable.ic_fav_selected)
            binding.btnLike.imageTintList = null
            binding.btnLike.alpha = 1.0f
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_fav_checkbox)
            binding.btnLike.imageTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.white))
            binding.btnLike.alpha = 0.7f
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.llFaqContainer.setOnClickListener {
            val dName = device?.deviceName ?: ""
            if (faqsList.isNotEmpty()) {
                val bottomSheet = ShopDeviceFaqBottomSheet.newInstance(dName, faqsList)
                bottomSheet.show(childFragmentManager, "ShopDeviceFaqBottomSheet")
            }
        }
    }
}
