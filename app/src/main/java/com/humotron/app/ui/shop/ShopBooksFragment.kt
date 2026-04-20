package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentShopBooksBinding
import com.humotron.app.domain.modal.param.AddToCartParam
import com.humotron.app.ui.MainActivity
import com.humotron.app.ui.bioHack.BookSummaryFragment
import com.humotron.app.ui.shop.adapter.ShopBookAdapter
import com.humotron.app.ui.shop.adapter.ShopParentAdapter
import com.humotron.app.ui.shop.adapter.ShopSectionItem
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopBooksFragment : BaseFragment(R.layout.fragment_shop_books), ShopBookAdapter.OnBookItemActions {

    private lateinit var binding: FragmentShopBooksBinding
    private val viewModel: ShopViewModel by viewModels()
    private lateinit var parentAdapter: ShopParentAdapter

    // Map to track ProductId -> CartItemId for removal
    private val cartItemsMap = mutableMapOf<String, String>()
    
    // To identify which book is currently being deleted
    private var bookIdBeingDeleted: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopBooksBinding.bind(view)

        setupAdapter()
        initObservers()
        viewModel.fetchBookPreference()
        // Sync cart items to get their IDs for deletion
        viewModel.fetchCart()
    }

    private fun setupAdapter() {
        parentAdapter = ShopParentAdapter(this) {
            (activity as? MainActivity)?.navigateToBioHack()
        }
        binding.rvShopSections.adapter = parentAdapter
    }

    private fun initObservers() {
        viewModel.getBookPreferenceLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    binding.rvShopSections.visibility = View.VISIBLE

                    val allCategories = resource.data?.data?.books
                    if (allCategories != null && allCategories.isNotEmpty()) {
                        val items = mutableListOf<ShopSectionItem>()
                        
                        // Always add Header
                        items.add(ShopSectionItem.Header)

                        allCategories.forEachIndexed { index, book ->
                            items.add(ShopSectionItem.CategorySection(book))
                            
                            // Insert Nuggets after 1st category
                            if (index == 0) {
                                items.add(ShopSectionItem.NuggetSection)
                            }
                            
                            // Insert Gist after 2nd category
                            if (index == 1) {
                                items.add(ShopSectionItem.GistSection)
                            }
                        }

                        // Always add See More at the end
                        items.add(ShopSectionItem.SeeMoreSection)
                        
                        parentAdapter.setItems(items)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.layoutLoader.root.visibility = View.GONE
                }
                Status.LOADING -> {
                    binding.layoutLoader.root.visibility = View.VISIBLE
                    binding.layoutLoader.tvLoadingMessage.text = getString(R.string.shop_loading_message)
                    binding.layoutLoader.lottieLoader.playAnimation()
                    binding.rvShopSections.visibility = View.GONE
                }
            }
        }

        viewModel.getLikeBookLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.data?.book?.let { likedBook ->
                        parentAdapter.updateLikeStatus(likedBook)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    // Handle error if needed
                }
                else -> {}
            }
        }

        viewModel.getCreateBookCartLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val bookId = resource.data?.data?.id
                    if (bookId != null) {
                        // Update the map with the new cart IDs
                        resource.data?.data?.cart?.forEach { cartItem ->
                            cartItem.productId?.let { pid ->
                                cartItem.id?.let { cid ->
                                    cartItemsMap[pid] = cid
                                }
                            }
                        }
                        
                        parentAdapter.updateAddToCartStatus(bookId, true)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    // Handle error if needed
                }
                else -> {}
            }
        }

        // Observe cart data to populate initial IDs
        viewModel.getCartLiveData().observe(viewLifecycleOwner) { resource ->
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

        // Observe deletion results
        viewModel.getDeleteCartItemLiveData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                bookIdBeingDeleted?.let { bookId ->
                    // Update adapters to show "Add to Cart" state
                    parentAdapter.updateAddToCartStatus(bookId, false)
                    
                    // Cleanup map
                    cartItemsMap.remove(bookId)
                }
                bookIdBeingDeleted = null
                
                // Refresh cart to stay in sync
                viewModel.fetchCart()
            }
        }
    }


    override fun likeBooks(bookId: String) {
        viewModel.likeBook(bookId)
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
            // Book already in cart, call delete API
            val cartItemId = cartItemsMap[bookId]
            if (cartItemId != null) {
                bookIdBeingDeleted = bookId
                viewModel.deleteCartItem(cartItemId)
            }
        } else {
            // Book not in cart, call add API
            viewModel.createBookCart(
                AddToCartParam(
                    productId = bookId,
                    quantity = 1,
                    variantId = "",
                    productType = "book",
                    cartItemId = ""
                )
            )
        }
    }
}
