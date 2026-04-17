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

    var onQuantityChanged: ((GetCartResponse.CartItem, Int) -> Unit)? = null
    var onDeleteClicked: ((GetCartResponse.CartItem) -> Unit)? = null
    var onEditClicked: ((GetCartResponse.CartItem) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<GetCartResponse.CartItem>) {
        this.cartItems = items
        notifyDataSetChanged()
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
            val formattedPrice = if (totalAmount % 1.0 == 0.0) {
                totalAmount.toInt().toString()
            } else {
                String.format("%.2f", totalAmount)
            }
            binding.tvPrice.text = "£$formattedPrice"
            binding.tvQuantity.text = item.quantity.toString()

            // Bind Variants separately
            binding.tvVariant1.visibility = View.GONE
            binding.tvVariant2.visibility = View.GONE

            when (item.productType) {
                "book" -> {
                    binding.tvVariant1.visibility = View.VISIBLE
                    binding.tvVariant1.text = "Author : ${item.productDetails?.author ?: ""}"
                }
                "device" -> {
                    val color = item.variantDetails?.color
                    val size = item.variantDetails?.size
                    if (!color.isNullOrEmpty()) {
                        binding.tvVariant1.visibility = View.VISIBLE
                        binding.tvVariant1.text = "Color : $color"
                    }
                    if (!size.isNullOrEmpty()) {
                        binding.tvVariant2.visibility = View.VISIBLE
                        binding.tvVariant2.text = "Size : $size"
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
            }

            // Load Image
            val imageUrl = item.variantDetails?.image
            Glide.with(binding.ivProductImage.context)
                .load(imageUrl)
                .into(binding.ivProductImage)

            // Edit button visibility (only for certain types if needed, though image shows for ring and review)
            binding.ivEdit.visibility = if (item.productType != "book") View.VISIBLE else View.GONE

            binding.ivEdit.setOnClickListener {
                onEditClicked?.invoke(item)
            }

            // Listeners
            binding.ivPlus.setOnClickListener {
                onQuantityChanged?.invoke(item, (item.quantity ?: 0) + 1)
            }
            binding.ivMinus.setOnClickListener {
                if ((item.quantity ?: 0) > 1) {
                    onQuantityChanged?.invoke(item, (item.quantity ?: 0) - 1)
                }
            }
            binding.ivDelete.setOnClickListener {
                onDeleteClicked?.invoke(item)
            }
        }
    }
}
