package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDecodeChatBoosterBinding
import com.humotron.app.databinding.ItemDecodeChatMsgBinding
import com.humotron.app.domain.modal.response.ConversationData
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.core.view.isVisible

class DecodeChatAdapter(
    private var items: List<ConversationData> = emptyList(),
    private val onAnimateTyping: (Int) -> Unit = {},
    private val onUserMsgClick: (ConversationData) -> Unit = {},
    private val onUnlockBoosterClick: (com.humotron.app.domain.modal.response.BoosterResponse.Booster) -> Unit = {},
    private val onHowItWorksClick: (com.humotron.app.domain.modal.response.BoosterResponse.Booster) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE = 1
        private const val VIEW_TYPE_BOOSTER = 2
    }

    private var animatedKeys = mutableSetOf<String>()
    private var playStoreProducts = emptyList<com.android.billingclient.api.ProductDetails>()

    fun setPlayStoreProducts(products: List<com.android.billingclient.api.ProductDetails>) {
        this.playStoreProducts = products
        notifyDataSetChanged()
    }

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

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        val isPremiumLocked = item.isNewMessage && item.boosterAiChat != null && item.boosterAiChat.isActive == false
        return if (isPremiumLocked) VIEW_TYPE_BOOSTER else VIEW_TYPE_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_BOOSTER) {
            BoosterViewHolder(ItemDecodeChatBoosterBinding.inflate(inflater, parent, false))
        } else {
            MessageViewHolder(ItemDecodeChatMsgBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is BoosterViewHolder) {
            holder.bind(item)
        } else if (holder is MessageViewHolder) {
            holder.bind(item, position)
        }
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

    inner class BoosterViewHolder(private val binding: ItemDecodeChatBoosterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConversationData) {
            val hasUserMsg = !item.userMessage.isNullOrEmpty()
            binding.layoutUserMsg.isVisible = hasUserMsg
            if (hasUserMsg) {
                binding.tvUserMsg.text = item.userMessage
                binding.tvUserMsgDate.text = formatChatDate(item.createdAt)
            }

            binding.ivLink.setOnClickListener {
                onUserMsgClick(item)
            }

            val boosterChat = item.boosterAiChat!!
            
            val booster = boosterChat.toBooster()
            val matchedProduct = playStoreProducts.find { it.productId == booster.playStoreProductId }
            val subscriptionOffer = matchedProduct?.subscriptionOfferDetails?.firstOrNull()
            val subscriptionPrice = subscriptionOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
            val priceText = subscriptionPrice ?: matchedProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: booster.displayPriceFallback
            binding.btnContinue.text = binding.root.context.getString(com.humotron.app.R.string.chat_booster_continue_price, priceText)

            binding.btnContinue.setOnClickListener {
                onUnlockBoosterClick(booster)
            }
            binding.tvHowItWorks.setOnClickListener {
                onHowItWorksClick(booster)
            }
        }
    }

    inner class MessageViewHolder(val binding: ItemDecodeChatMsgBinding) : RecyclerView.ViewHolder(binding.root) {
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
                        
                        onAnimateTyping(bindingAdapterPosition)
                        
                        val wordsToWait = if (totalWords > 300) 2 else 1
                        if (i % wordsToWait == 0) {
                            delay(typingDelay)
                        }
                    }
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
    }
}
