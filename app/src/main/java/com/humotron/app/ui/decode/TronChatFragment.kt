package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentTronChatBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.widget.doOnTextChanged
import androidx.core.view.isVisible
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.constraintlayout.widget.ConstraintLayout
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import com.humotron.app.data.network.Status
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.ui.decode.adapter.DecodeChatAdapter
import com.humotron.app.domain.modal.response.ConversationData
import com.humotron.app.domain.modal.response.BotResponse

@AndroidEntryPoint
class TronChatFragment : BaseFragment(R.layout.fragment_tron_chat) {

    private lateinit var binding: FragmentTronChatBinding
    private val viewModel: DecodeViewModel by activityViewModels()
    private val shopViewModel: com.humotron.app.ui.shop.ShopToolsViewModel by activityViewModels()
    private var autoScrollEnabled = true
    private var isHistoryLoading = false
    private var isMessageSentInSession = false
    
    private val headerAdapter = HeaderAdapter()
    
    inner class HeaderAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
        var isVisible: Boolean = true
            set(value) {
                if (field != value) {
                    field = value
                    notifyDataSetChanged()
                }
            }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
            val tv = androidx.appcompat.widget.AppCompatTextView(parent.context).apply {
                layoutParams = androidx.recyclerview.widget.RecyclerView.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (30 * resources.displayMetrics.density).toInt())
                }
                alpha = 0.3f
                gravity = android.view.Gravity.CENTER_VERTICAL
                text = getString(R.string.tron_chat_headline)
                setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.white))
                textSize = 26f
                androidx.core.widget.TextViewCompat.setTextAppearance(this, R.style.Text_26x_Manrope_SemiBold)
            }
            return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(tv) {}
        }
        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {}
        override fun getItemCount() = if (isVisible) 1 else 0
    }

    private val chatAdapter: DecodeChatAdapter by lazy { 
        DecodeChatAdapter(
            onAnimateTyping = { position ->
                if (autoScrollEnabled) {
                    scrollToBottom()
                }
            },
            onUserMsgClick = { item ->
                DecodeChatMessageDetailsFragment.newInstance(item).show(childFragmentManager, "message_details")
            },
            onUnlockBoosterClick = { booster ->
                val productDetails = shopViewModel.getProductDetailsForId(booster.playStoreProductId)
                if (productDetails != null) {
                    shopViewModel.launchBillingFlow(
                        activity = requireActivity(),
                        booster = booster,
                        productDetails = productDetails
                    )
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        getString(R.string.item_not_available_play_store),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onHowItWorksClick = { booster ->
                val bundle = Bundle().apply {
                    putParcelable("booster", booster)
                }
                viewModel.navigatedToBoosterDetails = true
                findNavController().navigate(R.id.fragmentShopBoosterDetail, bundle)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTronChatBinding.bind(view)

        initViews()
        initClicks()
        initObservers()
        initResultListeners()

        // Handle direct prompt from Shop screen
        val promptId = arguments?.getString("chat_prompt_id")
        val promptTitle = arguments?.getString("chat_prompt_title")
        
        if (!promptId.isNullOrEmpty() && !promptTitle.isNullOrEmpty()) {
            isHistoryLoading = false
            headerAdapter.isVisible = true
            chatAdapter.clearAnimationState()
            sendMessage(promptTitle, promptId)
            arguments?.remove("chat_prompt_id")
            arguments?.remove("chat_prompt_title")
        } else {
            val threadId = viewModel.getThreadId()
            if (threadId != null && viewModel.navigatedToBoosterDetails) {
                viewModel.navigatedToBoosterDetails = false
                binding.rvChat.isVisible = true
                val boosterProductIds = chatAdapter.currentList()
                    .mapNotNull { it.boosterAiChat?.androidProductId?.ifEmpty { null } }
                if (boosterProductIds.isNotEmpty()) {
                    shopViewModel.queryProductsIfNeeded(boosterProductIds)
                }
                if (chatAdapter.itemCount > 0) {
                    binding.rvChat.post {
                        binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (shopViewModel.isBillingFlowActive) {
            shopViewModel.clearBillingFlowActive()
        } else {
            shopViewModel.refreshPurchases()
            val boosterProductIds = chatAdapter.currentList()
                .mapNotNull { it.boosterAiChat?.androidProductId?.ifEmpty { null } }
            if (boosterProductIds.isNotEmpty()) {
                shopViewModel.queryProductsIfNeeded(boosterProductIds)
            }
        }
    }

    private fun initResultListeners() {
        childFragmentManager.setFragmentResultListener("start_new_chat", viewLifecycleOwner) { _, _ ->
            startNewChat()
        }
        childFragmentManager.setFragmentResultListener("select_conversation", viewLifecycleOwner) { _, bundle ->
            val conversationId = bundle.getString("conversationId")
            val conversationTitle = bundle.getString("conversationTitle")
            if (conversationId != null) {
                loadConversation(conversationId, conversationTitle)
            }
        }
        childFragmentManager.setFragmentResultListener("conversation_deleted", viewLifecycleOwner) { _, bundle ->
            val deletedId = bundle.getString("deletedId")
            if (deletedId != null && deletedId == viewModel.getThreadId()) {
                startNewChat()
            }
        }
    }

    private fun startNewChat() {
        isHistoryLoading = false
        headerAdapter.isVisible = false
        viewModel.resetThreadId()
        
        // Redirect specifically to the Decode selection screen (Chat Tab)
        DecodeFragment.selectedTabPosition = 3
        findNavController().navigate(R.id.fragmentDecode)
    }

    private fun loadConversation(conversationId: String, title: String? = null) {
        isHistoryLoading = true
        headerAdapter.isVisible = false
        chatAdapter.clearAnimationState()
        
        // Show a loading item for history mode
        val loadingItem = ConversationData(
            id = null,
            userMessage = title ?: getString(R.string.chat_history_title),
            botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_loading_general)),
            createdAt = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
        chatAdapter.submitList(listOf(loadingItem))
        
        viewModel.getConversationsByUserId(conversationThreadId = conversationId, metricName = title)
    }

    private fun initViews() {
        val concatAdapter = androidx.recyclerview.widget.ConcatAdapter(headerAdapter, chatAdapter)
        binding.rvChat.adapter = concatAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())

        binding.rvChat.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.canScrollVertically(1)) {
                    if (recyclerView.scrollState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING) {
                        autoScrollEnabled = false
                    }
                } else {
                    autoScrollEnabled = true
                }
            }
        })

        binding.etInput.doOnTextChanged { text, _, _, _ ->
            binding.ivSend.isVisible = !text.isNullOrEmpty()
        }

        binding.etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etInput.windowToken, 0)
                binding.etInput.clearFocus()
                true
            } else {
                false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.contentRoot) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            
            // Apply padding to container to move everything up
            v.updatePadding(bottom = if (isKeyboardVisible) imeInsets.bottom else systemInsets.bottom)
            
            binding.tvDisclaimer.isVisible = !isKeyboardVisible
            if (!isKeyboardVisible && binding.etInput.isFocused) {
                binding.etInput.clearFocus()
            }
            
            insets
        }
    }

    private fun initClicks() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardBack.setOnClickListener {
            DecodeChatConversationsFragment().show(childFragmentManager, "conversations")
        }

        binding.btnHistory.setOnClickListener {
            DecodeChatConversationsFragment().show(childFragmentManager, "conversations")
        }

        binding.ivSend.setOnClickListener {
            val message = binding.etInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etInput.setText("")
            }
        }
    }

    private fun sendMessage(message: String, promptId: String? = null) {
        isMessageSentInSession = true
        val newItem = ConversationData(
            id = null,
            userMessage = message,
            botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_loading_metrics)),
            createdAt = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
        val currentList = chatAdapter.currentList().toMutableList()
        currentList.add(newItem)
        chatAdapter.submitList(currentList)
        scrollToBottom()
        
        if (viewModel.getThreadId() != null) {
            viewModel.postFollowUpConversation(message)
        } else {
            viewModel.getConversationsByUserId(promptId = promptId ?: message, metricName = message)
        }
    }

    private fun scrollToBottom() {
        binding.rvChat.post {
            if (chatAdapter.itemCount > 0) {
                binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount)
            }
        }
    }

    private fun initObservers() {
        viewModel.conversationsData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                val rawList = (resource.data?.data ?: emptyList()).reversed()
                val wasMessageSent = isMessageSentInSession
                isMessageSentInSession = false
                val apiList = rawList.mapIndexed { index, item ->
                    val isLast = index == rawList.size - 1
                    item.copy(isNewMessage = isLast && wasMessageSent)
                }
                val boosterProductIds = apiList.mapNotNull { it.boosterAiChat?.toBooster()?.playStoreProductId }
                shopViewModel.queryProductsIfNeeded(boosterProductIds)
                if (apiList.isNotEmpty()) {
                    // Logic: If more than 1 item, treat as history (direct show), UNLESS we just sent a message.
                    // If we just sent a message, animate it (isHistory = false).
                    val shouldShowDirect = (!wasMessageSent && apiList.size > 1) || isHistoryLoading
                    chatAdapter.submitList(apiList, isHistory = shouldShowDirect)
                    
                    // Reset history flag after successful load
                    isHistoryLoading = false
                    
                    if (autoScrollEnabled) {
                        scrollToBottom()
                    }
                }
            }
        }

        viewModel.followUpData().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                val latestApiItem = resource.data?.data
                latestApiItem?.boosterAiChat?.toBooster()?.playStoreProductId?.let { id ->
                    shopViewModel.queryProductsIfNeeded(listOf(id))
                }
                if (latestApiItem != null) {
                    val currentList = chatAdapter.currentList().toMutableList()
                    val loadingIndex = currentList.indexOfLast { it.id == null }
                    if (loadingIndex != -1) {
                        val updatedItem = currentList[loadingIndex].copy(
                            id = latestApiItem.id,
                            botResponse = latestApiItem.botResponse,
                            createdAt = latestApiItem.createdAt ?: currentList[loadingIndex].createdAt,
                            boosterAiChat = latestApiItem.boosterAiChat,
                            isNewMessage = isMessageSentInSession
                        )
                        isMessageSentInSession = false
                        currentList[loadingIndex] = updatedItem
                        // This is a new follow-up, so isHistory should be false (default)
                        chatAdapter.submitList(currentList)
                        if (autoScrollEnabled) {
                            scrollToBottom()
                        }
                    }
                }
            }
        }

        shopViewModel.playStoreProductsLiveData.observe(viewLifecycleOwner) { products ->
            chatAdapter.setPlayStoreProducts(products)
        }

        shopViewModel.activePurchasesLiveData.observe(viewLifecycleOwner) { purchases ->
            val hasPurchasedLockedBooster = chatAdapter.currentList().any { item ->
                item.boosterAiChat != null &&
                item.boosterAiChat.isActive == false &&
                purchases.any { purchase ->
                    purchase.products.contains(item.boosterAiChat.androidProductId)
                }
            }
            if (hasPurchasedLockedBooster) {
                val currentThreadId = viewModel.getThreadId()
                if (currentThreadId != null) {
                    viewModel.getConversationsByUserId(conversationThreadId = currentThreadId)
                }
            }
        }

        shopViewModel.orderResultLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.unlocked_success),
                        Toast.LENGTH_LONG
                    ).show()
                    val threadId = viewModel.getThreadId()
                    if (threadId != null) {
                        viewModel.getConversationsByUserId(conversationThreadId = threadId)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    Toast.makeText(
                        requireContext(),
                        resource.error?.errorMessage ?: getString(R.string.order_activation_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        shopViewModel.purchaseErrorEvent.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(
                requireContext(),
                getString(R.string.purchase_failed_format, errorMsg),
                Toast.LENGTH_SHORT
            ).show()
        }

        shopViewModel.purchaseCancelEvent.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                getString(R.string.transaction_canceled),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

