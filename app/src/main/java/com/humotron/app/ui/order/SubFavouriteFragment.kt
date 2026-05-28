package com.humotron.app.ui.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSubFavouriteBinding
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.domain.modal.response.*
import com.humotron.app.ui.bioHack.BookSummaryFragment
import com.humotron.app.ui.order.adapter.FavouriteParentAdapter
import com.humotron.app.ui.order.adapter.FavouriteUIItem
import com.humotron.app.ui.shop.ShopViewModel
import com.humotron.app.ui.shop.adapter.ShopBookAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubFavouriteFragment : BaseFragment(R.layout.fragment_sub_favourite), ShopBookAdapter.OnBookItemActions {
    private lateinit var binding: FragmentSubFavouriteBinding
    private val viewModel: OrderViewModel by viewModels()
    private val shopViewModel: ShopViewModel by viewModels()
    private val cartItemsMap = mutableMapOf<String, String>()
    private var bookIdBeingDeleted: String? = null
    private val parentAdapter by lazy {
        FavouriteParentAdapter(
            action = this,
            onDeviceClick = { device -> 
                val bundle = Bundle().apply {
                    putParcelable("device", device)
                }
                parentFragment?.findNavController()?.navigate(R.id.action_fragmentOrder_to_fragmentShopDeviceDetails, bundle)
            },
            onProductClick = { product ->
                // Ensure we have a valid productId for the detail screen
                val effectiveProductId = if (product.productId.isNullOrEmpty()) product.id else product.productId
                val productToPass = product.copy(productId = effectiveProductId)
                
                val bundle = Bundle().apply {
                    putParcelable("supplement", productToPass)
                }
                parentFragment?.findNavController()?.navigate(R.id.action_fragmentOrder_to_fragmentShopOptimizeDetail, bundle)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubFavouriteBinding.bind(view)
        setupRecyclerView()
        setupObservers()
        shopViewModel.fetchCart()
    }

    private fun setupRecyclerView() {
        binding.rvFavourite.apply {
            adapter = parentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private var currentUIItems = mutableListOf<FavouriteUIItem>()

    private fun setupObservers() {
        viewModel.getAllLikesLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (parentAdapter.itemCount == 0) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvNoData.visibility = View.GONE
                    }
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    val response = resource.data
                    if (response?.status == "fail") {
                        currentUIItems.clear()
                        parentAdapter.setItems(ArrayList(currentUIItems))
                        binding.tvNoData.visibility = View.VISIBLE
                        val errorMsg = if (!response.message.isNullOrEmpty()) response.message else getString(R.string.something_went_wrong)
                        android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        val data = response?.data
                        currentUIItems.clear()

                        // 1. Books Section
                        data?.books?.let { books ->
                            if (books.isNotEmpty()) {
                                currentUIItems.add(FavouriteUIItem.Header("Books"))
                                currentUIItems.add(FavouriteUIItem.BookCarousel(
                                    BookPreferenceResponse.BookData.Book(
                                        bookRecommendation = books,
                                        category = null,
                                        primaryTag = null
                                    )
                                ))
                            }
                        }

                        // 2. Devices Section
                        data?.devices?.let { devices ->
                            if (devices.isNotEmpty()) {
                                currentUIItems.add(FavouriteUIItem.Header("Device"))
                                devices.forEach { device ->
                                    currentUIItems.add(FavouriteUIItem.Device(device))
                                }
                            }
                        }

                        // 3. Products/Supplements Section
                        data?.products?.let { products ->
                            if (products.isNotEmpty()) {
                                currentUIItems.add(FavouriteUIItem.Header("Suppliments"))
                                products.forEach { product ->
                                    currentUIItems.add(FavouriteUIItem.Product(product))
                                }
                            }
                        }

                        parentAdapter.setItems(ArrayList(currentUIItems))
                        binding.tvNoData.visibility = if (currentUIItems.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvNoData.visibility = if (currentUIItems.isEmpty()) View.VISIBLE else View.GONE
                    val errorMsg = getErrorMessage(resource.error)
                    android.widget.Toast.makeText(requireContext(), errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        shopViewModel.getLikeBookLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    // Success! Already updated locally in likeBooks
                }
                Status.ERROR, Status.EXCEPTION -> {
                    // If API fails, we might want to refresh to get correct state
                    fetchData()
                }
            }
        }

        // Add Cart observer to populate initial IDs
        shopViewModel.getCartLiveData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                resource.data?.data?.cart?.forEach { cartItem ->
                    if (cartItem.productType == "book") {
                        cartItem.productDetails?.productId?.let { pid ->
                            cartItem.id?.let { cid ->
                                cartItemsMap[pid] = cid
                            }
                        }
                    }
                }
            }
        }

        shopViewModel.getCreateBookCartLiveData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                val data = resource.data?.data
                val bookId = data?.id
                if (bookId != null) {
                    data.cart?.forEach { cartItem ->
                        cartItem.productId?.let { pid ->
                            cartItem.id?.let { cid ->
                                cartItemsMap[pid] = cid
                            }
                        }
                    }
                    updateCartUIStatus(bookId, true)
                }
            }
        }

        shopViewModel.getDeleteCartItemLiveData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                bookIdBeingDeleted?.let { bookId ->
                    cartItemsMap.remove(bookId)
                    updateCartUIStatus(bookId, false)
                }
                bookIdBeingDeleted = null
            }
        }
    }

    private fun updateCartUIStatus(bookId: String, isInCart: Boolean) {
        val updatedList = currentUIItems.toMutableList()
        val carouselIndex = updatedList.indexOfFirst { it is FavouriteUIItem.BookCarousel }
        
        if (carouselIndex != -1) {
            val item = updatedList[carouselIndex] as FavouriteUIItem.BookCarousel
            val recommendations = item.books.bookRecommendation?.toMutableList() ?: mutableListOf()
            val bookIndex = recommendations.indexOfFirst { it.id == bookId }
            
            if (bookIndex != -1) {
                recommendations[bookIndex] = recommendations[bookIndex].copy(isCart = isInCart)
                updatedList[carouselIndex] = FavouriteUIItem.BookCarousel(
                    item.books.copy(bookRecommendation = recommendations)
                )
                currentUIItems = updatedList
                parentAdapter.setItems(ArrayList(currentUIItems))
            }
        }
    }

    override fun likeBooks(bookId: String) {
        // Optimistic UI update: Remove locally first
        val updatedList = mutableListOf<FavouriteUIItem>()
        var i = 0
        while (i < currentUIItems.size) {
            val item = currentUIItems[i]
            if (item is FavouriteUIItem.BookCarousel) {
                val filteredBooks = item.books.bookRecommendation?.filter { it.id != bookId }
                if (!filteredBooks.isNullOrEmpty()) {
                    updatedList.add(FavouriteUIItem.BookCarousel(
                        item.books.copy(bookRecommendation = filteredBooks)
                    ))
                    i++
                } else {
                    // This carousel is now empty. Skip it AND its preceding Header if it was "Books"
                    if (updatedList.isNotEmpty() && updatedList.last() is FavouriteUIItem.Header) {
                        val lastHeader = updatedList.last() as FavouriteUIItem.Header
                        if (lastHeader.title == "Books") {
                            updatedList.removeAt(updatedList.size - 1)
                        }
                    }
                    i++
                }
            } else {
                updatedList.add(item)
                i++
            }
        }
        
        currentUIItems = updatedList
        parentAdapter.setItems(ArrayList(currentUIItems))
        binding.tvNoData.visibility = if (currentUIItems.isEmpty()) View.VISIBLE else View.GONE
        
        // Then call API
        shopViewModel.likeBook(bookId)
    }

    override fun openSummary(bookId: String) {
        val dialog = BookSummaryFragment()
        val bundle = Bundle()
        bundle.putString("bookId", bookId)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "book_summary")
    }

    override fun addToCart(bookId: String) {
        if (cartItemsMap.containsKey(bookId)) {
            val cartItemId = cartItemsMap[bookId]
            if (cartItemId != null) {
                bookIdBeingDeleted = bookId
                shopViewModel.deleteCartItem(cartItemId)
            }
        } else {
            val param = AddToCartParam(
                productId = bookId,
                productType = "book",
                quantity = 1,
                variantId = "",
                cartItemId = ""
            )
            shopViewModel.createBookCart(param)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    fun fetchData() {
        viewModel.fetchAllLikes()
    }

    private fun getErrorMessage(error: com.humotron.app.data.network.error.Error?): String {
        if (error == null) return getString(R.string.something_went_wrong)

        if (!error.errorMessage.isNullOrEmpty()) return error.errorMessage

        val rawError = error.error
        if (!rawError.isNullOrEmpty()) {
            return try {
                val json = org.json.JSONObject(rawError)
                when {
                    json.has("message") -> json.getString("message")
                    json.has("error") -> json.getString("error")
                    else -> rawError
                }
            } catch (e: Exception) {
                rawError
            }
        }

        return getString(R.string.something_went_wrong)
    }
}
