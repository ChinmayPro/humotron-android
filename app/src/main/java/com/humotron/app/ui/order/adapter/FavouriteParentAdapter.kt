package com.humotron.app.ui.order.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.databinding.*
import com.humotron.app.domain.modal.response.BookPreferenceResponse
import com.humotron.app.domain.modal.response.GetShopDevicesResponse
import com.humotron.app.domain.modal.response.SupplementItem
import com.humotron.app.ui.shop.adapter.ShopBookAdapter
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer

    sealed class FavouriteUIItem {
        data class Header(val title: String) : FavouriteUIItem()
        data class Book(val data: com.humotron.app.domain.modal.response.BookRecommendation) : FavouriteUIItem()
        data class Device(val data: GetShopDevicesResponse.Device) : FavouriteUIItem()
        data class Product(val data: SupplementItem) : FavouriteUIItem()
    }

    class FavouriteParentAdapter(
        private val action: ShopBookAdapter.OnBookItemActions,
        private val onDeviceClick: (GetShopDevicesResponse.Device) -> Unit,
        private val onProductClick: (SupplementItem) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items = listOf<FavouriteUIItem>()

        companion object {
            private const val TYPE_HEADER = 0
            private const val TYPE_BOOK = 1
            private const val TYPE_DEVICE = 2
            private const val TYPE_PRODUCT = 3
        }

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is FavouriteUIItem.Header -> TYPE_HEADER
                is FavouriteUIItem.Book -> TYPE_BOOK
                is FavouriteUIItem.Device -> TYPE_DEVICE
                is FavouriteUIItem.Product -> TYPE_PRODUCT
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_HEADER -> HeaderViewHolder(ItemOptimizeHeaderBinding.inflate(inflater, parent, false))
                TYPE_BOOK -> BookViewHolder(ItemFavouriteBookBinding.inflate(inflater, parent, false), action)
                TYPE_DEVICE -> DeviceViewHolder(ItemShopDeviceBinding.inflate(inflater, parent, false), onDeviceClick)
                TYPE_PRODUCT -> ProductViewHolder(ItemOptimizeSupplementBinding.inflate(inflater, parent, false), onProductClick)
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is FavouriteUIItem.Header -> (holder as HeaderViewHolder).bind(item)
                is FavouriteUIItem.Book -> (holder as BookViewHolder).bind(item)
                is FavouriteUIItem.Device -> (holder as DeviceViewHolder).bind(item)
                is FavouriteUIItem.Product -> (holder as ProductViewHolder).bind(item)
            }
        }

        override fun getItemCount(): Int = items.size

        fun setItems(newItems: List<FavouriteUIItem>) {
            this.items = newItems
            notifyDataSetChanged()
        }

        class HeaderViewHolder(private val binding: ItemOptimizeHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: FavouriteUIItem.Header) {
                binding.tvHeader.text = item.title
            }
        }

        class BookViewHolder(
            private val binding: ItemFavouriteBookBinding,
            private val action: ShopBookAdapter.OnBookItemActions
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: FavouriteUIItem.Book) {
                val data = item.data
                binding.tvTitle.text = data.bookTitle
                binding.tvAuthor.text = data.author1
                binding.tvPrice.text = "£${data.price ?: ""}"
                
                val context = binding.root.context
                if (data.isCart == true) {
                    binding.tvAddToCartText.text = context.getString(R.string.remove_cart)
                    binding.btnAddToCart.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E74C3C"))
                    binding.tvAddToCartText.setTextColor(android.graphics.Color.WHITE)
                    binding.ivAddToCartIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                } else {
                    binding.tvAddToCartText.text = context.getString(R.string.add_to_cart)
                    binding.btnAddToCart.backgroundTintList = null
                    binding.btnAddToCart.background = context.getDrawable(R.drawable.bg_cart_lime)
                    binding.tvAddToCartText.setTextColor(android.graphics.Color.parseColor("#C4F23E"))
                    binding.ivAddToCartIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C4F23E"))
                }

                binding.btnAddToCart.setOnClickListener {
                    data.id?.let { bookId -> action.addToCart(bookId) }
                }
                
                binding.root.setOnClickListener {
                    data.id?.let { bookId -> action.openSummary(bookId) }
                }
            }
        }

    class DeviceViewHolder(
        private val binding: ItemShopDeviceBinding,
        private val onDeviceClick: (GetShopDevicesResponse.Device) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FavouriteUIItem.Device) {
            val data = item.data
            binding.tvDeviceName.text = data.deviceName
            binding.tvDeviceCategory.text = data.deviceFacingName
            binding.tvPrice.text = "£${data.deviceModel?.deviceModelPrice ?: ""}"
            
            val imageUrl = data.deviceImage?.firstOrNull()
            Glide.with(binding.ivDeviceImage).load(imageUrl).into(binding.ivDeviceImage)

            binding.root.setOnClickListener { onDeviceClick(data) }
            binding.btnExplore.setOnClickListener { onDeviceClick(data) }
            
            // Handle Metrics display if present
            val metricsList = data.metrics?.mapNotNull { it.metricName } ?: emptyList()
            binding.tvMetric1.visibility = if (metricsList.isNotEmpty()) View.VISIBLE else View.GONE
            binding.tvMetric2.visibility = if (metricsList.size > 1) View.VISIBLE else View.GONE
            binding.tvMetric3.visibility = if (metricsList.size > 2) View.VISIBLE else View.GONE
            
            if (metricsList.isNotEmpty()) binding.tvMetric1.text = metricsList[0]
            if (metricsList.size > 1) binding.tvMetric2.text = metricsList[1]
            if (metricsList.size > 2) binding.tvMetric3.text = metricsList[2]

            // Apply horizontal margins to the root view
            val params = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            params.marginStart = binding.root.context.resources.getDimensionPixelSize(R.dimen._15dp)
            params.marginEnd = binding.root.context.resources.getDimensionPixelSize(R.dimen._15dp)
            binding.root.layoutParams = params
        }
    }

    class ProductViewHolder(
        private val binding: ItemOptimizeSupplementBinding,
        private val onProductClick: (SupplementItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Default visibility
            binding.clMetricPill.visibility = View.GONE
            binding.clPrompt.visibility = View.GONE
            binding.cardShadow.visibility = View.GONE
        }

        fun bind(item: FavouriteUIItem.Product) {
            val data = item.data
            binding.tvProductName.text = data.productName
            binding.tvProductDesc.text = data.productDesc
            Glide.with(binding.ivProduct).load(data.productImage).into(binding.ivProduct)

            // Show Metric Pill if reading is available
            if (!data.metricReading.isNullOrEmpty()) {
                binding.clMetricPill.visibility = View.VISIBLE
                binding.tvMetricInfo.text = binding.root.context.getString(
                    R.string.metric_info_template,
                    data.metricName,
                    data.metricReading
                )
                binding.tvMetricDelta.text = data.metricDelta
            } else {
                binding.clMetricPill.visibility = View.GONE
            }

            // Show AI prompt if available
            if (!data.productChatPrompt?.title.isNullOrEmpty()) {
                binding.clPrompt.visibility = View.VISIBLE
                binding.tvPrompt.text = data.productChatPrompt?.title
            } else {
                binding.clPrompt.visibility = View.GONE
            }

            // Apply horizontal margins to the card and prompt bar
            val cardParams = binding.cardContainer.layoutParams as ViewGroup.MarginLayoutParams
            cardParams.marginStart = binding.root.context.resources.getDimensionPixelSize(R.dimen._15dp)
            cardParams.marginEnd = binding.root.context.resources.getDimensionPixelSize(R.dimen._15dp)
            binding.cardContainer.layoutParams = cardParams

            val promptParams = binding.clPrompt.layoutParams as ViewGroup.MarginLayoutParams
            promptParams.marginStart = binding.root.context.resources.getDimensionPixelSize(R.dimen._15dp)
            promptParams.marginEnd = binding.root.context.resources.getDimensionPixelSize(R.dimen._15dp)
            binding.clPrompt.layoutParams = promptParams

            binding.root.setOnClickListener { onProductClick(data) }
            binding.btnExplore.setOnClickListener { onProductClick(data) }
        }
    }
}
