package com.humotron.app.ui.cart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
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

        viewModel.fetchCart()
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
                    val cartItems = resource.data?.data?.cart ?: emptyList()
                    android.util.Log.d("CartFragment", "Cart Data received: ${cartItems.size} items")
                    
                    if (cartItems.isEmpty()) {
                        showEmptyState()
                    } else {
                        showCartItems(cartItems)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    android.util.Log.e("CartFragment", "Error fetching cart: ${resource.error?.errorMessage}")
                    showEmptyState()
                }
                Status.LOADING -> {
                    // Show generic loading if needed
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.groupEmptyCart.visibility = View.VISIBLE
        binding.tvOrderDetailsLabel.visibility = View.GONE
        binding.rvCartItems.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE
    }

    private fun showCartItems(items: List<com.humotron.app.domain.modal.response.GetCartResponse.CartItem>) {
        binding.groupEmptyCart.visibility = View.GONE
        binding.tvOrderDetailsLabel.visibility = View.VISIBLE
        binding.rvCartItems.visibility = View.VISIBLE
        binding.btnCheckout.visibility = View.VISIBLE
        cartAdapter.setItems(items)
    }

    private fun initViews() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.header.title.text = getString(R.string.review_details)
    }
}
