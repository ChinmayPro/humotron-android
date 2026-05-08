package com.humotron.app.ui.order.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.databinding.ItemOrderItemBinding
import com.humotron.app.domain.modal.response.GetOrderDetailResponse
import com.humotron.app.util.formatCartDate

class OrderItemAdapter : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    private var items: List<GetOrderDetailResponse.OrderItem> = emptyList()
    private var promoCode: String? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<GetOrderDetailResponse.OrderItem>, promoCode: String? = null) {
        this.items = items
        this.promoCode = promoCode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OrderItemViewHolder(private val binding: ItemOrderItemBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: GetOrderDetailResponse.OrderItem) {
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

            // Build variants string including quantity
            val variants = mutableListOf<String>()
            when (item.productType) {
                "blood_home", "blood_lab", "blood_self" -> {
                    val variant = when (item.productType) {
                        "blood_home" -> "At Home"
                        "blood_lab" -> "Lab Visit"
                        "blood_self" -> "Self Collection"
                        else -> ""
                    }
                    if (variant.isNotEmpty()) variants.add(variant)
                }
                "expert_review" -> {
                    variants.add("Expert Consultation")
                }
                "book" -> {
                    if (!item.productDetails?.author.isNullOrEmpty()) {
                        variants.add("Author : ${item.productDetails?.author}")
                    }
                }
                "device" -> {
                    item.variantDetails?.color?.let { if (it.isNotEmpty()) variants.add(it) }
                    item.variantDetails?.size?.let { if (it.isNotEmpty()) variants.add("Size : $it") }
                }
            }
            
            variants.add("Qty: ${item.quantity ?: 0}")
            binding.tvVariants.text = variants.joinToString("  |  ")

            // Load Image
            if (item.productType == "book") {
                binding.ivProductImage.setImageResource(R.drawable.ic_book_biohack)
            } else {
                val imageUrl = item.variantDetails?.image
                Glide.with(binding.ivProductImage.context)
                    .load(imageUrl)
                    .into(binding.ivProductImage)
            }
        }
    }
}
