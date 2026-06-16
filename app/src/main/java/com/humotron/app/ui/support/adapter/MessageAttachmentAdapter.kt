package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.databinding.ItemMessageAttachmentBinding
import com.humotron.app.domain.modal.response.TicketAttachment

class MessageAttachmentAdapter(
    private val onAttachmentClick: (TicketAttachment) -> Unit
) : ListAdapter<TicketAttachment, MessageAttachmentAdapter.ViewHolder>(AttachmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageAttachmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMessageAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TicketAttachment) {
            binding.tvFileName.text = item.filename ?: binding.root.context.getString(R.string.support_attachment_label)
            binding.tvFileSize.text = formatSize(item.sizeBytes)

            val isImage = item.mimeType?.startsWith("image/", ignoreCase = true) == true ||
                    item.filename?.lowercase()?.let {
                        it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".webp")
                    } == true

            if (isImage && !item.url.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(item.url)
                    .placeholder(R.drawable.ic_grid)
                    .into(binding.ivThumbnail)
            } else {
                val iconRes = if (item.mimeType?.contains("pdf", ignoreCase = true) == true ||
                    item.filename?.lowercase()?.endsWith(".pdf") == true
                ) {
                    R.drawable.ic_pdf
                } else {
                    R.drawable.ic_grid
                }
                binding.ivThumbnail.setImageResource(iconRes)
            }

            binding.root.setOnClickListener {
                onAttachmentClick(item)
            }
        }

        private fun formatSize(bytes: Long?): String {
            val ctx = binding.root.context
            if (bytes == null || bytes <= 0) return ctx.getString(R.string.support_attachment_size_zero)
            val kb = bytes.toDouble() / 1024
            if (kb < 1024) {
                return String.format(java.util.Locale.ENGLISH, ctx.getString(R.string.support_attachment_size_kb), kb)
            }
            val mb = kb / 1024
            return String.format(java.util.Locale.ENGLISH, ctx.getString(R.string.support_attachment_size_mb), mb)
        }
    }

    class AttachmentDiffCallback : DiffUtil.ItemCallback<TicketAttachment>() {
        override fun areItemsTheSame(oldItem: TicketAttachment, newItem: TicketAttachment): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TicketAttachment, newItem: TicketAttachment): Boolean = oldItem == newItem
    }
}
