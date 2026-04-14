package com.humotron.app.ui.shop

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentShopBuyNowBinding
import com.humotron.app.domain.modal.response.ColorVariant
import com.humotron.app.domain.modal.response.SizeVariant
import com.humotron.app.ui.shop.adapter.ColorVariantAdapter
import com.humotron.app.ui.shop.adapter.SizeVariantAdapter
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopBuyNowFragment : BaseFragment(R.layout.fragment_shop_buy_now) {

    private lateinit var binding: FragmentShopBuyNowBinding
    private val viewModel: ShopBuyNowViewModel by viewModels()
    private var deviceId: String? = null
    private var isLiked: Boolean = false
    private var deviceUrl: String? = null
    private var cartItemId: String? = null
    private var isInitialLoad: Boolean = true
    private var preSelectedVariantId: String? = null
    
    private var colorAdapter: ColorVariantAdapter? = null
    private var sizeAdapter: SizeVariantAdapter? = null
    
    private var allSizes: List<SizeVariant> = emptyList()
    private var selectedSize: SizeVariant? = null
    private var quantity: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopBuyNowBinding.bind(view)

        // Proper Solution for Navigation Bar Clearance
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnContinue) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // Base margin (20dp) + dynamic navigation bar height
                bottomMargin = systemBars.bottom + dpToPx(20)
            }
            insets
        }

        deviceId = arguments?.getString("deviceId")
        cartItemId = arguments?.getString("cartItemId")
        quantity = arguments?.getInt("quantity", 1) ?: 1
        preSelectedVariantId = arguments?.getString("variantId")
        
        binding.layoutLoader.tvLoadingMessage.text = getString(R.string.loading_product_variant)
        
        setupObservers()
        initViews()

        deviceId?.let {
            viewModel.getProductVariantById(it)
        } ?: run {
            Toast.makeText(requireContext(), "Device ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.getProductVariantLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.layoutLoader.root.visibility = android.view.View.GONE
                    binding.nsvContent.visibility = android.view.View.VISIBLE
                    binding.tvNoData.visibility = android.view.View.GONE

                    val data = resource.data?.data
                    Log.d("ShopBuyNowFragment", "API Success: ${data?.deviceName}")
                    
                    // Initialize state from data
                    isLiked = data?.isLiked ?: false
                    deviceUrl = data?.deviceUrl?.firstOrNull()
                    
                    binding.btnLike.setImageResource(if (isLiked) R.drawable.ic_fav_selected else R.drawable.ic_fav_checkbox)

                    // Bind Product Info
                    binding.tvDeviceName.text = data?.deviceName
                    binding.tvPrice.text = getString(R.string.currency_symbol) + (data?.price ?: "")

                    // Show size section only if isUniversal is false
                    val isSizeVisible = data?.isUniversal == false
                    binding.clSizeHeader.visibility = if (isSizeVisible) android.view.View.VISIBLE else android.view.View.GONE
                    binding.rvSizes.visibility = if (isSizeVisible) android.view.View.VISIBLE else android.view.View.GONE

                    // Setup Gallery (Main Image)
                    val initialImage = data?.color?.firstOrNull()?.image?.firstOrNull()
                    initialImage?.let {
                        Glide.with(requireContext()).load(it).into(binding.ivProduct)
                    }

                    // Setup Sizes
                    data?.size?.let { sizes ->
                        allSizes = sizes
                        val targetVariant = if (isInitialLoad) sizes.find { it.id == preSelectedVariantId } else null
                        val initialColorName = targetVariant?.colorName ?: data.color?.firstOrNull()?.colorName
                        
                        // Setup Colors
                        data.color?.let { colors ->
                            colorAdapter = ColorVariantAdapter(colors) { selectedColor ->
                                updateSelectedColorDetails(selectedColor)
                                filterSizesByColor(selectedColor.colorName)
                            }
                            binding.rvColors.adapter = colorAdapter
                            if (isInitialLoad && initialColorName != null) {
                                colorAdapter?.setSelectedPositionByColorName(initialColorName)
                                updateSelectedColorDetails(colors.find { it.colorName == initialColorName } ?: colors[0])
                            }
                        }

                        filterSizesByColor(initialColorName)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.layoutLoader.root.visibility = android.view.View.GONE
                    binding.nsvContent.visibility = android.view.View.GONE
                    binding.tvNoData.visibility = android.view.View.VISIBLE
                    
                    Log.e("ShopBuyNowFragment", "API Error: ${resource.error?.errorMessage}")
                    Toast.makeText(requireContext(), "Error: ${resource.error?.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    binding.layoutLoader.root.visibility = android.view.View.VISIBLE
                    binding.nsvContent.visibility = android.view.View.GONE
                    binding.tvNoData.visibility = android.view.View.GONE
                    
                    Log.d("ShopBuyNowFragment", "API Loading...")
                }
            }
        }

        viewModel.getAddToCartLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideCartLoading()
                    if (cartItemId.isNullOrEmpty()) {
                        findNavController().navigate(R.id.action_fragmentShopBuyNow_to_fragmentCart)
                    } else {
                        findNavController().popBackStack()
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideCartLoading()
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Failed to add to cart", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    showCartLoading()
                }
            }
        }
    }

    private fun showCartLoading() {
        binding.layoutLoader.root.visibility = android.view.View.VISIBLE
        binding.layoutLoader.tvLoadingMessage.text = "Adding to cart..."
    }

    private fun hideCartLoading() {
        binding.layoutLoader.root.visibility = android.view.View.GONE
    }

    private fun initViews() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        // Share button
        binding.btnShare.setOnClickListener {
            if (!deviceUrl.isNullOrEmpty()) {
                val shareText = getString(R.string.share_device_text, deviceUrl)
                val sendIntent: android.content.Intent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }

        // Compare button (Open URL in Browser)
        binding.btnCompare.setOnClickListener {
            if (!deviceUrl.isNullOrEmpty()) {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(deviceUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Like button
        var lastClickTime: Long = 0
        binding.btnLike.setOnClickListener {
            if (android.os.SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = android.os.SystemClock.elapsedRealtime()
            
            deviceId?.let { id ->
                isLiked = !isLiked
                binding.btnLike.setImageResource(if (isLiked) R.drawable.ic_fav_selected else R.drawable.ic_fav_checkbox)
                viewModel.likeDislikeDevice(id)
            }
        }

        // Quantity Buttons
        binding.btnPlus.setOnClickListener {
            selectedSize?.quantity?.let { stock ->
                if (quantity < stock) {
                    quantity++
                    updateQuantityUi()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.msg_out_of_stock, stock), Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.msg_select_size_first), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityUi()
            }
        }

        // Continue Button
        binding.btnContinue.setOnClickListener {
            val data = viewModel.getProductVariantLiveData().value?.data?.data
            val isUniversal = data?.isUniversal ?: false
            
            if (!isUniversal && selectedSize == null) {
                Toast.makeText(requireContext(), getString(R.string.msg_select_size), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val param = com.humotron.app.domain.modal.param.AddToCartParam(
                productId = deviceId,
                variantId = selectedSize?.id ?: "",
                productType = "device",
                quantity = quantity,
                cartItemId = cartItemId ?: ""
            )
            
            viewModel.addToCart(param)
        }

        updateQuantityUi()
    }

    private fun filterSizesByColor(colorName: String?) {
        val filtered = allSizes.filter { it.colorName == colorName }
        if (sizeAdapter == null) {
            sizeAdapter = SizeVariantAdapter(filtered) { size ->
                selectedSize = size
                if (!isInitialLoad) {
                    quantity = 1 // Reset quantity on size change
                }
                updateQuantityUi()
                
                // Update price if size-specific
                size.price?.let {
                    binding.tvPrice.text = getString(R.string.currency_symbol) + it
                }
            }
            binding.rvSizes.adapter = sizeAdapter
        }
        
        sizeAdapter?.updateList(filtered, if (isInitialLoad) preSelectedVariantId else null)
        
        // Auto-select first size for the new color (this is now handled by updateList)
        if (filtered.isNotEmpty()) {
            val index = if (isInitialLoad) {
                filtered.indexOfFirst { it.id == preSelectedVariantId }.takeIf { it != -1 } ?: 0
            } else {
                0
            }
            selectedSize = filtered[index]
            isInitialLoad = false
        }
    }

    private fun updateQuantityUi() {
        binding.tvQuantity.text = String.format("%02d", quantity)
        
        // Opt: Disable/Enable buttons based on limits
        binding.btnMinus.alpha = if (quantity > 1) 1.0f else 0.5f
        selectedSize?.quantity?.let { stock ->
            binding.btnPlus.alpha = if (quantity < stock) 1.0f else 0.5f
        }
    }

    private fun updateSelectedColorDetails(color: ColorVariant) {
        // Update Gallery (Main Image)
        color.image?.firstOrNull()?.let { imageUrl ->
            Glide.with(requireContext()).load(imageUrl).into(binding.ivProduct)
        }

        // Update price if size-specific
        color.price?.let { price ->
            binding.tvPrice.text = getString(R.string.currency_symbol) + price
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
