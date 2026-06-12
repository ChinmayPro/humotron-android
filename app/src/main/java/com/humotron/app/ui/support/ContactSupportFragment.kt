package com.humotron.app.ui.support

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentContactSupportBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.humotron.app.domain.modal.response.SupportCategory
import com.humotron.app.domain.modal.response.CommonResponse
import com.humotron.app.ui.support.adapter.ContactCategoryAdapter
import com.humotron.app.util.showToast
import androidx.activity.OnBackPressedCallback
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.ui.support.adapter.ContactAttachmentAdapter
import com.humotron.app.ui.support.adapter.ReviewAttachmentAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.provider.OpenableColumns
import java.io.File

@AndroidEntryPoint
class ContactSupportFragment : BaseFragment(R.layout.fragment_contact_support) {

    private lateinit var binding: FragmentContactSupportBinding
    private val viewModel: SupportViewModel by activityViewModels()
    private lateinit var categoryAdapter: ContactCategoryAdapter
    private lateinit var attachmentAdapter: ContactAttachmentAdapter
    private lateinit var reviewAttachmentAdapter: ReviewAttachmentAdapter
    private var originalSoftInputMode: Int = android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
    private var currentStep: Int = 1
    private var savedTicketId: String = ""
    private var savedTicketNumber: String = ""
    private val selectedImages = mutableListOf<android.net.Uri>()

