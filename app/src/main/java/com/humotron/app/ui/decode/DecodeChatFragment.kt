package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.ui.decode.adapter.DecodeChatAdapter
import com.humotron.app.domain.modal.response.ConversationData
import com.humotron.app.domain.modal.response.BotResponse
import com.humotron.app.domain.modal.response.FeltOffQuestionData

@AndroidEntryPoint
class DecodeChatFragment : BaseFragment(R.layout.fragment_decode_chat) {

    private lateinit var binding: FragmentDecodeChatBinding
    private val viewModel: DecodeViewModel by viewModels()
    private var autoScrollEnabled = true
    private val chatAdapter by lazy { 
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
            }
        )
    }
    private var selectedMetricLabel: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeChatBinding.bind(view)

        initViews()
        initClicks()
        initObservers()
        initResultListeners()
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
    }

    private fun initViews() {
        binding.rvChat.adapter = chatAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())

        binding.rvChat.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // If the user scrolls up, disable auto-scroll.
                // canScrollVertically(1) returns true if the view can scroll down (meaning user is NOT at bottom)
                if (recyclerView.canScrollVertically(1)) {
                    // Only disable if the user is actively dragging.
                    if (recyclerView.scrollState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING) {
                        autoScrollEnabled = false
                    }
                } else {
                    // User reached the bottom, enable auto-scroll.
                    autoScrollEnabled = true
                }
            }
        })

        binding.etInput.doOnTextChanged { text, _, _, _ ->
            binding.ivSend.isVisible = !text.isNullOrEmpty()
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
            viewModel.getConversationsByUserId(message)
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
                    val list = (resource.data?.data ?: emptyList()).reversed() // Reverse to show oldest first if API returns newest first
                    val latestApiItem = list.lastOrNull()
                    
                    if (latestApiItem != null) {
                        val currentList = chatAdapter.currentList().toMutableList()
                        if (currentList.isNotEmpty() && currentList.last().id == null) {
                            // Merge API response into our local/loading item
                            val updatedItem = currentList.last().copy(
                                id = latestApiItem.id,
                                botResponse = latestApiItem.botResponse,
                                createdAt = latestApiItem.createdAt ?: currentList.last().createdAt
                            )
                            currentList[currentList.size - 1] = updatedItem
                            chatAdapter.submitList(currentList)
                        } else {
                            // No local item to merge with, likely loading a history thread
                            chatAdapter.submitList(list)
                        }
                    } else if (list.isEmpty()) {
                        val currentList = chatAdapter.currentList().toMutableList()
                        if (currentList.isNotEmpty() && currentList.last().id == null) {
                            // API returned nothing for our prompt, show a 'No data' response
                            val updatedItem = currentList.last().copy(
                                botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_no_insights))
                            )
                            currentList[currentList.size - 1] = updatedItem
                            chatAdapter.submitList(currentList)
                        } else {
                            chatAdapter.submitList(emptyList())
                        }
                    }

                    binding.rvChat.postDelayed({
                        if (chatAdapter.itemCount > 0) {
                            binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }, 100)
                }
                Status.ERROR, Status.EXCEPTION -> {
                    val errorItem = ConversationData(
                        id = null,
                        userMessage = "Help me understand my $selectedMetricLabel over the last 30 days and what I can do to improve it.",
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


                    if (latestApiItem != null) {
                        val currentList = chatAdapter.currentList().toMutableList()
                        val loadingIndex = currentList.indexOfLast { it.id == null }
                        if (loadingIndex != -1) {
                            val updatedItem = currentList[loadingIndex].copy(
                                id = latestApiItem.id,
                                botResponse = latestApiItem.botResponse,
                                createdAt = latestApiItem.createdAt ?: currentList[loadingIndex].createdAt
                            )
                            currentList[loadingIndex] = updatedItem
                            chatAdapter.submitList(currentList)
                        } else {
                            currentList.add(latestApiItem)
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
    }


    private fun showMetricChat(metric: ActiveMetric) {
        selectedMetricLabel = metric.label ?: ""
        binding.nsvContent.isVisible = false
        binding.rvChat.isVisible = true
        chatAdapter.clearAnimationState()
        
        // Show immediate user message with a loading indicator in the bot response
        val loadingItem = ConversationData(
            id = null,
            userMessage = "Analyze my $selectedMetricLabel over the last 30 days.",
            botResponse = BotResponse(success = true, message = getString(R.string.chat_bot_loading_metrics)),
            createdAt = java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
        chatAdapter.submitList(listOf(loadingItem))
        
        viewModel.getConversationsByUserId(metric.id, metric.label)
    }

    private fun showQuestionChat(question: FeltOffQuestionData) {
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

        viewModel.getConversationsByUserId(question.id ?: "", metricName = question.question)
    }

    private fun startNewChat() {
        viewModel.resetThreadId()
        binding.nsvContent.isVisible = true
        binding.rvChat.isVisible = false
        chatAdapter.submitList(emptyList())
        binding.etInput.setText("")
    }

    private fun loadConversation(conversationId: String, title: String? = null) {
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
        
        viewModel.getConversationsByUserId(conversationId, title)
    }
}
