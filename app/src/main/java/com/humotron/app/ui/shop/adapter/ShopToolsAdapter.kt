package com.humotron.app.ui.shop.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.humotron.app.R
import com.humotron.app.databinding.ItemShopToolBinding
import com.humotron.app.domain.modal.response.BoosterResponse

class ShopToolsAdapter(
    private val onUnlockClick: (BoosterResponse.Booster) -> Unit,
    private val onItemClick: (BoosterResponse.Booster) -> Unit
) : RecyclerView.Adapter<ShopToolsAdapter.ToolViewHolder>() {

    private var items = listOf<BoosterResponse.Booster>()
    private var activePurchases = listOf<Purchase>()
    private var playStoreProducts = listOf<ProductDetails>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<BoosterResponse.Booster>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    fun setActivePurchases(purchases: List<Purchase>) {
        this.activePurchases = purchases
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setPlayStoreProducts(products: List<ProductDetails>) {
        this.playStoreProducts = products
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemShopToolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val booster = items[position]
        val context = holder.itemView.context

        // Check if user has already unlocked this tool (via backend isSubscribed or local Play Store active purchases)
        val isUnlocked = booster.isSubscribed == true || activePurchases.any { purchase ->
            purchase.products.contains(booster.playStoreProductId)
        }

        // Try to match with Google Play ProductDetails to extract localized price formatting
        val matchedProduct = playStoreProducts.find { it.productId == booster.playStoreProductId }
        val subscriptionOffer = matchedProduct?.subscriptionOfferDetails?.firstOrNull()
        val subscriptionPrice = subscriptionOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        val priceText = subscriptionPrice ?: matchedProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: booster.displayPriceFallback

        holder.binding.apply {
            tvToolName.text = booster.displayName
            tvToolDescription.text = booster.displayDescription
            tvToolPrice.text = priceText

            // Always show the period label as /monthly to match the premium booster design mockup
            tvToolPricePeriod.visibility = android.view.View.VISIBLE

            // Update CTA button based on ownership state
            if (isUnlocked) {
                btnBuy.text = context.getString(R.string.unlocked_label)
                btnBuy.isEnabled = false
                btnBuy.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#262626")) // Sleek Charcoal Grey
                btnBuy.setTextColor(Color.parseColor("#80FFFFFF")) // Muted White / Light Grey
                btnBuy.setOnClickListener(null)
            } else {
                btnBuy.text = context.getString(R.string.upgrade_label)
                btnBuy.isEnabled = true
                btnBuy.backgroundTintList = context.getColorStateList(R.color.lime_green)
                btnBuy.setTextColor(Color.BLACK)
                btnBuy.setOnClickListener {
                    onUnlockClick(booster)
                }
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(booster)
        }
    }

    override fun getItemCount(): Int = items.size

    class ToolViewHolder(val binding: ItemShopToolBinding) : RecyclerView.ViewHolder(binding.root)
}
