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
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import androidx.fragment.app.activityViewModels

@AndroidEntryPoint
class DecodeChatFragment : BaseFragment(R.layout.fragment_decode_chat) {

    private lateinit var binding: FragmentDecodeChatBinding
    private val viewModel: DecodeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeChatBinding.bind(view)

        initViews()
        initClicks()
        initObservers()
        initResultListeners()
    }

    private fun initViews() {
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
            
            if (!isKeyboardVisible && binding.etInput.isFocused) {
                binding.etInput.clearFocus()
            }
            
            val params = binding.layoutBottom.layoutParams as ConstraintLayout.LayoutParams
            if (isKeyboardVisible) {
                params.bottomMargin = (10 * resources.displayMetrics.density).toInt()
            } else {
                params.bottomMargin = (40 * resources.displayMetrics.density).toInt()
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
        binding.ivSend.setOnClickListener {
            val message = binding.etInput.text.toString().trim()
            if (message.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("chat_prompt_id", message)
                    putString("chat_prompt_title", message)
                }
                findNavController().navigate(R.id.fragmentTronChat, bundle)
                binding.etInput.setText("")
            }
        }
    }

    private fun initObservers() {
        // Metrics and Questions fragments now navigate directly to TronChat,
        // so savedStateHandle observers are no longer needed here.
    }

    private fun initResultListeners() {
        childFragmentManager.setFragmentResultListener("start_new_chat", viewLifecycleOwner) { _, _ ->
            startNewChat()
        }
        childFragmentManager.setFragmentResultListener("select_conversation", viewLifecycleOwner) { _, bundle ->
            val conversationId = bundle.getString("conversationId")
            val conversationTitle = bundle.getString("conversationTitle")
            if (conversationId != null) {
                val navBundle = Bundle().apply {
                    putString("chat_conversation_id", conversationId)
                    putString("chat_prompt_title", conversationTitle)
                }
                findNavController().navigate(R.id.fragmentTronChat, navBundle)
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
        viewModel.resetThreadId()
        binding.etInput.setText("")
    }
}
