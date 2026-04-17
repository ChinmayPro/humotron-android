package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.databinding.ItemOptimizeHeaderBinding
import com.humotron.app.databinding.ItemOptimizeSupplementBinding
import com.humotron.app.domain.modal.response.OptimizedRecommendationItem
import com.humotron.app.domain.modal.response.RecipeItem
import com.humotron.app.domain.modal.response.SupplementItem

sealed class OptimizeUIItem {
    data class Header(val title: String) : OptimizeUIItem()
    data class Supplement(val data: SupplementItem) : OptimizeUIItem()
    data class Recipe(val data: RecipeItem) : OptimizeUIItem()
    data class Recommendation(val data: OptimizedRecommendationItem) : OptimizeUIItem()
}

class OptimizeAdapter(
    private var items: List<OptimizeUIItem>,
    private val onExploreClick: (id: String, type: String) -> Unit,
    private val onChatPromptClick: (promptId: String, title: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SUPPLEMENT = 1
        private const val TYPE_RECIPE = 2
        private const val TYPE_RECOMMENDATION = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is OptimizeUIItem.Header -> TYPE_HEADER
            is OptimizeUIItem.Supplement -> TYPE_SUPPLEMENT
            is OptimizeUIItem.Recipe -> TYPE_RECIPE
            is OptimizeUIItem.Recommendation -> TYPE_RECOMMENDATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(ItemOptimizeHeaderBinding.inflate(inflater, parent, false))
            TYPE_SUPPLEMENT -> SupplementViewHolder(ItemOptimizeSupplementBinding.inflate(inflater, parent, false))
            TYPE_RECIPE -> RecipeViewHolder(ItemOptimizeSupplementBinding.inflate(inflater, parent, false))
            TYPE_RECOMMENDATION -> RecommendationViewHolder(ItemOptimizeSupplementBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is OptimizeUIItem.Header -> (holder as HeaderViewHolder).bind(item)
            is OptimizeUIItem.Supplement -> (holder as SupplementViewHolder).bind(item)
            is OptimizeUIItem.Recipe -> (holder as RecipeViewHolder).bind(item)
            is OptimizeUIItem.Recommendation -> (holder as RecommendationViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<OptimizeUIItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(private val binding: ItemOptimizeHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OptimizeUIItem.Header) {
            binding.tvHeader.text = item.title
        }
    }

    inner class SupplementViewHolder(private val binding: ItemOptimizeSupplementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OptimizeUIItem.Supplement) {
            val data = item.data
            binding.tvMetricInfo.text = binding.root.context.getString(
                com.humotron.app.R.string.metric_info_template,
                data.metricName,
                data.metricReading
            )
            binding.tvMetricDelta.text = data.metricDelta
            binding.tvProductName.text = data.productName
            binding.tvProductDesc.text = data.productDesc
            binding.tvPrompt.text = data.productChatPrompt?.title

            // Handle Prompt Visibility
            binding.clPrompt.visibility = if (data.productChatPrompt?.title.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Handle Delta Color and Arrow
            val delta = data.metricDelta?.toDoubleOrNull() ?: 0.0
            if (delta < 0) {
                binding.ivDeltaIndicator.rotation = 90f // Down
                binding.vStatusDot.setBackgroundResource(com.humotron.app.R.drawable.bg_red_indicator)
            } else {
                binding.ivDeltaIndicator.rotation = -90f // Up
                binding.vStatusDot.setBackgroundResource(com.humotron.app.R.drawable.bg_round_green)
            }

            Glide.with(binding.ivProduct).load(data.productImage).into(binding.ivProduct)

            binding.btnExplore.setOnClickListener { 
                data.productId?.let { id: String -> onExploreClick(id, "supplement") }
            }
            binding.clPrompt.setOnClickListener {
                data.productChatPrompt?.let { prompt -> onChatPromptClick(prompt.id ?: "", prompt.title ?: "") }
            }
        }
    }

    inner class RecipeViewHolder(private val binding: ItemOptimizeSupplementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OptimizeUIItem.Recipe) {
            val data = item.data
            binding.tvMetricInfo.text = binding.root.context.getString(
                com.humotron.app.R.string.metric_info_template,
                data.metricName,
                data.metricReading
            )
            binding.tvMetricDelta.text = data.metricDelta
            binding.tvProductName.text = data.recipeBundleName
            binding.tvProductDesc.text = data.recipeBundleDesc
            binding.tvPrompt.text = data.chatPrompt?.title

            // Handle Prompt Visibility
            binding.clPrompt.visibility = if (data.chatPrompt?.title.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Handle Delta Color and Arrow
            val delta = data.metricDelta?.toDoubleOrNull() ?: 0.0
            if (delta < 0) {
                binding.ivDeltaIndicator.rotation = 90f // Down
                binding.vStatusDot.setBackgroundResource(com.humotron.app.R.drawable.bg_red_indicator)
            } else {
                binding.ivDeltaIndicator.rotation = -90f // Up
                binding.vStatusDot.setBackgroundResource(com.humotron.app.R.drawable.bg_round_green)
            }

            Glide.with(binding.ivProduct).load(data.recipeBundleImage).into(binding.ivProduct)

            binding.btnExplore.setOnClickListener {
                data.recipeBundleId?.let { id: String -> onExploreClick(id, "recipe") }
            }
            binding.clPrompt.setOnClickListener {
                data.chatPrompt?.let { prompt -> onChatPromptClick(prompt.id ?: "", prompt.title ?: "") }
            }
        }
    }

    inner class RecommendationViewHolder(private val binding: ItemOptimizeSupplementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OptimizeUIItem.Recommendation) {
            val data = item.data
            binding.clMetricPill.visibility = View.GONE
            binding.ivProduct.visibility = View.GONE
            binding.tvProductName.text = data.title
            binding.tvProductDesc.text = data.subtitle
            binding.clPrompt.visibility = View.GONE
            binding.ivCart.visibility = View.GONE
            binding.btnExplore.text = "Learn More  →"

            // Adjust constraints for Recommendation
            val params = binding.tvProductName.layoutParams as ViewGroup.MarginLayoutParams
            params.marginStart = 20 // Move back to 20dp margin since image is gone
            binding.tvProductName.layoutParams = params

            binding.btnExplore.setOnClickListener {
                data.id?.let { id: String -> onExploreClick(id, data.type ?: "recommendation") }
            }
        }
    }
}
