package com.humotron.app.ui.decode

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.DialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.FragmentDecodeChatConversationsBinding
import dagger.hilt.android.AndroidEntryPoint

import com.humotron.app.ui.decode.adapter.ConversationAdapter
import com.humotron.app.domain.modal.response.Conversation
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.humotron.app.data.network.Status
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import com.humotron.app.util.utcOffsetToFullOrdinalDate
import com.humotron.app.util.DialogUtils
import android.widget.TextView

@AndroidEntryPoint
class DecodeChatConversationsFragment : DialogFragment() {

    private lateinit var binding: FragmentDecodeChatConversationsBinding
    private lateinit var adapter: ConversationAdapter
    private val viewModel: DecodeViewModel by viewModels()
    private var lastDeletedId: String? = null

    override fun getTheme(): Int = R.style.FullScreenDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDecodeChatConversationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start with transparent background, will animate to dim
        view.setBackgroundColor(Color.TRANSPARENT)

        // Hide sidebar initially to prevent flicker before animation
        binding.sidebar.visibility = View.INVISIBLE

        // Close when clicking outside the sidebar
        view.setOnClickListener {
            dismissWithAnimation()
        }

        // Prevent clicks from passing through the sidebar to the root
        binding.sidebar.setOnClickListener {
            // Do nothing, just consume the click
        }

        initRecyclerView()
        initClicks()
        initObservers()

        // Fetch threads
        viewModel.getAllConversationThreads()

        // Animate sidebar slide-in and background dim
        view.post {
            animateIn()
        }
    }

    private fun initRecyclerView() {
        adapter = ConversationAdapter(
            onDeleteClick = { conversation ->
                DialogUtils.showConfirmationDialog(
                    context = requireContext(),
                    title = "Confirmation",
                    message = "Are you sure you want to remove \"${conversation.title}\"?",
                ) {
                    lastDeletedId = conversation.id
                    viewModel.deleteConversationThread(conversation.id)
                }
            },
            onItemClick = { conversation ->
                // Pass selected conversation back to parent fragment
                parentFragmentManager.setFragmentResult("select_conversation", Bundle().apply { 
                    putString("conversationId", conversation.id)
                    putString("conversationTitle", conversation.title)
                })
                dismissWithAnimation()
            }
        )
        binding.rvConversations.adapter = adapter
    }

    private fun initObservers() {
        viewModel.conversationThreadsData().observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val threads = resource.data?.data
                    if (threads != null) {
                        val conversationList = threads.map { thread ->
                            Conversation(
                                id = thread.id ?: "",
                                title = thread.title ?: "Untitled",
                                date = utcOffsetToFullOrdinalDate(thread.createdAt)
                            )
                        }
                        adapter.submitList(conversationList)
                    }
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Error", Toast.LENGTH_SHORT).show()
                }
                Status.EXCEPTION -> {
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Exception", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    // Could show a progress bar if needed
                }
            }
        }

        viewModel.deleteThreadData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    Toast.makeText(requireContext(), "Conversation deleted", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult("conversation_deleted", Bundle().apply { 
                        putString("deletedId", lastDeletedId)
                    })
                    viewModel.getAllConversationThreads() // Refresh list
                }
                Status.ERROR, Status.EXCEPTION -> {
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Failed to delete", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {}
            }
        }

        viewModel.deleteAllThreadsData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    Toast.makeText(requireContext(), "All conversations deleted", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.setFragmentResult("start_new_chat", Bundle().apply { putBoolean("triggered", true) })
                    viewModel.getAllConversationThreads() // Refresh list
                }
                Status.ERROR, Status.EXCEPTION -> {
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Failed to delete all", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {}
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Draw behind system bars so dim covers status bar too
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(this, false)
            // Remove window-level animations since we handle them manually
            setWindowAnimations(0)
        }
    }

    private fun animateIn() {
        val sidebar = binding.sidebar

        // Position sidebar off-screen, then make visible and animate in
        sidebar.translationX = -sidebar.width.toFloat()
        sidebar.visibility = View.VISIBLE
        sidebar.animate()
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Fade in background dim
        val animator = ValueAnimator.ofInt(0, 128)
        animator.duration = 300
        animator.addUpdateListener { anim ->
            val alpha = anim.animatedValue as Int
            view?.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
        }
        animator.start()
    }

    private fun dismissWithAnimation() {
        val sidebar = binding.sidebar

        // Slide sidebar out to left
        sidebar.animate()
            .translationX(-sidebar.width.toFloat())
            .setDuration(250)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Fade out background dim
        val animator = ValueAnimator.ofInt(128, 0)
        animator.duration = 250
        animator.addUpdateListener { anim ->
            val alpha = anim.animatedValue as Int
            view?.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
        }
        animator.withEndAction {
            if (isAdded && parentFragmentManager != null) {
                dismissAllowingStateLoss()
            }
        }
        animator.start()
    }

    override fun dismiss() {
        dismissWithAnimation()
    }

    private fun ValueAnimator.withEndAction(action: () -> Unit) {
        addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                action()
            }
        })
    }

    private fun initClicks() {
        binding.ivClose.setOnClickListener {
            dismissWithAnimation()
        }

        binding.btnStartNew.setOnClickListener {
            // Start a new conversation logic
            parentFragmentManager.setFragmentResult("start_new_chat", Bundle().apply { putBoolean("triggered", true) })
            dismissWithAnimation()
        }

        binding.tvDeleteAll.setOnClickListener {
            DialogUtils.showConfirmationDialog(
                context = requireContext(),
                title = "Confirmation",
                message = "Are you sure you want to remove all conversations?",
            ) {
                viewModel.deleteAllConversationThreads()
            }
        }

        binding.tvDataInfo.setOnClickListener {
            Toast.makeText(requireContext(), "Showing data info", Toast.LENGTH_SHORT).show()
        }

        binding.llSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Settings", Toast.LENGTH_SHORT).show()
        }
    }
}
