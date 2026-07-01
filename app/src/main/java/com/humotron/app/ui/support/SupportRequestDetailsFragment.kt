package com.humotron.app.ui.support

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSupportRequestDetailsBinding
import com.humotron.app.domain.modal.response.TicketAttachment
import com.humotron.app.domain.modal.response.TicketDetail
import com.humotron.app.domain.modal.response.TicketMessage
import com.humotron.app.ui.support.adapter.SelectedAttachmentAdapter
import com.humotron.app.ui.support.adapter.TicketMessageAdapter
import com.humotron.app.util.showToast
import com.humotron.app.util.utcOffsetToLocalTime
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class SupportRequestDetailsFragment : BaseFragment(R.layout.fragment_support_request_details) {

    private lateinit var binding: FragmentSupportRequestDetailsBinding
    private val viewModel: SupportViewModel by activityViewModels()

    private lateinit var messageAdapter: TicketMessageAdapter
    private lateinit var selectedAttachmentAdapter: SelectedAttachmentAdapter

    private val selectedImages = mutableListOf<Uri>()

    // ActivityResultLauncher to pick images
    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (selectedImages.size < 5) {
                selectedImages.add(it)
                updateSelectedAttachmentsUI()
            } else {
                requireContext().showToast(getString(R.string.support_chat_max_files))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportRequestDetailsBinding.bind(view)

        val ticket = arguments?.getParcelable<TicketDetail>("ticket") ?: return
        val ticketId = ticket.id ?: return

        setupHeader()
        setupSummaryCard(ticket)
        setupRecyclerViews()
        setupInputHandlers(ticketId)
        setupObservers(ticketId)
        setupKeyboardManagement()

        // Load full ticket detail with messages from network
        viewModel.fetchTicketDetail(ticketId)
    }

    private fun setupHeader() {
        binding.header.title.text = getString(R.string.support_request_details)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupSummaryCard(ticket: TicketDetail) {
        val ticketIdText = when {
            !ticket.ticketNumber.isNullOrEmpty() -> "#${ticket.ticketNumber}"
            !ticket.id.isNullOrEmpty() && ticket.id.length >= 6 -> "#${ticket.id.takeLast(6)}"
            else -> ""
        }
        binding.tvTicketNumber.text = ticketIdText
        binding.tvSubject.text = ticket.subject ?: ""

        val rawSubcategory = ticket.subcategory ?: ticket.contactReasonCode ?: ""
        binding.tvSubcategory.text = toTitleCase(rawSubcategory)

        val isDraft = ticket.currentStatus.equals("draft", ignoreCase = true)
        val status = ticket.status.orEmpty().lowercase()

        val statusText = when {
            isDraft -> getString(R.string.support_status_draft)
            status == "waiting" || status == "waiting_for_user" -> "Waiting for you"
            status == "resolved" -> "Resolved"
            else -> status.replaceFirstChar { it.uppercase() }
        }
        binding.tvStatus.text = statusText

        val tintColorRes = when {
            isDraft -> R.color.support_help_icon_bg
            status == "waiting" || status == "waiting_for_user" -> R.color.support_status_awaiting_reply_bg
            status == "resolved" -> R.color.support_status_resolved_bg
            else -> R.color.support_open_badge_bg
        }
        binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), tintColorRes)
        )

        val textColorRes = when {
            isDraft -> R.color.colorBgBtn1
            status == "waiting" || status == "waiting_for_user" -> R.color.support_status_awaiting_reply_text
            status == "resolved" -> R.color.support_status_awaiting_reply_text
            else -> R.color.white
        }
        binding.tvStatus.setTextColor(
            ContextCompat.getColor(requireContext(), textColorRes)
        )

        // Relative time updated footer
        val relativeTime = try {
            val timestamp = ticket.updatedAt ?: ticket.createdAt
            val instant = java.time.Instant.parse(timestamp)
            com.humotron.app.util.getTimeAgo(instant.toEpochMilli())
        } catch (e: Exception) {
            "just now"
        }
        val createdDate = utcOffsetToLocalTime(ticket.createdAt, "MMM dd, yyyy")

        binding.tvFooter.text = getString(R.string.support_detail_footer_format, createdDate, relativeTime)

        // Hide input area if ticket is resolved
        val isResolved = status == "resolved"
        binding.layoutInput.visibility = if (isResolved) View.GONE else View.VISIBLE
    }

    private fun setupRecyclerViews() {
        // Chat messages Adapter
        messageAdapter = TicketMessageAdapter { attachment ->
            openAttachmentUrl(attachment)
        }
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = messageAdapter

        // Selected picked attachments preview Adapter
        selectedAttachmentAdapter = SelectedAttachmentAdapter { uri ->
            selectedImages.remove(uri)
            updateSelectedAttachmentsUI()
        }
        binding.rvSelectedAttachments.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvSelectedAttachments.adapter = selectedAttachmentAdapter
    }

    private fun setupInputHandlers(ticketId: String) {
        binding.btnAttach.setOnClickListener {
            if (selectedImages.size < 5) {
                pickImageLauncher.launch("image/*")
            } else {
                requireContext().showToast(getString(R.string.support_chat_max_files))
            }
        }

        binding.btnSend.setOnClickListener {
            val body = binding.etReply.text?.toString()?.trim().orEmpty()
            if (body.isEmpty() && selectedImages.isEmpty()) {
                return@setOnClickListener
            }

            // Hide keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etReply.windowToken, 0)

            // Convert images to multipart
            val attachmentsList = selectedImages.mapNotNull { uri ->
                uriToMultipartBodyPart(uri)
            }

            // Send reply to API
            viewModel.replyTicket(ticketId, body, attachmentsList)
        }

        binding.etReply.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        updateSendButtonState()
    }

    private fun setupObservers(ticketId: String) {
        // Observe ticket detail response
        viewModel.ticketDetailData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (messageAdapter.itemCount == 0) {
                        showProgress()
                    }
                }
                Status.SUCCESS -> {
                    hideProgress()
                    val responseTicket = resource.data?.data?.ticket
                    if (responseTicket != null) {
                        setupSummaryCard(responseTicket)
                        updateMessageFeed(responseTicket)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    requireContext().showToast(errorMsg)
                }
            }
        }

        // Observe reply submit response
        viewModel.replyTicketData.observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe
            when (resource.status) {
                Status.LOADING -> {
                    binding.btnSend.isEnabled = false
                    binding.btnSend.alpha = 0.4f
                    binding.btnAttach.isEnabled = false
                }
                Status.SUCCESS -> {
                    binding.btnAttach.isEnabled = true
                    binding.etReply.text?.clear()
                    selectedImages.clear()
                    updateSelectedAttachmentsUI()
                    // Reload message list
                    viewModel.fetchTicketDetail(ticketId)
                    viewModel.clearReplyTicketData()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.btnAttach.isEnabled = true
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    requireContext().showToast(errorMsg)
                    viewModel.clearReplyTicketData()
                    updateSendButtonState()
                }
            }
        }
    }

    private fun setupKeyboardManagement() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            
            val paddingBottom = maxOf(systemInsets.bottom, imeInsets.bottom)
            view.updatePadding(bottom = paddingBottom)
            
            if (isKeyboardVisible) {
                binding.scrollContainer.postDelayed({
                    val child = binding.scrollContainer.getChildAt(0)
                    if (child != null) {
                        binding.scrollContainer.scrollTo(0, child.height)
                    }
                }, 100)
            }
            
            insets
        }
    }

    private fun updateMessageFeed(ticket: TicketDetail) {
        val chatMessages = mutableListOf<TicketMessage>()

        // Synthesize description message as the first item
        chatMessages.add(
            TicketMessage(
                id = ticket.id,
                senderType = "user",
                senderId = ticket.userId,
                senderName = ticket.userName ?: getString(R.string.support_chat_sender_you),
                body = ticket.description,
                isRead = true,
                createdAt = ticket.createdAt,
                updatedAt = ticket.updatedAt,
                attachments = ticket.attachments
            )
        )

        // Add the rest of the ticket conversation messages
        ticket.messages?.let {
            chatMessages.addAll(it)
        }

        messageAdapter.submitList(chatMessages) {
            if (chatMessages.isNotEmpty()) {
                binding.scrollContainer.post {
                    val child = binding.scrollContainer.getChildAt(0)
                    if (child != null) {
                        binding.scrollContainer.scrollTo(0, child.height)
                    }
                }
            }
        }
    }

    private fun updateSelectedAttachmentsUI() {
        if (selectedImages.isEmpty()) {
            binding.rvSelectedAttachments.visibility = View.GONE
        } else {
            binding.rvSelectedAttachments.visibility = View.VISIBLE
            selectedAttachmentAdapter.submitList(selectedImages.toList())
        }
        updateSendButtonState()
    }

    private fun updateSendButtonState() {
        val hasText = binding.etReply.text?.toString()?.trim().orEmpty().isNotEmpty()
        val hasAttachments = selectedImages.isNotEmpty()
        val shouldEnable = hasText || hasAttachments
        binding.btnSend.isEnabled = shouldEnable
        binding.btnSend.alpha = if (shouldEnable) 1.0f else 0.4f
    }

    private fun openAttachmentUrl(attachment: TicketAttachment) {
        val url = attachment.url
        if (!url.isNullOrEmpty()) {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                requireContext().showToast(getString(R.string.support_chat_cannot_open_attachment))
            }
        }
    }

    private fun uriToMultipartBodyPart(uri: Uri): MultipartBody.Part? {
        if (uri.scheme == "http" || uri.scheme == "https") {
            return null
        }
        val contentResolver = requireContext().contentResolver
        var fileName = "attachment_${System.currentTimeMillis()}"
        val mimeType = contentResolver.getType(uri) ?: "image/*"

        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
        } else if (uri.scheme == "file") {
            uri.path?.let { path ->
                fileName = File(path).name
            }
        }

        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull(), 0, bytes.size)
            MultipartBody.Part.createFormData("attachments", fileName, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun toTitleCase(input: String): String {
        if (input.isBlank()) return ""
        return input.replace('_', ' ').replace('-', ' ').split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
