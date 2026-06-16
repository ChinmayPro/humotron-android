package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemTicketMessageAgentBinding
import com.humotron.app.databinding.ItemTicketMessageUserBinding
import com.humotron.app.R
import com.humotron.app.domain.modal.response.TicketAttachment
import com.humotron.app.domain.modal.response.TicketMessage

class TicketMessageAdapter(
    private val onAttachmentClick: (TicketAttachment) -> Unit
) : ListAdapter<TicketMessage, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AGENT = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderType.equals("user", ignoreCase = true)) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_AGENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(ItemTicketMessageUserBinding.inflate(inflater, parent, false))
        } else {
            AgentViewHolder(ItemTicketMessageAgentBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is UserViewHolder) {
            holder.bind(message)
        } else if (holder is AgentViewHolder) {
            holder.bind(message)
        }
    }

    private fun formatMessageTime(dateStr: String?, context: android.content.Context): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val instant = java.time.Instant.parse(dateStr)
            val diff = System.currentTimeMillis() - instant.toEpochMilli()
            val hours = diff / (1000 * 60 * 60)
            if (hours < 48) {
                if (hours < 1) {
                    val minutes = diff / (1000 * 60)
                    if (minutes < 1) context.getString(R.string.support_chat_just_now) else context.getString(R.string.support_chat_minutes_ago, minutes.toInt())
                } else {
                    val days = hours / 24
                    val remainingHours = hours % 24
                    if (days > 0) {
                        context.getString(R.string.support_chat_days_hours, days.toInt(), remainingHours.toInt())
                    } else {
                        context.getString(R.string.support_chat_hours_ago, hours.toInt())
                    }
                }
            } else {
                com.humotron.app.util.utcOffsetToLocalTime(dateStr, "MMM dd, hh:mm a")
            }
        } catch (e: Exception) {
            com.humotron.app.util.utcOffsetToLocalTime(dateStr, "MMM dd, hh:mm a")
        }
    }

    inner class UserViewHolder(private val binding: ItemTicketMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TicketMessage) {
            binding.tvSender.text = binding.root.context.getString(R.string.support_chat_sender_you)
            binding.tvDate.text = formatMessageTime(item.createdAt, binding.root.context)
            binding.tvBody.text = item.body ?: ""

            if (!item.attachments.isNullOrEmpty()) {
                binding.rvAttachments.visibility = View.VISIBLE
                binding.rvAttachments.layoutManager = LinearLayoutManager(binding.root.context)
                val attachmentAdapter = MessageAttachmentAdapter(onAttachmentClick)
                binding.rvAttachments.adapter = attachmentAdapter
                attachmentAdapter.submitList(item.attachments)
            } else {
                binding.rvAttachments.visibility = View.GONE
            }
        }
    }

    inner class AgentViewHolder(private val binding: ItemTicketMessageAgentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TicketMessage) {
            val ctx = binding.root.context
            binding.tvSender.text = item.senderName?.ifEmpty { ctx.getString(R.string.support_chat_sender_agent) } ?: ctx.getString(R.string.support_chat_sender_agent)
            binding.tvDate.text = formatMessageTime(item.createdAt, ctx)
            binding.tvBody.text = item.body ?: ""

            if (!item.attachments.isNullOrEmpty()) {
                binding.rvAttachments.visibility = View.VISIBLE
                binding.rvAttachments.layoutManager = LinearLayoutManager(binding.root.context)
                val attachmentAdapter = MessageAttachmentAdapter(onAttachmentClick)
                binding.rvAttachments.adapter = attachmentAdapter
                attachmentAdapter.submitList(item.attachments)
            } else {
                binding.rvAttachments.visibility = View.GONE
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<TicketMessage>() {
        override fun areItemsTheSame(oldItem: TicketMessage, newItem: TicketMessage): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TicketMessage, newItem: TicketMessage): Boolean = oldItem == newItem
    }
}
