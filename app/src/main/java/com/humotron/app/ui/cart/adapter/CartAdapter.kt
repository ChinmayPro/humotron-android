package com.humotron.app.ui.cart.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.databinding.ItemCartBinding
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.util.formatCartDate

class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var cartItems: List<GetCartResponse.CartItem> = emptyList()
    private var promoCode: String? = null
    private val loadingItemStates = mutableMapOf<String, String>() // itemId -> "plus" or "minus"

    var onQuantityChanged: ((GetCartResponse.CartItem, Int, String) -> Unit)? = null
    var onDeleteClicked: ((GetCartResponse.CartItem) -> Unit)? = null
    var onEditClicked: ((GetCartResponse.CartItem) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<GetCartResponse.CartItem>, promoCode: String? = null) {
        this.promoCode = promoCode
        loadingItemStates.clear() // Clear loading states when new items are set
        val diffCallback = object : androidx.recyclerview.widget.DiffUtil.Callback() {
            override fun getOldListSize(): Int = cartItems.size
            override fun getNewListSize(): Int = items.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return cartItems[oldItemPosition].id == items[newItemPosition].id
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return cartItems[oldItemPosition] == items[newItemPosition]
            }
        }
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)
        this.cartItems = items
        diffResult.dispatchUpdatesTo(this)
    }

    fun setLoading(itemId: String, isLoading: Boolean, action: String = "") {
        if (isLoading) {
            loadingItemStates[itemId] = action
        } else {
            loadingItemStates.remove(itemId)
        }
        val index = cartItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun clearLoadingStates() {
        loadingItemStates.clear()
        notifyDataSetChanged()
    }

    fun removeItem(itemId: String) {
        val index = cartItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val mutableList = cartItems.toMutableList()
            mutableList.removeAt(index)
            cartItems = mutableList
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: GetCartResponse.CartItem) {
            binding.tvProductName.text = item.productDetails?.name
            
            val totalAmount = item.totalAmount ?: 0.0
            val discountedAmount = item.discountedAmount ?: 0.0
            val itemDiscount = item.itemDiscount ?: 0.0
            val vatAmount = item.vatAmount ?: 0.0

            // Format price helper
            fun formatPrice(amount: Double) = if (amount % 1.0 == 0.0) String.format("%.1f", amount) else String.format("%.2f", amount)

            if (itemDiscount > 0) {
                binding.tvOriginalPrice.visibility = View.VISIBLE
                binding.tvOriginalPrice.text = binding.root.context.getString(R.string.price_with_currency, binding.root.context.getString(R.string.currency_symbol), formatPrice(totalAmount))
                binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                
                binding.tvPrice.text = binding.root.context.getString(R.string.price_with_currency, binding.root.context.getString(R.string.currency_symbol), formatPrice(discountedAmount))
                (binding.tvPrice.layoutParams as ViewGroup.MarginLayoutParams).marginStart = binding.root.context.resources.getDimensionPixelSize(R.dimen._10dp)
                
                binding.llPromoBadge.visibility = View.VISIBLE
                binding.tvPromoCodeInfo.text = binding.root.context.getString(R.string.promo_discount_format, promoCode ?: "", binding.root.context.getString(R.string.currency_symbol), formatPrice(itemDiscount))
            } else {
                binding.tvOriginalPrice.visibility = View.GONE
                binding.tvPrice.text = binding.root.context.getString(R.string.price_with_currency, binding.root.context.getString(R.string.currency_symbol), formatPrice(totalAmount))
                (binding.tvPrice.layoutParams as ViewGroup.MarginLayoutParams).marginStart = 0
                binding.llPromoBadge.visibility = View.GONE
            }

            if (vatAmount > 0) {
                binding.tvVatInfo.visibility = View.VISIBLE
                binding.tvVatInfo.text = binding.root.context.getString(R.string.vat_info_format, binding.root.context.getString(R.string.currency_symbol), formatPrice(vatAmount))
            } else {
                binding.tvVatInfo.visibility = View.GONE
            }

            binding.tvQuantity.text = item.quantity.toString()

            // Bind Variants separately
            binding.tvVariant1.visibility = View.GONE
            binding.tvVariant2.visibility = View.GONE

            when (item.productType) {
                "blood_home", "blood_lab", "blood_self" -> {
                    val variant1 = when (item.productType) {
                        "blood_home" -> "At Home"
                        "blood_lab" -> "Lab Visit"
                        "blood_self" -> "Self Collection"
                        else -> ""
                    }
                    binding.tvVariant1.visibility = View.VISIBLE
                    binding.tvVariant1.text = variant1

                    val addressStr = when (item.productType) {
                        "blood_lab" -> formatLabAddress(item.bookingDetails?.labAddress)
                        else -> {
                            val addr = item.visitAddress
                            if (addr != null) {
                                listOfNotNull(
                                    addr.line1, addr.line2, addr.line3,
                                    addr.city, addr.country, addr.postcode
                                ).filter { it.isNotBlank() }.joinToString(", ")
                            } else null
                        }
                    }

                    if (!addressStr.isNullOrEmpty()) {
                        binding.tvVariant2.visibility = View.VISIBLE
                        binding.tvVariant2.text = addressStr
                    }
                }
                "expert_review" -> {
                    val date = item.bookingDetails?.date
                    val time = item.bookingDetails?.time
                    binding.tvVariant1.visibility = View.VISIBLE
                    binding.tvVariant1.text = "Expert Consultation"
                    if (!date.isNullOrEmpty() || !time.isNullOrEmpty()) {
                        binding.tvVariant2.visibility = View.VISIBLE
                        binding.tvVariant2.text = formatCartDate(date, time)
                    }
                }
                "book" -> {
                    binding.tvVariant1.visibility = View.VISIBLE
                    binding.tvVariant1.text = "Author : ${item.productDetails?.author ?: ""}"
                }
                "device" -> {
                    val color = item.variantDetails?.color
                    val size = item.variantDetails?.size
                    val variants = mutableListOf<String>()
                    if (!color.isNullOrEmpty()) variants.add(color)
                    if (!size.isNullOrEmpty()) variants.add("Size : $size")

                    if (variants.isNotEmpty()) {
                        binding.tvVariant1.visibility = View.VISIBLE
                        binding.tvVariant1.text = variants.joinToString("  |  ")
                    }
                }
            }

            // Load Image
            if (item.productType == "book") {
                binding.ivProductImage.setImageResource(R.drawable.ic_book_biohack)
            } else {
                val imageUrl = item.variantDetails?.image
                Glide.with(binding.ivProductImage.context)
                    .load(imageUrl)
                    .into(binding.ivProductImage)
            }

            // Edit button visibility
            val isBloodTest = item.productType?.startsWith("blood_") == true
            val isBook = item.productType == "book"
            
            binding.ivEdit.visibility = if (!isBook && !isBloodTest) View.VISIBLE else View.GONE
            
            // Quantity controls visibility
            val loadingAction = if (item.id != null) loadingItemStates[item.id] else null
            val isPlusLoading = loadingAction == "plus"
            val isMinusLoading = loadingAction == "minus"
            val showQuantity = !isBloodTest
            
            if (showQuantity) {
                binding.ivPlus.visibility = if (isPlusLoading) View.INVISIBLE else View.VISIBLE
                binding.pbPlus.visibility = if (isPlusLoading) View.VISIBLE else View.GONE

                binding.ivMinus.visibility = if (isMinusLoading) View.INVISIBLE else View.VISIBLE
                binding.pbMinus.visibility = if (isMinusLoading) View.VISIBLE else View.GONE

                binding.tvQuantity.visibility = View.VISIBLE
            } else {
                binding.ivPlus.visibility = View.GONE
                binding.pbPlus.visibility = View.GONE
                binding.ivMinus.visibility = View.GONE
                binding.pbMinus.visibility = View.GONE
                binding.tvQuantity.visibility = View.GONE
            }

            binding.ivEdit.setOnClickListener {
                onEditClicked?.invoke(item)
            }

            // Listeners
            binding.ivPlus.setOnClickListener {
                onQuantityChanged?.invoke(item, (item.quantity ?: 0) + 1, "plus")
            }
            binding.ivMinus.setOnClickListener {
                if ((item.quantity ?: 0) > 1) {
                    onQuantityChanged?.invoke(item, (item.quantity ?: 0) - 1, "minus")
                }
            }
            binding.ivDelete.setOnClickListener {
                onDeleteClicked?.invoke(item)
            }
        }
    }

    private fun formatLabAddress(labAddress: String?): String? {
        if (labAddress.isNullOrEmpty()) return null
        return if (labAddress.startsWith("{") && labAddress.endsWith("}")) {
            try {
                val clean = labAddress.trim().removeSurrounding("{", "}")
                val pairs = clean.split(",")
                val map = pairs.associate {
                    val parts = it.split(":")
                    val key = parts[0].trim()
                    val value = if (parts.size > 1) parts[1].trim().trim { c -> c == '\'' || c == ' ' } else ""
                    key to value
                }

                listOfNotNull(
                    map["line1"], map["line2"], map["city"],
                    map["country"], map["postcode"]
                ).filter { it.isNotBlank() && it != "null" }
                    .joinToString(", ")
            } catch (e: Exception) {
                labAddress
            }
        } else {
            labAddress
        }
    }
}
