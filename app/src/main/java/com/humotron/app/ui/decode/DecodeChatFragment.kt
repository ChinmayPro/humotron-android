package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeChatBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.widget.doOnTextChanged
import androidx.core.view.isVisible
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.constraintlayout.widget.ConstraintLayout
import com.humotron.app.domain.modal.ui.ActiveMetric
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import com.humotron.app.data.network.Status
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.ui.decode.adapter.DecodeChatAdapter
import com.humotron.app.domain.modal.response.ConversationData
import com.humotron.app.domain.modal.response.BotResponse
import com.humotron.app.domain.modal.response.FeltOffQuestionData
import androidx.activity.OnBackPressedCallback

@AndroidEntryPoint
class DecodeChatFragment : BaseFragment(R.layout.fragment_decode_chat) {

    private lateinit var binding: FragmentDecodeChatBinding
    private val viewModel: DecodeViewModel by activityViewModels()
    private val shopViewModel: com.humotron.app.ui.shop.ShopToolsViewModel by activityViewModels()
    private var autoScrollEnabled = true
    private var isHistoryLoading = false
    private val chatAdapter: DecodeChatAdapter by lazy { 
        DecodeChatAdapter(
            onAnimateTyping = { position ->
                if (autoScrollEnabled) {
                    binding.rvChat.post {
                        binding.rvChat.scrollBy(0, 1000)
                    }
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
    private var selectedMetricLabel: String = ""
    private var isMessageSentInSession = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeChatBinding.bind(view)

        initViews()
        initClicks()
        initObservers()
        initResultListeners()
        handleBackPress()

        val threadId = viewModel.getThreadId()
        if (threadId != null && viewModel.navigatedToBoosterDetails) {
            viewModel.navigatedToBoosterDetails = false
            // Fragment instance survived — adapter already holds the chat data (booster card etc.)
            // Just restore the view visibility without any API call
            binding.nsvContent.isVisible = false
            binding.rvChat.isVisible = true
            // Refresh Play Store product details (prices) for any booster in current items
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
        } else {
            binding.nsvContent.isVisible = true
            binding.rvChat.isVisible = false
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

    private fun handleBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.rvChat.isVisible) {
                    startNewChat()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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


    private fun initViews() {
        binding.rvChat.adapter = chatAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())

        binding.rvChat.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // If the user scrolls up, disable auto-scroll.
                if (recyclerView.canScrollVertically(1)) {
                    if (recyclerView.scrollState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING) {
                        autoScrollEnabled = false
                    }
                } else {
                    autoScrollEnabled = true
                }

                // Load more when reaching top
                if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                    val threadId = viewModel.getThreadId()
                    if (threadId != null) {
                        viewModel.getConversationsByUserId(conversationThreadId = threadId, isLoadMore = true)
                    }
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
            
            val extraNavPadding = (40 * resources.displayMetrics.density).toInt()
            val buffer = (10 * resources.displayMetrics.density).toInt()
            val paddingBottom = if (isKeyboardVisible) {
                maxOf(0, imeInsets.bottom - (systemInsets.bottom + extraNavPadding) + buffer)
            } else {
                0
            }
            
            v.updatePadding(bottom = paddingBottom)
            
            binding.tvDisclaimer.isVisible = !isKeyboardVisible
            if (!isKeyboardVisible && binding.etInput.isFocused) {
                binding.etInput.clearFocus()
            }
            
            val params = binding.layoutBottom.layoutParams as ConstraintLayout.LayoutParams
            if (isKeyboardVisible) {
                params.bottomMargin = 0
            } else {
                params.bottomMargin = (10 * resources.displayMetrics.density).toInt()
            }
            binding.layoutBottom.layoutParams = params
            
            insets
        }
    }

    private fun initClicks() {
        binding.cardBack.setOnClickListener {
            DecodeChatConversationsFragment().show(childFragmentManager, "conversations")
        }

        binding.btnOptionMetrics.setOnClickListener {
            findNavController().navigate(R.id.fragmentDecodeMetrics)
        }
        binding.btnOptionFeltOff.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "FELT_OFF")
                putString("title", getString(R.string.chat_option_felt_off_desc))
            }
            findNavController().navigate(R.id.fragmentDecodeQuestions, bundle)
        }
        binding.btnOptionNutrition.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "NUTRITION")
                putString("title", getString(R.string.chat_option_nutrition_desc))
            }
            findNavController().navigate(R.id.fragmentDecodeQuestions, bundle)
        }
        binding.btnOptionOptimize.setOnClickListener { /* Handle click */ }
        binding.ivSend.setOnClickListener {
            val message = binding.etInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etInput.setText("")
            }
        }
    }

    private fun sendMessage(message: String) {
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
        binding.rvChat.post {
            binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
        
        // Trigger API call for manual message
        if (viewModel.getThreadId() != null) {
            viewModel.postFollowUpConversation(message)
        } else {
            viewModel.getConversationsByUserId(promptId = message)
        }
    }

    private fun initObservers() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ActiveMetric>("selected_metric")
            ?.observe(viewLifecycleOwner) { metric ->
                showMetricChat(metric)
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<ActiveMetric>("selected_metric")
            }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<FeltOffQuestionData>("selected_question")
            ?.observe(viewLifecycleOwner) { question ->
                showQuestionChat(question)
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<FeltOffQuestionData>("selected_question")
            }

        viewModel.conversationsData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val rawList = (resource.data?.data ?: emptyList()).reversed()
                    val wasMessageSent = isMessageSentInSession
                    isMessageSentInSession = false
                    val apiList = rawList.mapIndexed { index, item ->
                        val isLast = index == rawList.size - 1
                        item.copy(isNewMessage = isLast && wasMessageSent)
                    }
                    val boosterProductIds = apiList.mapNotNull { it.boosterAiChat?.toBooster()?.playStoreProductId }
                    shopViewModel.queryProductsIfNeeded(boosterProductIds)
                    val currentList = chatAdapter.currentList().toMutableList()

                    if (apiList.isNotEmpty()) {
                        val shouldShowDirect = (!wasMessageSent && apiList.size > 1) || isHistoryLoading
                        
                        // Check if these are new items to prepend (pagination)
                        val firstApiId = apiList.first().id
                        val alreadyHasFirst = currentList.any { it.id == firstApiId }

                        if (!alreadyHasFirst && currentList.isNotEmpty() && !currentList.last().id.isNullOrEmpty()) {
                            // This looks like pagination (older items)
                            currentList.addAll(0, apiList)
                            chatAdapter.submitList(currentList, isHistory = true)
                        } else {
                            // Fresh load or update.
                            val isFirstLoad = currentList.isEmpty() || (currentList.size == 1 && currentList[0].id == null)
                            
                            if (isFirstLoad && currentList.isNotEmpty() && apiList.isNotEmpty()) {
                                // Keep our local userMessage for the first message to avoid flickering/changing text
                                val mergedList = apiList.toMutableList()
                                val firstItem = mergedList[0].copy(userMessage = currentList[0].userMessage)
                                mergedList[0] = firstItem
                                chatAdapter.submitList(mergedList, isHistory = shouldShowDirect)
                            } else {
                                chatAdapter.submitList(apiList, isHistory = shouldShowDirect)
                            }
                        }
                        
                        // Reset history flag after submitting
                        isHistoryLoading = false
                    } else if (apiList.isEmpty() && currentList.isNotEmpty() && currentList.last().id == null) {
                        // API returned nothing for our prompt, show a 'No data' response
                        val updatedItem = currentList.last().copy(
                            botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_no_insights))
                        )
                        currentList[currentList.size - 1] = updatedItem
                        chatAdapter.submitList(currentList)
                    } else {
                        chatAdapter.submitList(apiList, isHistory = true)
                    }

                    binding.rvChat.postDelayed({
                        if (chatAdapter.itemCount > 0 && autoScrollEnabled) {
                            binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }, 100)
                }
                Status.ERROR, Status.EXCEPTION -> {
                    val errorItem = ConversationData(
                        id = null,
                        userMessage = getString(R.string.chat_help_understand_metric_prompt, selectedMetricLabel),
                        botResponse = BotResponse(success = false, message = getString(R.string.chat_error_fetch_insights)),
                        createdAt = ""
                    )
                    chatAdapter.submitList(listOf(errorItem))
                }
                else -> {}
            }
        }

        viewModel.followUpData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
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
                            chatAdapter.submitList(currentList)
                        } else {
                            currentList.add(latestApiItem.copy(isNewMessage = isMessageSentInSession))
                            isMessageSentInSession = false
                            chatAdapter.submitList(currentList)
                        }
                    }

                    binding.rvChat.postDelayed({
                        if (chatAdapter.itemCount > 0) {
                            binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }, 100)
                }
                Status.ERROR, Status.EXCEPTION -> {
                    isMessageSentInSession = false
                    val currentList = chatAdapter.currentList().toMutableList()
                    val loadingIndex = currentList.indexOfLast { it.id == null }
                    if (loadingIndex != -1) {
                        val errorItem = currentList[loadingIndex].copy(
                            botResponse = BotResponse(success = false, message = getString(R.string.chat_error_fetch_response))
                        )
                        currentList[loadingIndex] = errorItem
                        chatAdapter.submitList(currentList)
                    }
                }
                else -> {}
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


    private fun showMetricChat(metric: ActiveMetric) {
        isHistoryLoading = false
        isMessageSentInSession = true
        selectedMetricLabel = metric.label ?: ""
        binding.nsvContent.isVisible = false
        binding.rvChat.isVisible = true
        chatAdapter.clearAnimationState()
        
        // Show immediate user message with a loading indicator in the bot response
        val loadingItem = ConversationData(
            id = null,
            userMessage = getString(R.string.chat_analyze_metric_prompt, selectedMetricLabel),
            botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_loading_metrics)),
            createdAt = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
        chatAdapter.submitList(listOf(loadingItem))
        
        viewModel.getConversationsByUserId(promptId = metric.id, metricName = metric.label)
    }

    private fun showQuestionChat(question: FeltOffQuestionData) {
        isHistoryLoading = false
        isMessageSentInSession = true
        selectedMetricLabel = ""
        binding.nsvContent.isVisible = false
        binding.rvChat.isVisible = true
        chatAdapter.clearAnimationState()

        val loadingItem = ConversationData(
            id = null,
            userMessage = question.question ?: "",
            botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_loading_general)),
            createdAt = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
        chatAdapter.submitList(listOf(loadingItem))

        viewModel.getConversationsByUserId(promptId = question.id ?: "", metricName = question.question)
    }

    private fun startNewChat() {
        isHistoryLoading = false
        viewModel.resetThreadId()
        binding.nsvContent.isVisible = true
        binding.rvChat.isVisible = false
        chatAdapter.submitList(emptyList())
        binding.etInput.setText("")
    }

    private fun loadConversation(conversationId: String, title: String? = null) {
        isHistoryLoading = true
        selectedMetricLabel = ""
        binding.nsvContent.isVisible = false
        binding.rvChat.isVisible = true
        chatAdapter.clearAnimationState()
        
        // Show a loading item for history mode with the conversation title
        val loadingItem = ConversationData(
            id = null,
            userMessage = title ?: getString(R.string.chat_history_title),
            botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_loading_general)),
            createdAt = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
        chatAdapter.submitList(listOf(loadingItem))
        
        viewModel.getConversationsByUserId(conversationThreadId = conversationId, metricName = title)
    }
}
