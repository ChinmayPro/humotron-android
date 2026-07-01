package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSupportTicketBinding
import com.humotron.app.domain.modal.response.TicketDetail
import com.humotron.app.util.loadImage
import com.humotron.app.util.getTimeAgo
import java.time.Instant

class SupportTicketAdapter(
    private val onTicketClick: (TicketDetail) -> Unit
) : RecyclerView.Adapter<SupportTicketAdapter.ViewHolder>() {

    private val items = mutableListOf<TicketDetail>()

    fun setData(newItems: List<TicketDetail>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportTicketBinding.inflate(
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

    inner class ViewHolder(
        private val binding: ItemSupportTicketBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TicketDetail) {
            binding.tvTitle.text = item.subject ?: ""

            // Status Tag
            val isDraft = item.currentStatus.equals("draft", ignoreCase = true)
            val status = item.status.orEmpty().lowercase()

            val statusText = when {
                isDraft -> binding.root.context.getString(R.string.support_status_draft)
                status == "waiting" || status == "waiting_for_user" -> "Waiting for you"
                status == "resolved" -> "Resolved"
                else -> status.replaceFirstChar { it.uppercase() }
            }
            binding.tvStatus.text = statusText

            val tintColorRes = when {
                isDraft -> R.color.support_help_icon_bg
                status == "waiting" || status == "waiting_for_user" -> R.color.support_status_awaiting_reply_bg
                status == "resolved" -> R.color.support_status_resolved_bg
                else -> R.color.support_open_badge_bg
            }
            binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(binding.root.context, tintColorRes)
            )

            val textColorRes = when {
                isDraft -> R.color.colorBgBtn1
                status == "waiting" || status == "waiting_for_user" -> R.color.support_status_awaiting_reply_text
                status == "resolved" -> R.color.support_status_awaiting_reply_text
                else -> R.color.white
            }
            binding.tvStatus.setTextColor(
                androidx.core.content.ContextCompat.getColor(binding.root.context, textColorRes)
            )

            // Description
            val rawDesc = item.subcategory.orEmpty().ifEmpty { item.description.orEmpty() }
            binding.tvDescription.text = toTitleCase(rawDesc)

            // Icon resolution: load attachment thumbnail if present, else fallback to device category icon
            val firstAttachmentUrl = item.attachments?.firstOrNull()?.url
            if (!firstAttachmentUrl.isNullOrEmpty()) {
                binding.ivIcon.loadImage(firstAttachmentUrl)
            } else {
                val iconRes = when {
                    item.contactReasonCode.equals("smart_ring", ignoreCase = true) -> R.drawable.ic_smart_ring
                    item.contactReasonCode.equals("wrist_band", ignoreCase = true) -> R.drawable.ic_wrist_band
                    item.contactReasonCode.equals("weight_scale", ignoreCase = true) -> R.drawable.ic_weight_scale
                    item.contactReasonCode.equals("smart_cuff", ignoreCase = true) -> R.drawable.ic_smart_cuff
                    item.contactReasonCode.equals("android", ignoreCase = true) -> R.drawable.ic_android
                    else -> R.drawable.ic_grid
                }
                binding.ivIcon.setImageResource(iconRes)
            }

            // Relative time footer
            val relativeTime = try {
                val timestamp = item.updatedAt ?: item.createdAt
                val instant = Instant.parse(timestamp)
                getTimeAgo(instant.toEpochMilli())
            } catch (e: Exception) {
                binding.root.context.getString(R.string.support_chat_just_now)
            }

            val ticketIdText = when {
                !item.ticketNumber.isNullOrEmpty() -> "#${item.ticketNumber}"
                !item.id.isNullOrEmpty() && item.id.length >= 6 -> "#${item.id.takeLast(6)}"
                else -> ""
            }

            if (ticketIdText.isNotEmpty()) {
                binding.tvFooter.text = binding.root.context.getString(R.string.support_detail_updated_footer_format, relativeTime, ticketIdText)
            } else {
                binding.tvFooter.text = binding.root.context.getString(R.string.support_detail_updated_format, relativeTime)
            }

            binding.root.setOnClickListener {
                onTicketClick(item)
            }
        }

        private fun toTitleCase(input: String): String {
            if (input.isBlank()) return ""
            return input.replace('_', ' ').replace('-', ' ').split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
}
