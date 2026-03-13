package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemChatPromptsBinding
import com.humotron.app.domain.modal.response.ChatPrompt

class ChatPromptAdapter :
    ListAdapter<ChatPrompt, ChatPromptAdapter.ChatPromptViewHolder>(ChatPromptDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatPromptViewHolder {
        val binding =
            ItemChatPromptsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatPromptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatPromptViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatPromptViewHolder(private val binding: ItemChatPromptsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatPrompt) {
            binding.chipChatPrompt.text = item.title
        }
    }

    class ChatPromptDiffCallback : DiffUtil.ItemCallback<ChatPrompt>() {
        override fun areItemsTheSame(oldItem: ChatPrompt, newItem: ChatPrompt): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatPrompt, newItem: ChatPrompt): Boolean {
            return oldItem == newItem
        }
    }
}
