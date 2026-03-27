package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDecodeChatMsgBinding
import com.humotron.app.domain.modal.response.ConversationData
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.core.view.isVisible

class DecodeChatAdapter(
    private var items: List<ConversationData> = emptyList(),
    private val onAnimateTyping: (Int) -> Unit = {},
    private val onUserMsgClick: (ConversationData) -> Unit = {}
) : RecyclerView.Adapter<DecodeChatAdapter.ViewHolder>() {

    private var animatedKeys = mutableSetOf<String>()

    fun clearAnimationState() {
        animatedKeys.clear()
    }

    fun markAllAsAnimated(newList: List<ConversationData>) {
        newList.forEachIndexed { index, item ->
            val aiMessage = item.botResponse?.message ?: ""
            val messageKey = "${index}_${aiMessage.hashCode()}"
            animatedKeys.add(messageKey)
        }
    }

    inner class ViewHolder(val binding: ItemDecodeChatMsgBinding) : RecyclerView.ViewHolder(binding.root) {
        private var typingJob: Job? = null
        private var currentAnimatingKey: String? = null

        fun bind(item: ConversationData, position: Int) {
            val hasUserMsg = !item.userMessage.isNullOrEmpty()
            binding.layoutUserMsg.isVisible = hasUserMsg
            if (hasUserMsg) {
                binding.tvUserMsg.text = item.userMessage
                binding.tvUserMsgDate.text = formatChatDate(item.createdAt)
            }

            binding.ivLink.setOnClickListener {
                onUserMsgClick(item)
            }

            val aiMessage = item.botResponse?.message ?: ""
            val isLoading = aiMessage.startsWith("Checking", ignoreCase = true)
            
            binding.lavLoading.isVisible = isLoading
            binding.tvAiResponse.isVisible = !isLoading

            if (isLoading) {
                typingJob?.cancel()
                currentAnimatingKey = null
                return
            }

            val messageKey = "${position}_${aiMessage.hashCode()}"
            
            // Only animate if it's the latest item and this specific content hasn't been animated
            val isLatest = position == itemCount - 1
            if (isLatest && !animatedKeys.contains(messageKey) && aiMessage.isNotEmpty()) {
                animatedKeys.add(messageKey)
                currentAnimatingKey = messageKey
                animateTyping(aiMessage)
            } else if (currentAnimatingKey == messageKey) {
                // Already animating this specific content, let it continue
            } else {
                typingJob?.cancel()
                currentAnimatingKey = null
                binding.tvAiResponse.text = aiMessage
            }
        }

        private fun animateTyping(text: String) {
            typingJob?.cancel()
            binding.tvAiResponse.text = ""
            
            val view = binding.tvAiResponse
            // Wait for view to be attached to ensure LifecycleOwner is available
            view.post {
                val lifecycleOwner = view.findViewTreeLifecycleOwner()
                if (lifecycleOwner == null) {
                    view.text = text
                    return@post
                }
                typingJob = lifecycleOwner.lifecycleScope.launch {
                    val words = text.split(" ")
                    for (i in words.indices) {
                        view.append(words[i])
                        if (i < words.size - 1) view.append(" ")
                        onAnimateTyping(bindingAdapterPosition)
                        delay(60) // Natural word-by-word typing speed (50ms)
                    }
                }
            }
        }

        private fun formatChatDate(dateStr: String?): String {
            if (dateStr.isNullOrEmpty()) return ""
            return try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                outputFormat.format(date ?: java.util.Date()).replace("AM", "am").replace("PM", "pm")
            } catch (e: Exception) {
                dateStr ?: ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDecodeChatMsgBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size

    fun submitList(newList: List<ConversationData>, isHistory: Boolean = false) {
        if (items == newList) return
        if (isHistory) {
            markAllAsAnimated(newList)
        }
        items = newList
        notifyDataSetChanged()
    }

    fun currentList() = items
}
