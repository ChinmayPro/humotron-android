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

        val style = getBoosterStyle(booster.boosterId, booster.displayName)
        val isInPro = when (booster.boosterId?.lowercase()) {
            "ai", "aichat", "cm", "ss" -> true
            else -> {
                val name = booster.displayName.lowercase()
                name.contains("insight") || name.contains("chat") || name.contains("computed") || name.contains("suggestion")
            }
        }

        holder.binding.apply {
            tvToolName.text = booster.displayName
            tvToolDescription.text = booster.displayDescription
            tvToolPrice.text = priceText

            // Set dynamic booster icon and accent color mix styling from resources
            ivBoosterIcon.setImageResource(style.iconRes)
            val accentColor = context.getColor(style.accentColorRes)
            ivBoosterIcon.imageTintList = ColorStateList.valueOf(accentColor)

            // Programmatically calculate a 14% transparent version of the accent color
            val alphaColor = Color.argb(36, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
            cvBoosterIcon.setCardBackgroundColor(alphaColor)

            // Update badge text and background using resources
            if (isInPro) {
                layoutBoosterBadge.text = context.getString(R.string.in_pro_badge)
                layoutBoosterBadge.setTextColor(context.getColor(R.color.booster_accent_lime))
                layoutBoosterBadge.setBackgroundResource(R.drawable.bg_booster_tag_pro)
            } else {
                layoutBoosterBadge.text = context.getString(R.string.add_on_badge)
                layoutBoosterBadge.setTextColor(context.getColor(R.color.booster_accent_watch))
                layoutBoosterBadge.setBackgroundResource(R.drawable.bg_booster_tag)
            }

            // Always show the period label as /monthly to match the premium booster design mockup
            tvToolPricePeriod.visibility = android.view.View.VISIBLE

            // Update card background and CTA button based on ownership state
            if (isUnlocked) {
                llBoosterRoot.setBackgroundResource(R.drawable.bg_booster_unlocked)
                btnBuy.text = context.getString(R.string.unlocked_label)
                btnBuy.isEnabled = false
                btnBuy.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.unlocked_btn_bg))
                btnBuy.setTextColor(context.getColor(R.color.white50))
                btnBuy.setOnClickListener(null)
            } else {
                llBoosterRoot.setBackgroundResource(R.drawable.bg_booster)
                btnBuy.text = context.getString(R.string.view_label)
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

    private data class BoosterStyle(
        val iconRes: Int,
        val accentColorRes: Int
    )

    private fun getBoosterStyle(boosterId: String?, displayName: String): BoosterStyle {
        val id = boosterId?.lowercase() ?: ""
        val name = displayName.lowercase()
        return when {
            id == "ai" || name.contains("insight") -> {
                BoosterStyle(R.drawable.ic_spark, R.color.booster_accent_lime)
            }
            id == "aichat" || name.contains("chat") -> {
                BoosterStyle(R.drawable.ic_bot, R.color.booster_accent_cool)
            }
            id == "cm" || name.contains("computed") -> {
                BoosterStyle(R.drawable.ic_shop_optimize, R.color.booster_accent_cool)
            }
            id == "ss" || name.contains("suggestion") -> {
                BoosterStyle(R.drawable.ic_target, R.color.booster_accent_good)
            }
            id == "ln" || name.contains("lifestyle") || name.contains("nudge") -> {
                BoosterStyle(R.drawable.ic_shop_scan, R.color.booster_accent_watch)
            }
            id == "md" || name.contains("multiple") || name.contains("device") -> {
                BoosterStyle(R.drawable.ic_shop_device, R.color.booster_accent_cool)
            }
            id == "dt" || name.contains("dependent") || name.contains("track") -> {
                BoosterStyle(R.drawable.ic_shop_tools, R.color.booster_accent_lime)
            }
            id == "rg" || name.contains("recipe") -> {
                BoosterStyle(R.drawable.ic_bowl, R.color.booster_accent_good)
            }
            else -> {
                BoosterStyle(R.drawable.ic_spark, R.color.booster_accent_cool)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class ToolViewHolder(val binding: ItemShopToolBinding) : RecyclerView.ViewHolder(binding.root)
}
