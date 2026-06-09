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

@AndroidEntryPoint
class ContactSupportFragment : BaseFragment(R.layout.fragment_contact_support) {

    private lateinit var binding: FragmentContactSupportBinding
    private val viewModel: SupportViewModel by activityViewModels()
    private lateinit var categoryAdapter: ContactCategoryAdapter
    private var originalSoftInputMode: Int = android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
    private var currentStep: Int = 1

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
        if (currentStep == 2) {
            currentStep = 1
            animateHeaderVisibility(true)
            binding.scrollViewStep2.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
            binding.flStep1Buttons.visibility = View.VISIBLE
            binding.llStep2Buttons.visibility = View.GONE
            binding.bottomBar.visibility = View.VISIBLE

            // Transition Step Indicator back to Step 1
            binding.circle1.text = "1"
            binding.circle1.setBackgroundResource(R.drawable.bg_contact_step_active)
            binding.circle1.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
            binding.tvLabel1.setTextColor(resources.getColor(R.color.colorBgBtn1, null))

            binding.circle2.text = "2"
            binding.circle2.setBackgroundResource(R.drawable.bg_contact_radio_inactive)
            binding.circle2.setTextColor(resources.getColor(R.color.white30, null))
            binding.tvLabel2.setTextColor(resources.getColor(R.color.white30, null))
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
            }
        }

        // Problem selector click
        val problems = arrayOf(
            "Bluetooth connection failure",
            "Data syncing delay",
            "Inaccurate biometric measurements",
            "App crash or freeze",
            "Charging or battery drain",
            "Other issue"
        )
        binding.rlProblemSelector.setOnClickListener {
            showModernDropdown(it, problems) { selected ->
                binding.tvSelectedProblem.text = selected
                binding.tvSelectedProblem.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
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

        val versions = arrayOf(appVersionString, "Not Specified")
        binding.rlAppVersionSelector.setOnClickListener {
            showModernDropdown(it, versions) { selected ->
                binding.tvSelectedAppVersion.text = selected
            }
        }

        // Bind buttons for Step 2
        binding.btnBackStep2.setOnClickListener {
            handleBackAction()
        }

        binding.btnContinueStep2.setOnClickListener {
            requireContext().showToast("Support ticket successfully submitted!")
            findNavController().popBackStack()
        }
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
            when (resource.status) {
                Status.LOADING -> {
                    binding.btnContinue.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.visibility = View.VISIBLE
                    val response = resource.data
                    if (response?.status == "success") {
                        // Hide Step 1 UI, Show Step 2 UI
                        binding.scrollView.visibility = View.GONE
                        binding.scrollViewStep2.visibility = View.VISIBLE
                        binding.flStep1Buttons.visibility = View.GONE
                        binding.llStep2Buttons.visibility = View.VISIBLE
                        binding.bottomBar.visibility = View.VISIBLE

                        // Transition Step Indicator to Step 2
                        binding.circle1.text = ""
                        binding.circle1.setBackgroundResource(R.drawable.bg_contact_step_completed)
                        binding.circle1.setTextColor(resources.getColor(R.color.white30, null))
                        binding.tvLabel1.setTextColor(resources.getColor(R.color.white30, null))

                        binding.circle2.text = "2"
                        binding.circle2.setBackgroundResource(R.drawable.bg_contact_step_active)
                        binding.circle2.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                        binding.tvLabel2.setTextColor(resources.getColor(R.color.colorBgBtn1, null))
                         
                        animateHeaderVisibility(false)
                        currentStep = 2
                    } else {
                        val msg = response?.message ?: "Failed to save support ticket"
                        requireContext().showToast(msg)
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.visibility = View.VISIBLE
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    requireContext().showToast(errorMsg)
                }
                Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnContinue.visibility = View.VISIBLE
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
                requireContext().showToast("Please select a category.")
                return@setOnClickListener
            }

            if (message.isNullOrEmpty()) {
                requireContext().showToast("Please describe your issue.")
                return@setOnClickListener
            }

            val selectedCategory = categoryAdapter.currentList.find { it.key == selectedKey }
            val categoryId = selectedCategory?.id ?: ""
            val subject = selectedCategory?.label ?: "Support Ticket"

            var appVersion = "1.0.1"
            try {
                val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                appVersion = pInfo.versionName ?: "1.0.1"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val deviceMetaJson = "{\"deviceId\":\"\",\"fw\":\"\",\"mac\":\"\",\"sn\":\"\",\"lpm\":false,\"desc\":\"\"}"

            // Hide keyboard before calling API
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etDescription.windowToken, 0)

            viewModel.saveTicket(
                category = categoryId,
                contactReasonCode = selectedKey,
                subject = subject,
                description = message,
                currentScreen = "1",
                source = "app",
                osPlatform = "android",
                appVersion = appVersion,
                deviceMetaSnapshot = deviceMetaJson
            )
        }
    }
}