    // ActivityResultLauncher to pick images
    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            if (selectedImages.size < 5) {
                selectedImages.add(it)
                updateSelectedAttachmentsUI()
            } else {
                requireContext().showToast("You can only add up to 5 files.")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        originalSoftInputMode = requireActivity().window.attributes.softInputMode
        requireActivity().window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.setSoftInputMode(originalSoftInputMode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentContactSupportBinding.bind(view)

        setupHeader()
        setupKeyboardFocusClear()
        setupRecyclerView()
        setupObservers()
        setupDescriptionWatcher()
        setupContinueButton()
        setupStep2Selectors()
        setupStep5Listeners()

        // Handle physical/system back navigation
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackAction()
            }
        })

        // Check if categories are already loaded in ViewModel, if not fetch them
        val homeData = viewModel.supportHomeData.value
        if (homeData != null && homeData.status == Status.SUCCESS && !homeData.data?.data?.categories.isNullOrEmpty()) {
            displayCategories(homeData.data.data.categories!!)
        } else {
            viewModel.fetchSupportHomeData()
        }
    }

    private fun setupHeader() {
        binding.header.title.setText(R.string.contact_support)
        binding.header.ivBack.setOnClickListener {
            handleBackAction()
        }
    }

    private fun handleBackAction() {
        if (currentStep == 5) {
            findNavController().popBackStack()
        } else if (currentStep == 4) {
            navigateToStep(3)
        } else if (currentStep == 3) {
            navigateToStep(2)
        } else if (currentStep == 2) {
            navigateToStep(1)
        } else {
            findNavController().popBackStack()
        }
    }

    private fun animateHeaderVisibility(show: Boolean) {
        val transition = androidx.transition.TransitionSet().apply {
            addTransition(androidx.transition.Fade())
            addTransition(androidx.transition.ChangeBounds())
            duration = 300
        }
        androidx.transition.TransitionManager.beginDelayedTransition(binding.llTopContainer, transition)
        if (show) {
            binding.tvSupportTitle.visibility = View.VISIBLE
            binding.tvSupportSubtitle.visibility = View.VISIBLE
        } else {
            binding.tvSupportTitle.visibility = View.GONE
            binding.tvSupportSubtitle.visibility = View.GONE
        }
    }

    private fun setupStep2Selectors() {
        val deviceIcons = mapOf(
            "Smart Ring" to R.drawable.ic_smart_ring,
            "Wrist Band" to R.drawable.ic_wrist_band,
            "Weight Scale" to R.drawable.ic_weight_scale,
            "Smart Cuff" to R.drawable.ic_smart_cuff
        )

        // Device selector click
        val devices = arrayOf("Smart Ring", "Wrist Band", "Smart Cuff", "Weight Scale", "None")
        binding.rlDeviceSelector.setOnClickListener {
            showModernDropdown(it, devices, deviceIcons) { selected ->
                binding.tvSelectedDevice.text = selected
                binding.tvSelectedDevice.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
                val iconRes = deviceIcons[selected]
                if (iconRes != null && iconRes != 0) {
                    binding.ivSelectedDeviceIcon.setImageResource(iconRes)
                    binding.ivSelectedDeviceIcon.visibility = View.VISIBLE
                } else {
                    binding.ivSelectedDeviceIcon.visibility = View.GONE
                }
                validateStep2Fields()
            }
        }

        // Problem selector click
        val problems = arrayOf(
            "Battery, sleep tracking, syncing, sizing, and measurements.",
            "Heart rate, blood pressure, tracking, and connectivity help.",
            "Blood pressure, ECG, cuff placement, and measurement guidance.",
            "Weight, body composition, calibration, and measurement help."
        )
        binding.rlProblemSelector.setOnClickListener {
            showModernDropdown(it, problems) { selected ->
                binding.tvSelectedProblem.text = selected
                binding.tvSelectedProblem.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
                validateStep2Fields()
            }
        }

        // App version selector
        var appVersionString = "1.0.1 (55)"
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = pInfo.versionName ?: "1.0.1"
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
            appVersionString = "$versionName ($versionCode)"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.tvSelectedAppVersion.text = appVersionString

        val versions = arrayOf(appVersionString, getString(R.string.contact_clear))
        binding.rlAppVersionSelector.setOnClickListener {
            showModernDropdown(it, versions) { selected ->
                if (selected == getString(R.string.contact_clear)) {
                    binding.tvSelectedAppVersion.text = getString(R.string.contact_select_app_version)
                    binding.tvSelectedAppVersion.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white50))
                } else {
                    binding.tvSelectedAppVersion.text = selected
                    binding.tvSelectedAppVersion.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }

        // Bind buttons for Step 2
        binding.btnBackStep2.setOnClickListener {
            handleBackAction()
        }

        binding.btnContinueStep2.setOnClickListener {
            val selectedKey = categoryAdapter.getSelectedCategoryKey()
            val message = binding.etDescription.text?.toString()?.trim() ?: ""

            if (selectedKey.isNullOrEmpty()) {
                requireContext().showToast(getString(R.string.contact_please_select_category))
                return@setOnClickListener
            }

            val selectedCategory = categoryAdapter.currentList.find { it.key == selectedKey }
            val categoryId = selectedCategory?.id ?: ""
            val subject = selectedCategory?.label ?: getString(R.string.contact_ticket_default_subject)

            val selectedAppVersion = binding.tvSelectedAppVersion.text?.toString() ?: ""
            val appVersion = if (selectedAppVersion == getString(R.string.contact_select_app_version)) "" else selectedAppVersion

            val selectedDeviceText = binding.tvSelectedDevice.text?.toString() ?: ""
            val deviceName = if (selectedDeviceText == getString(R.string.contact_select_device)) "" else selectedDeviceText
            val deviceType = "" // device_type expects ObjectId, will be set when connected devices API is integrated

            // Build Step 2 final description including the "When did this start" duration selection
            val selectedDuration = when (binding.rgStartDuration.checkedRadioButtonId) {
                R.id.rbToday -> "Today"
                R.id.rbLast7Days -> "In the last 7 days"
                R.id.rbLast30Days -> "In the last 30 days"
                R.id.rbMoreThan30Days -> "More than 30 days ago"
                R.id.rbNotSure -> "Not sure"
                else -> "Today"
            }
            val deviceInfo = if (deviceName.isNotEmpty()) "\n\nDevice: $deviceName" else ""
            val finalDescription = "$message\n\nWhen did this start: $selectedDuration$deviceInfo"

            // Get selected problem as subcategory
            val selectedProblem = binding.tvSelectedProblem.text?.toString() ?: ""
            val subcategory = if (selectedProblem == getString(R.string.contact_select_issue)) "" else selectedProblem

            viewModel.saveTicket(
                category = categoryId,
                subcategory = subcategory,
                contactReasonCode = selectedKey,
                subject = subject,
                description = finalDescription,
                currentScreen = "2",
                source = "app",
                osPlatform = "android",
                appVersion = appVersion,
                deviceType = deviceType,
                region = "",
                ticketId = savedTicketId
            )
        }

        // Bind buttons for Step 3
        binding.btnBackStep3.setOnClickListener {
            handleBackAction()
        }

        binding.llUploadContainer.setOnClickListener {
            if (selectedImages.size < 5) {
                pickImageLauncher.launch("image/*")
            } else {
                requireContext().showToast("You can only add up to 5 files.")
            }
        }

        binding.btnContinueStep3.setOnClickListener {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etAdditionalNotes.windowToken, 0)

            val selectedKey = categoryAdapter.getSelectedCategoryKey() ?: ""
            val selectedCategory = categoryAdapter.currentList.find { it.key == selectedKey }
            val categoryId = selectedCategory?.id ?: ""
            val subject = selectedCategory?.label ?: getString(R.string.contact_ticket_default_subject)

            val selectedAppVersion = binding.tvSelectedAppVersion.text?.toString() ?: ""
            val appVersion = if (selectedAppVersion == getString(R.string.contact_select_app_version)) "" else selectedAppVersion

            val selectedDeviceText = binding.tvSelectedDevice.text?.toString() ?: ""
            val deviceName = if (selectedDeviceText == getString(R.string.contact_select_device)) "" else selectedDeviceText
            val deviceType = ""

            val additionalNotes = binding.etAdditionalNotes.text?.toString()?.trim() ?: ""

            val attachmentsList = selectedImages.mapNotNull { uri ->
                uriToMultipartBodyPart(uri)
            }

            val selectedProblem = binding.tvSelectedProblem.text?.toString() ?: ""
            val subcategory = if (selectedProblem == getString(R.string.contact_select_issue)) "" else selectedProblem

            val message = binding.etDescription.text?.toString()?.trim() ?: ""
            val selectedDuration = when (binding.rgStartDuration.checkedRadioButtonId) {
                R.id.rbToday -> "Today"
                R.id.rbLast7Days -> "In the last 7 days"
                R.id.rbLast30Days -> "In the last 30 days"
                R.id.rbMoreThan30Days -> "More than 30 days ago"
                R.id.rbNotSure -> "Not sure"
                else -> "Today"
            }
            val deviceInfo = if (deviceName.isNotEmpty()) "\n\nDevice: $deviceName" else ""
            val finalDescription = "$message\n\nWhen did this start: $selectedDuration$deviceInfo"
            val combinedDescription = if (additionalNotes.isNotEmpty()) "$finalDescription\n\nAdditional Notes: $additionalNotes" else finalDescription

            viewModel.saveTicket(
                category = categoryId,
                subcategory = subcategory,
                contactReasonCode = selectedKey,
                subject = subject,
                description = combinedDescription,
                currentScreen = "3",
                source = "app",
                osPlatform = "android",
                appVersion = appVersion,
                deviceType = deviceType,
                region = "",
                ticketId = savedTicketId,
                attachments = attachmentsList
            )
        }

        binding.tvEditSummary.setOnClickListener {
            navigateToStep(2)
        }

        binding.tvEditAttachments.setOnClickListener {
            navigateToStep(3)
        }

        binding.tvEditAdditionalNotes.setOnClickListener {
            navigateToStep(3)
        }

        binding.tvEditAdditionalDetails.setOnClickListener {
            navigateToStep(1)
        }

        binding.btnSubmitRequest.setOnClickListener {
            val selectedKey = categoryAdapter.getSelectedCategoryKey() ?: ""
            val selectedCategory = categoryAdapter.currentList.find { it.key == selectedKey }
            val categoryId = selectedCategory?.id ?: ""
            val subject = selectedCategory?.label ?: getString(R.string.contact_ticket_default_subject)

            val selectedAppVersion = binding.tvSelectedAppVersion.text?.toString() ?: ""
            val appVersion = if (selectedAppVersion == getString(R.string.contact_select_app_version)) "" else selectedAppVersion

            val selectedDeviceText = binding.tvSelectedDevice.text?.toString() ?: ""
            val deviceName = if (selectedDeviceText == getString(R.string.contact_select_device)) "" else selectedDeviceText
            val deviceType = ""

            val additionalNotes = binding.etAdditionalNotes.text?.toString()?.trim() ?: ""

            // We don't necessarily need to upload attachments again at step 4, but the API accepts it.
            // If the user wants to upload again, we can send the attachmentsList. To be safe, we send it.
            val attachmentsList = selectedImages.mapNotNull { uri ->
                uriToMultipartBodyPart(uri)
            }

            val selectedProblem = binding.tvSelectedProblem.text?.toString() ?: ""
            val subcategory = if (selectedProblem == getString(R.string.contact_select_issue)) "" else selectedProblem

            val message = binding.etDescription.text?.toString()?.trim() ?: ""
            val selectedDuration = when (binding.rgStartDuration.checkedRadioButtonId) {
                R.id.rbToday -> "Today"
                R.id.rbLast7Days -> "In the last 7 days"
                R.id.rbLast30Days -> "In the last 30 days"
                R.id.rbMoreThan30Days -> "More than 30 days ago"
                R.id.rbNotSure -> "Not sure"
                else -> "Today"
            }
            val deviceInfo = if (deviceName.isNotEmpty()) "\n\nDevice: $deviceName" else ""
            val finalDescription = "$message\n\nWhen did this start: $selectedDuration$deviceInfo"
            val combinedDescription = if (additionalNotes.isNotEmpty()) "$finalDescription\n\nAdditional Notes: $additionalNotes" else finalDescription

            viewModel.saveTicket(
                category = categoryId,
                subcategory = subcategory,
                contactReasonCode = selectedKey,
                subject = subject,
                description = combinedDescription,
                currentScreen = "4",
                source = "app",
                osPlatform = "android",
                appVersion = appVersion,
                deviceType = deviceType,
                region = "",
                ticketId = savedTicketId,
                attachments = attachmentsList
            )
        }

        // Setup character counter for Step 3 Additional Notes
        binding.etAdditionalNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.tvAdditionalNotesCharCounter.text = "$length/1000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        validateStep2Fields()
    }

    private fun setupStep5Listeners() {
        binding.btnBackToSupportHome.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.llCopyTicketId.setOnClickListener {
            val ticketId = binding.tvTicketIdValue.text.toString().trim()
            if (ticketId.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Ticket ID", ticketId)
                clipboard.setPrimaryClip(clip)
                requireContext().showToast("Ticket ID copied to clipboard")
            }
        }
    }

    private fun validateStep2Fields() {
        val isProblemSelected = binding.tvSelectedProblem.text != getString(R.string.contact_select_issue)
        binding.flContinueStep2Container.visibility = if (isProblemSelected) View.VISIBLE else View.GONE
    }

    private fun showModernDropdown(
        anchorView: View,
        items: Array<String>,
        icons: Map<String, Int> = emptyMap(),
        onItemSelected: (String) -> Unit
    ) {
        val context = anchorView.context
        val listPopupWindow = androidx.appcompat.widget.ListPopupWindow(context)

        listPopupWindow.anchorView = anchorView

        val adapter = object : android.widget.ArrayAdapter<String>(context, R.layout.item_dropdown_popup, R.id.tvDropdownText, items) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text = items[position]
                val imageView = view.findViewById<android.widget.ImageView>(R.id.ivDropdownIcon)
                val iconRes = icons[text]
                if (iconRes != null && iconRes != 0) {
                    imageView.setImageResource(iconRes)
                    imageView.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.GONE
                }
                return view
            }
        }

        listPopupWindow.setAdapter(adapter)

        // Set custom background drawable with rounded corners and dark color
        listPopupWindow.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_popup_menu))

        // Match the width of the anchor selector
        listPopupWindow.width = anchorView.width

        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            onItemSelected(items[position])
            listPopupWindow.dismiss()
        }

        listPopupWindow.isModal = true
        listPopupWindow.show()
    }



    private fun setupKeyboardFocusClear() {
        binding.root.setOnApplyWindowInsetsListener { view, insets ->
            val localInsets = view.onApplyWindowInsets(insets)
            // Zero out top padding to prevent double status bar padding from fitsSystemWindows
            view.setPadding(view.paddingLeft, 0, view.paddingRight, view.paddingBottom)

            val isKeyboardVisible = WindowInsetsCompat.toWindowInsetsCompat(insets).isVisible(WindowInsetsCompat.Type.ime())
            if (!isKeyboardVisible && binding.etDescription.hasFocus()) {
                binding.etDescription.clearFocus()
            }
            localInsets
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = ContactCategoryAdapter { _ ->
            // Selection is handled within the adapter
        }
        binding.rvCategories.adapter = categoryAdapter

        attachmentAdapter = ContactAttachmentAdapter { uri ->
            selectedImages.remove(uri)
            updateSelectedAttachmentsUI()
        }
        binding.rvAttachments.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvAttachments.adapter = attachmentAdapter

        reviewAttachmentAdapter = ReviewAttachmentAdapter()
        binding.rvReviewAttachments.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvReviewAttachments.adapter = reviewAttachmentAdapter
    }

    private fun updateSelectedAttachmentsUI() {
        if (selectedImages.isEmpty()) {
            binding.rvAttachments.visibility = View.GONE
            binding.llReviewAttachmentsContainer.visibility = View.GONE
        } else {
            binding.rvAttachments.visibility = View.VISIBLE
            binding.llReviewAttachmentsContainer.visibility = View.VISIBLE
            attachmentAdapter.submitList(selectedImages.toList())
            reviewAttachmentAdapter.submitList(selectedImages.toList())
            binding.tvReviewAttachmentsTitle.text = "Attachments\n(${selectedImages.size})"
        }
    }

    private fun setupObservers() {
        viewModel.supportHomeData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    val response = resource.data
                    if (response?.status == "success" && response.data?.categories != null) {
                        displayCategories(response.data.categories)
                    } else {
                        val msg = response?.message ?: getString(R.string.support_failed_get_details)
                        requireContext().showToast(msg)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    requireContext().showToast(errorMsg)
                }
                Status.EXCEPTION -> {
                    hideProgress()
                    val exceptionMsg = resource.error?.errorMessage ?: getString(R.string.support_exception_occurred)
                    requireContext().showToast(exceptionMsg)
                }
            }
        }

        viewModel.saveTicketData.observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe
            when (resource.status) {
                Status.LOADING -> {
                    if (currentStep == 4) {
                        binding.btnSubmitRequest.visibility = View.INVISIBLE
                        binding.progressBarStep4.visibility = View.VISIBLE
                    } else if (currentStep == 3) {
                        binding.btnContinueStep3.visibility = View.INVISIBLE
                        binding.progressBarStep3.visibility = View.VISIBLE
                        binding.btnBackStep3.isEnabled = false
                    } else if (currentStep == 2) {
                        binding.btnContinueStep2.visibility = View.INVISIBLE
                        binding.progressBarStep2.visibility = View.VISIBLE
                        binding.btnBackStep2.isEnabled = false
                    } else {
                        binding.btnContinue.visibility = View.INVISIBLE
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.visibility = View.VISIBLE
                    binding.progressBarStep2.visibility = View.GONE
                    binding.btnContinueStep2.visibility = View.VISIBLE
                    binding.btnBackStep2.isEnabled = true
                    binding.progressBarStep3.visibility = View.GONE
                    binding.btnContinueStep3.visibility = View.VISIBLE
                    binding.btnBackStep3.isEnabled = true
                    binding.progressBarStep4.visibility = View.GONE
                    binding.btnSubmitRequest.visibility = View.VISIBLE
                    val response = resource.data
                    if (response?.status == "success") {
                        if (currentStep == 4) {
                            val ticketNumber = response.data?.ticket?.ticketNumber
                            if (!ticketNumber.isNullOrEmpty()) {
                                binding.tvTicketIdValue.text = ticketNumber
                            } else if (savedTicketNumber.isNotEmpty()) {
                                binding.tvTicketIdValue.text = savedTicketNumber
                            } else {
                                binding.tvTicketIdValue.text = "TKT-${System.currentTimeMillis().toString().takeLast(5)}"
                            }
                            navigateToStep(5)
                        } else if (currentStep == 3) {
                            val ticket = response.data?.ticket
                            val ticketNum = ticket?.ticketNumber ?: ""
                            if (ticketNum.isNotEmpty()) {
                                savedTicketNumber = ticketNum
                            }
                            val tId = ticket?.id ?: ""
                            if (tId.isNotEmpty()) {
                                savedTicketId = tId
                            }
                            navigateToStep(4)
                        } else if (currentStep == 2) {
                            val ticket = response.data?.ticket
                            val ticketNum = ticket?.ticketNumber ?: ""
                            if (ticketNum.isNotEmpty()) {
                                savedTicketNumber = ticketNum
                            }
                            val tId = ticket?.id ?: ""
                            if (tId.isNotEmpty()) {
                                savedTicketId = tId
                            }
                            navigateToStep(3)
                        } else {
                            // Extract ticket_id from Step 1 response for Step 2
                            val ticket = response.data?.ticket
                            savedTicketId = ticket?.id ?: ""
                            savedTicketNumber = ticket?.ticketNumber ?: ""
                            navigateToStep(2)
                        }
                    } else {
                        val msg = response?.message ?: "Failed to save support ticket"
                        requireContext().showToast(msg)
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.visibility = View.VISIBLE
                    binding.progressBarStep2.visibility = View.GONE
                    binding.btnContinueStep2.visibility = View.VISIBLE
                    binding.btnBackStep2.isEnabled = true
                    binding.progressBarStep3.visibility = View.GONE
                    binding.btnContinueStep3.visibility = View.VISIBLE
                    binding.btnBackStep3.isEnabled = true
                    binding.progressBarStep4.visibility = View.GONE
                    binding.btnSubmitRequest.visibility = View.VISIBLE
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    requireContext().showToast(errorMsg)
                }
                Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.visibility = View.VISIBLE
                    binding.progressBarStep2.visibility = View.GONE
                    binding.btnContinueStep2.visibility = View.VISIBLE
                    binding.btnBackStep2.isEnabled = true
                    binding.progressBarStep4.visibility = View.GONE
                    binding.btnSubmitRequest.visibility = View.VISIBLE
                    val exceptionMsg = resource.error?.errorMessage ?: getString(R.string.support_exception_occurred)
                    requireContext().showToast(exceptionMsg)
                }
            }
        }
    }

    private fun displayCategories(categories: List<SupportCategory>) {
        categoryAdapter.submitList(categories)
        if (categories.isNotEmpty() && categoryAdapter.getSelectedCategoryKey() == null) {
            categoryAdapter.setSelectedCategoryKey(categories[0].key)
        }
    }

    private fun setupDescriptionWatcher() {
        binding.bottomBar.visibility = View.GONE

        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.tvCharCounter.text = "$length/1000"
                binding.bottomBar.visibility = if (s?.toString()?.trim().isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            val selectedKey = categoryAdapter.getSelectedCategoryKey()
            val message = binding.etDescription.text?.toString()?.trim()

            if (selectedKey.isNullOrEmpty()) {
                requireContext().showToast(getString(R.string.contact_please_select_category))
                return@setOnClickListener
            }

            if (message.isNullOrEmpty()) {
                requireContext().showToast(getString(R.string.contact_please_describe_issue))
                return@setOnClickListener
            }

            val selectedCategory = categoryAdapter.currentList.find { it.key == selectedKey }
            val categoryId = selectedCategory?.id ?: ""
            val subject = selectedCategory?.label ?: getString(R.string.contact_ticket_default_subject)

            var appVersion = "1.0.1"
            try {
                val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                appVersion = pInfo.versionName ?: "1.0.1"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Hide keyboard before calling API
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etDescription.windowToken, 0)

            viewModel.saveTicket(
                category = categoryId,
                subcategory = "",
                contactReasonCode = selectedKey,
                subject = subject,
                description = message,
                currentScreen = "1",
                source = "app",
                osPlatform = "android",
                appVersion = appVersion,
                deviceType = "",
                region = "",
                ticketId = ""
            )
        }
    }

    private fun uriToMultipartBodyPart(uri: android.net.Uri): MultipartBody.Part? {
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

    private fun navigateToStep(step: Int) {
        // Hide all steps first
        binding.scrollView.visibility = View.GONE
        binding.scrollViewStep2.visibility = View.GONE
        binding.scrollViewStep3.visibility = View.GONE
        binding.scrollViewStep4.visibility = View.GONE
        binding.scrollViewStep5.visibility = View.GONE

        binding.flStep1Buttons.visibility = View.GONE
        binding.llStep2Buttons.visibility = View.GONE
        binding.llStep3Buttons.visibility = View.GONE
        binding.llStep4Buttons.visibility = View.GONE
        binding.flStep5Buttons.visibility = View.GONE

        // Reset all indicators to default inactive/completed
        binding.circle1.text = "1"
        binding.circle1.setBackgroundResource(R.drawable.bg_contact_radio_inactive)
        binding.circle1.setTextColor(resources.getColor(R.color.white30, null))
        binding.tvLabel1.setTextColor(resources.getColor(R.color.white30, null))

        binding.circle2.text = "2"
        binding.circle2.setBackgroundResource(R.drawable.bg_contact_radio_inactive)
        binding.circle2.setTextColor(resources.getColor(R.color.white30, null))
        binding.tvLabel2.setTextColor(resources.getColor(R.color.white30, null))

        binding.circle3.text = "3"
        binding.circle3.setBackgroundResource(R.drawable.bg_contact_radio_inactive)
        binding.circle3.setTextColor(resources.getColor(R.color.white30, null))
        binding.tvLabel3.setTextColor(resources.getColor(R.color.white30, null))

        binding.circle4.text = "4"
        binding.circle4.setBackgroundResource(R.drawable.bg_contact_radio_inactive)
        binding.circle4.setTextColor(resources.getColor(R.color.white30, null))
        binding.tvLabel4.setTextColor(resources.getColor(R.color.white30, null))

        animateHeaderVisibility(step == 1)
        currentStep = step

        when (step) {
            1 -> {
                binding.scrollView.visibility = View.VISIBLE
                binding.flStep1Buttons.visibility = View.VISIBLE
                
                binding.circle1.text = "1"
                binding.circle1.setBackgroundResource(R.drawable.bg_contact_step_active)
                binding.circle1.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                binding.tvLabel1.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
            }
            2 -> {
                binding.scrollViewStep2.visibility = View.VISIBLE
                binding.llStep2Buttons.visibility = View.VISIBLE

                binding.circle1.text = ""
                binding.circle1.setBackgroundResource(R.drawable.bg_contact_step_completed)
                
                binding.circle2.text = "2"
                binding.circle2.setBackgroundResource(R.drawable.bg_contact_step_active)
                binding.circle2.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                binding.tvLabel2.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
            }
            3 -> {
                binding.scrollViewStep3.visibility = View.VISIBLE
                binding.llStep3Buttons.visibility = View.VISIBLE

                binding.circle1.text = ""
                binding.circle1.setBackgroundResource(R.drawable.bg_contact_step_completed)
                binding.circle2.text = ""
                binding.circle2.setBackgroundResource(R.drawable.bg_contact_step_completed)

                binding.circle3.text = "3"
                binding.circle3.setBackgroundResource(R.drawable.bg_contact_step_active)
                binding.circle3.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                binding.tvLabel3.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
            }
            4 -> {
                binding.scrollViewStep4.visibility = View.VISIBLE
                binding.llStep4Buttons.visibility = View.VISIBLE

                binding.circle1.text = ""
                binding.circle1.setBackgroundResource(R.drawable.bg_contact_step_completed)
                binding.circle2.text = ""
                binding.circle2.setBackgroundResource(R.drawable.bg_contact_step_completed)
                binding.circle3.text = ""
                binding.circle3.setBackgroundResource(R.drawable.bg_contact_step_completed)

                binding.circle4.text = "4"
                binding.circle4.setBackgroundResource(R.drawable.bg_contact_step_active)
                binding.circle4.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                binding.tvLabel4.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                
                // Populate Review details
                val selectedKey = categoryAdapter.getSelectedCategoryKey() ?: ""
                val selectedCategory = categoryAdapter.currentList.find { it.key == selectedKey }
                binding.tvReviewCategory.text = selectedCategory?.label ?: ""

                val selectedProblem = binding.tvSelectedProblem.text?.toString() ?: ""
                binding.tvReviewProblem.text = if (selectedProblem == getString(R.string.contact_select_issue)) "" else selectedProblem

                val selectedDuration = when (binding.rgStartDuration.checkedRadioButtonId) {
                    R.id.rbToday -> "Today"
                    R.id.rbLast7Days -> "In the last 7 days"
                    R.id.rbLast30Days -> "In the last 30 days"
                    R.id.rbMoreThan30Days -> "More than 30 days ago"
                    R.id.rbNotSure -> "Not sure"
                    else -> "Today"
                }
                binding.tvReviewStartDuration.text = selectedDuration
                binding.tvReviewAppVersion.text = binding.tvSelectedAppVersion.text

                binding.tvReviewAdditionalDetails.text = binding.etDescription.text?.toString()?.trim() ?: ""

                val additionalNotes = binding.etAdditionalNotes.text?.toString()?.trim() ?: ""
                if (additionalNotes.isNotEmpty()) {
                    binding.tvReviewAdditionalNotes.text = additionalNotes
                    binding.llReviewAdditionalNotesContainer.visibility = View.VISIBLE
                } else {
                    binding.llReviewAdditionalNotesContainer.visibility = View.GONE
                }

                binding.tvReviewAttachmentsTitle.text = "Attachments\n(${selectedImages.size})"
                if (selectedImages.isEmpty()) {
                    binding.llReviewAttachmentsContainer.visibility = View.GONE
                } else {
                    binding.llReviewAttachmentsContainer.visibility = View.VISIBLE
                    reviewAttachmentAdapter.submitList(selectedImages.toList())
                }
            }
            5 -> {
                binding.scrollViewStep5.visibility = View.VISIBLE
                binding.flStep5Buttons.visibility = View.VISIBLE

                val instructionsHtml = "You can view the status of your request in<br/><font color='#BEF362'>Profile &gt; Support &gt; My requests</font>"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    binding.tvStatusInstructions.text = android.text.Html.fromHtml(instructionsHtml, android.text.Html.FROM_HTML_MODE_LEGACY)
                } else {
                    @Suppress("DEPRECATION")
                    binding.tvStatusInstructions.text = android.text.Html.fromHtml(instructionsHtml)
                }

                binding.circle1.text = ""
                binding.circle1.setBackgroundResource(R.drawable.bg_contact_step_completed)
                binding.circle2.text = ""
                binding.circle2.setBackgroundResource(R.drawable.bg_contact_step_completed)
                binding.circle3.text = ""
                binding.circle3.setBackgroundResource(R.drawable.bg_contact_step_completed)

                binding.circle4.text = "4"
                binding.circle4.setBackgroundResource(R.drawable.bg_contact_step_active)
                binding.circle4.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                binding.tvLabel4.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSaveTicketData()
    }
}
