package com.humotron.app.ui.decode.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemDecodePastInsightBinding
import com.humotron.app.domain.modal.response.TimelineWindow
import com.humotron.app.ui.decode.DecodeAnimationUtils

class DecodeTimelineAdapter(
    private var items: List<TimelineWindow> = emptyList(),
    private val onItemClick: (TimelineWindow) -> Unit
) : RecyclerView.Adapter<DecodeTimelineAdapter.ViewHolder>() {

    fun updateItems(newItems: List<TimelineWindow>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDecodePastInsightBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemDecodePastInsightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimelineWindow) {
            val context = itemView.context
            
            // Bind range & days
            binding.tvDateRange.text = item.label ?: ""
            binding.tvDays.text = context.getString(R.string.insights_days_format, item.patternDays ?: 7)

            val action = item.windowAction ?: "NOT_ENOUGH_DATA"
            when (action) {
                "NOT_ENOUGH_DATA" -> {
                    binding.tvStatusIcon.text = "🔒"
                    binding.tvStatusIcon.setBackgroundResource(R.drawable.bg_decode_status_none)
                    binding.tvStatusIcon.setTextColor(ContextCompat.getColor(context, R.color.insights_slate_grey))
                    
                    binding.tvActionBadge.text = context.getString(R.string.insights_locked).uppercase()
                    binding.tvActionBadge.setBackgroundResource(R.drawable.bg_badge_none)
                    binding.tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_dark_grey_text))
                    
                    binding.root.setOnClickListener(null)
                    binding.root.isClickable = false
                    binding.root.isFocusable = false
                }
                "GENERATE_INSIGHT" -> {
                    binding.tvStatusIcon.text = "✓"
                    binding.tvStatusIcon.setBackgroundResource(R.drawable.bg_decode_status_ready)
                    binding.tvStatusIcon.setTextColor(Color.WHITE)
                    
                    binding.tvActionBadge.text = context.getString(R.string.insights_generate_badge).uppercase()
                    binding.tvActionBadge.setBackgroundResource(R.drawable.bg_badge_gen)
                    binding.tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_amber))
                    
                    binding.root.setOnClickListener { onItemClick(item) }
                    DecodeAnimationUtils.addPressEffect(binding.root)
                }
                "VIEW_INSIGHT" -> {
                    binding.tvStatusIcon.text = "✓"
                    binding.tvStatusIcon.setBackgroundResource(R.drawable.bg_decode_status_ready)
                    binding.tvStatusIcon.setTextColor(Color.WHITE)
                    
                    binding.tvActionBadge.text = context.getString(R.string.insights_view).uppercase()
                    binding.tvActionBadge.setBackgroundResource(R.drawable.bg_badge_ready)
                    binding.tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_green))
                    
                    binding.root.setOnClickListener { onItemClick(item) }
                    DecodeAnimationUtils.addPressEffect(binding.root)
                }
                else -> {
                    binding.tvStatusIcon.text = "🔒"
                    binding.tvStatusIcon.setBackgroundResource(R.drawable.bg_decode_status_none)
                    binding.tvStatusIcon.setTextColor(ContextCompat.getColor(context, R.color.insights_slate_grey))
                    
                    binding.tvActionBadge.text = context.getString(R.string.insights_locked).uppercase()
                    binding.tvActionBadge.setBackgroundResource(R.drawable.bg_badge_none)
                    binding.tvActionBadge.setTextColor(ContextCompat.getColor(context, R.color.insights_dark_grey_text))
                    
                    binding.root.setOnClickListener(null)
                    binding.root.isClickable = false
                    binding.root.isFocusable = false
                }
            }
        }
    }
}
