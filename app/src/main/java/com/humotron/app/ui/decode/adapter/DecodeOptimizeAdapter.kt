package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemOptimizeRecommendationBinding
import com.humotron.app.domain.modal.response.RecommendationMetricItem

class DecodeOptimizeAdapter(
    private var items: List<RecommendationMetricItem>,
    private val onExploreClick: (productId: String, productType: String, metricName: String?, metricDelta: String?, metricStatus: String?) -> Unit
) : RecyclerView.Adapter<DecodeOptimizeAdapter.RecommendationViewHolder>() {

    private val expandedIds = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemOptimizeRecommendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<RecommendationMetricItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class RecommendationViewHolder(private val binding: ItemOptimizeRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecommendationMetricItem, position: Int) {
            val context = binding.root.context

            // Category/Eyebrow label formatting
            val typeStr = when (item.type) {
                "supplement" -> "SUPPLEMENT"
                "recipeBundle" -> "RECIPES"
                "lifestyle" -> "LIFESTYLE"
                "device" -> "DEVICE"
                "service" -> "SERVICE"
                else -> item.type?.uppercase() ?: "RECOMMENDATION"
            }
            val subCat = when {
                item.type == "service" && item.category?.any { it.contains("app", true) } == true -> "APP"
                else -> item.subCategory?.firstOrNull()?.uppercase()
            }
            val categoryLabel = if (typeStr == "SUPPLEMENT" || typeStr == "RECIPES" || subCat == null) {
                typeStr
            } else {
                "$typeStr · $subCat"
            }
            binding.tvCategory.text = categoryLabel

            // Metric Pill visibility and binding
            if (item.metricName.isNullOrEmpty()) {
                binding.clMetricPill.visibility = View.GONE
            } else {
                binding.clMetricPill.visibility = View.VISIBLE
                
                val rawName = item.recommendationMetricName ?: item.metricName ?: ""
                val formattedName = when {
                    rawName.contains("Resting Heart Rate", ignoreCase = true) -> "RHR"
                    rawName.contains("Heart Rate Variability", ignoreCase = true) -> "HRV"
                    rawName.contains("Deep Sleep", ignoreCase = true) -> "Deep Sleep"
                    rawName.contains("REM Sleep", ignoreCase = true) -> "REM Sleep"
                    rawName.contains("Sleep Duration", ignoreCase = true) -> "Sleep"
                    rawName.contains("HRV", ignoreCase = true) -> "HRV"
                    rawName.contains("RHR", ignoreCase = true) -> "RHR"
                    else -> rawName
                }
                binding.tvMetricName.text = formattedName

                val deltaVal = item.metricDelta?.replace("%", "")?.toDoubleOrNull() ?: 0.0
                val sign = if (deltaVal > 0) "↑" else "↓"
                val absVal = Math.abs(deltaVal)
                binding.tvMetricDelta.text = "$sign ${String.format("%.0f", absVal)}%"

                // Set Dot Status and Delta Text color dynamically
                val absDelta = Math.abs(deltaVal)
                val isDown = deltaVal < 0
                val isPositiveMetric = when {
                    item.metricName.contains("HRV", ignoreCase = true) -> true
                    item.metricName.contains("Variability", ignoreCase = true) -> true
                    item.metricName.contains("Sleep", ignoreCase = true) -> true
                    item.metricName.contains("Recovery", ignoreCase = true) -> true
                    else -> false
                }

                val status = if (isPositiveMetric) {
                    if (isDown) {
                        if (absDelta >= 20.0) "attention" else "watch"
                    } else {
                        "optimal"
                    }
                } else {
                    if (isDown) {
                        "optimal"
                    } else {
                        if (absDelta >= 10.0) "attention" else "watch"
                    }
                }

                when (status) {
                    "attention" -> {
                        binding.vStatusDot.setBackgroundResource(R.drawable.bg_red_indicator)
                        binding.tvMetricDelta.setTextColor(android.graphics.Color.parseColor("#EE4D3D"))
                    }
                    "watch" -> {
                        binding.vStatusDot.setBackgroundResource(R.drawable.bg_amber_indicator)
                        binding.tvMetricDelta.setTextColor(android.graphics.Color.parseColor("#E7A93C"))
                    }
                    else -> { // optimal
                        binding.vStatusDot.setBackgroundResource(R.drawable.bg_round_green)
                        binding.tvMetricDelta.setTextColor(android.graphics.Color.parseColor("#C4F23E"))
                    }
                }
            }

            // Title and Description
            binding.tvTitle.text = item.title
            binding.tvShort.text = item.short

            // Confidence Badge
            if (item.priorityRank == 1) {
                binding.tvBadge.text = "High confidence"
                binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_high)
                binding.tvBadge.setTextColor(context.getColor(R.color.colorBgBtn))
            } else {
                binding.tvBadge.text = "Worth exploring"
                binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_explore)
                binding.tvBadge.setTextColor(context.getColor(R.color.w200))
            }

            // Expanded logic
            val isExpanded = expandedIds.contains(item.id)
            binding.clExpandedSection.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivChevron.rotation = if (isExpanded) 90f else 0f

            if (isExpanded) {
                binding.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#D1070C0D"))
                binding.cardContainer.strokeColor = android.graphics.Color.parseColor("#38C4F23E")
            } else {
                binding.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#8C080F10"))
                binding.cardContainer.strokeColor = android.graphics.Color.parseColor("#12FFFFFF")
            }

            // Rationale binding
            binding.tvWhyThisContent.text = item.whyThis
            binding.tvWhyYouContent.text = item.whyYou
            binding.tvWhyNowContent.text = item.whyNow

            // Click listener to toggle expand
            binding.cardContainer.setOnClickListener {
                item.id?.let { id ->
                    if (isExpanded) {
                        expandedIds.remove(id)
                    } else {
                        expandedIds.add(id)
                    }
                    notifyItemChanged(position)
                }
            }

            // Details click
            binding.btnViewDetails.setOnClickListener {
                item.id?.let { id ->
                    var formattedName: String? = null
                    var deltaText: String? = null
                    var status = "optimal"
                    
                    if (!item.metricName.isNullOrEmpty()) {
                        val rawName = item.recommendationMetricName ?: item.metricName ?: ""
                        formattedName = when {
                            rawName.contains("Resting Heart Rate", ignoreCase = true) -> "RHR"
                            rawName.contains("Heart Rate Variability", ignoreCase = true) -> "HRV"
                            rawName.contains("Deep Sleep", ignoreCase = true) -> "Deep Sleep"
                            rawName.contains("REM Sleep", ignoreCase = true) -> "REM Sleep"
                            rawName.contains("Sleep Duration", ignoreCase = true) -> "Sleep"
                            rawName.contains("HRV", ignoreCase = true) -> "HRV"
                            rawName.contains("RHR", ignoreCase = true) -> "RHR"
                            else -> rawName
                        }

                        val deltaVal = item.metricDelta?.replace("%", "")?.toDoubleOrNull() ?: 0.0
                        val sign = if (deltaVal > 0) "↑" else "↓"
                        val absVal = Math.abs(deltaVal)
                        deltaText = "$sign ${String.format("%.0f", absVal)}%"

                        val absDelta = Math.abs(deltaVal)
                        val isDown = deltaVal < 0
                        val isPositiveMetric = when {
                            item.metricName.contains("HRV", ignoreCase = true) -> true
                            item.metricName.contains("Variability", ignoreCase = true) -> true
                            item.metricName.contains("Sleep", ignoreCase = true) -> true
                            item.metricName.contains("Recovery", ignoreCase = true) -> true
                            else -> false
                        }
                        status = if (isPositiveMetric) {
                            if (isDown) {
                                if (absDelta >= 20.0) "attention" else "watch"
                            } else {
                                "optimal"
                            }
                        } else {
                            if (isDown) {
                                "optimal"
                            } else {
                                if (absDelta >= 10.0) "attention" else "watch"
                            }
                        }
                    }
                    onExploreClick(id, item.type ?: "", formattedName, deltaText, status)
                }
            }
        }
    }
}
