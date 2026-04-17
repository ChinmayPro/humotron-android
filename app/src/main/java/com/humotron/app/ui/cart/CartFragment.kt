package com.humotron.app.ui.cart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import android.view.ViewGroup
import com.humotron.app.databinding.FragmentCartBinding
import com.humotron.app.ui.cart.adapter.CartAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : BaseFragment(R.layout.fragment_cart) {

    private lateinit var binding: FragmentCartBinding
    private val viewModel: CartViewModel by viewModels()
    private val cartAdapter by lazy { CartAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCartBinding.bind(view)

        setupAdapter()
        setupObservers()
        initViews()
        setupBottomBar()

        binding.layoutLoader.tvLoadingMessage.text = getString(R.string.loading)
        viewModel.fetchCart()
    }

    private fun setupBottomBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnCheckout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // Base margin (20dp) + dynamic navigation bar height
                bottomMargin = systemBars.bottom + dpToPx(20)
            }
            insets
        }
    }

    private fun setupAdapter() {
        binding.rvCartItems.adapter = cartAdapter
        
        cartAdapter.onQuantityChanged = { item, newQuantity ->
            android.util.Log.d("CartFragment", "Update Quantity: ${item.id} -> $newQuantity")
            // Logic to update quantity via API can be added here
        }

        cartAdapter.onDeleteClicked = { item ->
            android.util.Log.d("CartFragment", "Delete Item: ${item.id}")
            // Logic to delete item via API can be added here
        }

        cartAdapter.onEditClicked = { item ->
            val bundle = Bundle().apply {
                putString("deviceId", item.productDetails?.productId)
                putString("cartItemId", item.id)
                putInt("quantity", item.quantity ?: 1)
                putString("variantId", item.variantDetails?.variantId)
            }
            findNavController().navigate(R.id.action_fragmentCart_to_fragmentShopBuyNow, bundle)
        }
    }

    private fun setupObservers() {
        viewModel.getCartLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    val cartItems = resource.data?.data?.cart ?: emptyList()
                    android.util.Log.d("CartFragment", "Cart Data received: ${cartItems.size} items")
                    
                    if (cartItems.isEmpty()) {
                        showEmptyState()
                    } else {
                        showCartItems(resource.data?.data)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideLoading()
                    android.util.Log.e("CartFragment", "Error fetching cart: ${resource.error?.errorMessage}")
                    showEmptyState()
                }
                Status.LOADING -> {
                    showLoading()
                }
            }
        }
    }

    private fun showLoading() {
        binding.layoutLoader.root.visibility = View.VISIBLE
        binding.rvCartItems.visibility = View.GONE
        binding.tvOrderDetailsLabel.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE
        binding.bottomShadow.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.layoutLoader.root.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.tvOrderDetailsLabel.visibility = View.GONE
        binding.rvCartItems.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE
        binding.bottomShadow.visibility = View.GONE
    }

    private fun showCartItems(data: com.humotron.app.domain.modal.response.GetCartResponse.Data?) {
        val items = data?.cart ?: emptyList()
        binding.tvOrderDetailsLabel.visibility = View.VISIBLE
        binding.rvCartItems.visibility = View.VISIBLE
        binding.btnCheckout.visibility = View.VISIBLE
        binding.bottomShadow.visibility = View.VISIBLE
        binding.llCartSummary.visibility = View.VISIBLE
        
        cartAdapter.setItems(items)

        // Bind Address
        data?.address?.let { address ->
            binding.tvAddressName.text = "${address.firstName} ${address.lastName}"
            binding.tvAddressPhone.text = address.contactNo
            
            val addressParts = mutableListOf<String>()
            address.address1?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.address2?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.city?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.country?.let { if (it.isNotEmpty()) addressParts.add(it) }
            address.postcode?.let { if (it.isNotEmpty()) addressParts.add(it) }
            
            binding.tvAddressDetails.text = addressParts.joinToString(", ")
        }

        // Bind Total
        val total = data?.totalAmount ?: 0.0
        binding.tvTotalAmount.text = getString(R.string.currency_symbol) + String.format("%.2f", total)
    }

    private fun initViews() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.header.title.text = getString(R.string.review_details)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
