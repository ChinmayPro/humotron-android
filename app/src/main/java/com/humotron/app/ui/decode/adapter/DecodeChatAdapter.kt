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
        newList.forEach { item ->
            val aiMessage = item.botResponse?.message ?: ""
            val itemKey = item.id ?: "${item.createdAt}_${item.userMessage?.hashCode()}"
            val messageKey = "${itemKey}_${aiMessage.hashCode()}"
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

            // Stable key using ID if available, otherwise a combination of timestamp and user message hash
            // This prevents animation restarts on scrolling or list updates
            val itemKey = item.id ?: "${item.createdAt}_${item.userMessage?.hashCode()}"
            val messageKey = "${itemKey}_${aiMessage.hashCode()}"
            
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
                binding.tvAiResponse.text = androidx.core.text.HtmlCompat.fromHtml(convertMarkdownToHtml(aiMessage), androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
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
                    view.text = androidx.core.text.HtmlCompat.fromHtml(convertMarkdownToHtml(text), androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
                    return@post
                }
                typingJob = lifecycleOwner.lifecycleScope.launch {
                    val words = text.split(" ")
                    val totalWords = words.size
                    
                    // Dynamic typing speed: faster for longer messages to keep user engaged without extreme waiting
                    // Shorter messages: ~60ms per word
                    // Longer messages: Reduces down to ~15ms per word for 500+ words
                    val typingDelay = when {
                        totalWords > 400 -> 15L
                        totalWords > 200 -> 30L
                        totalWords > 100 -> 45L
                        else -> 60L
                    }

                    val builder = StringBuilder()
                    for (i in words.indices) {
                        builder.append(words[i])
                        if (i < words.size - 1) builder.append(" ")
                        
                        val currentHtml = convertMarkdownToHtml(builder.toString())
                        view.text = androidx.core.text.HtmlCompat.fromHtml(currentHtml, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
                        
                        // Scroll request
                        onAnimateTyping(bindingAdapterPosition)
                        
                        // Small optimization: for very long responses, we can type multiple words per frame
                        // to make it look smooth but faster.
                        val wordsToWait = if (totalWords > 300) 2 else 1
                        if (i % wordsToWait == 0) {
                            delay(typingDelay)
                        }
                    }
                    // Ensure full text is set at the end just in case
                    view.text = androidx.core.text.HtmlCompat.fromHtml(convertMarkdownToHtml(text), androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
                }
            }
        }

        private fun convertMarkdownToHtml(text: String): String {
            return text
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                .replace(Regex("__(.*?)__"), "<b>$1</b>")
                .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
                .replace(Regex("_(.*?)_"), "<i>$1</i>")
                .replace("\n", "<br>")
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
