package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemDecodePatternCardBinding
import com.humotron.app.ui.decode.DecodeAnimationUtils

data class PatternItem(
    val id: String,
    val title: String,
    val description: String,
    val isLocked: Boolean,
    val progress: String? = null,
    val badgeText: String,
    val actionText: String
)

class DecodePatternsAdapter(
    private var items: List<PatternItem> = emptyList(),
    private val onItemClick: (PatternItem) -> Unit
) : RecyclerView.Adapter<DecodePatternsAdapter.ViewHolder>() {

    fun updateItems(newItems: List<PatternItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDecodePatternCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemDecodePatternCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PatternItem) {
            val context = itemView.context
            
            if (item.isLocked) {
                binding.tvPatternTitle.text = "🔒 ${item.title}"
                binding.tvPatternTitle.setTextColor(ContextCompat.getColor(context, R.color.insights_slate_grey))
                
                binding.viewStripe.setBackgroundColor(ContextCompat.getColor(context, R.color.white10))
                
                binding.tvPatternBadge.text = item.badgeText
                binding.tvPatternBadge.setBackgroundResource(R.drawable.bg_badge_gen)
                binding.tvPatternBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_amber))
                
                binding.tvPatternAction.text = item.actionText
                binding.tvPatternAction.setTextColor(ContextCompat.getColor(context, R.color.insights_slate_grey))
                
                binding.root.alpha = 0.75f
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
                binding.root.isFocusable = false
                
                binding.layoutContent.setOnClickListener(null)
                binding.layoutContent.isClickable = false
                binding.layoutContent.isFocusable = false
                
                binding.tvPatternAction.setOnClickListener(null)
                binding.tvPatternAction.isClickable = false
                binding.tvPatternAction.isFocusable = false
            } else {
                binding.tvPatternTitle.text = item.title
                binding.tvPatternTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
                
                binding.viewStripe.setBackgroundColor(ContextCompat.getColor(context, R.color.insights_green))
                
                binding.tvPatternBadge.text = item.badgeText
                binding.tvPatternBadge.setBackgroundResource(R.drawable.bg_badge_ready)
                binding.tvPatternBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_green))
                
                binding.tvPatternAction.text = item.actionText
                binding.tvPatternAction.setTextColor(ContextCompat.getColor(context, R.color.insights_green))
                
                binding.root.alpha = 1.0f
                binding.root.isClickable = true
                binding.root.isFocusable = true
                binding.root.setOnClickListener { onItemClick(item) }
                
                binding.layoutContent.isClickable = true
                binding.layoutContent.isFocusable = true
                binding.layoutContent.setOnClickListener { onItemClick(item) }
                
                binding.tvPatternAction.isClickable = true
                binding.tvPatternAction.isFocusable = true
                binding.tvPatternAction.setOnClickListener { onItemClick(item) }
                
                DecodeAnimationUtils.addPressEffect(binding.root)
            }
            
            binding.tvPatternDesc.text = item.description
        }
    }
}
