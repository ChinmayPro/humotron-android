package com.humotron.app.ui.profile

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.DialogDeleteSourceConfirmBinding
import com.humotron.app.databinding.DialogPauseSourceConfirmBinding
import com.humotron.app.databinding.FragmentDataSourceDetailBinding
import com.humotron.app.domain.modal.response.DataSourceDetailResponse
import com.humotron.app.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataSourceDetailFragment : BaseFragment(R.layout.fragment_data_source_detail) {

    private lateinit var binding: FragmentDataSourceDetailBinding
    private val args: DataSourceDetailFragmentArgs by navArgs()
    private val viewModel: ProfileViewModel by activityViewModels()

    private var isPausedState: Boolean = false
    private var activeSourceName: String = ""
    private var isProcessingPause: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDataSourceDetailBinding.bind(view)

        setupInsets()

        // Setup initial UI from NavArgs
        setupInitialUi()

        // Observe & fetch live API data for this source
        setupObservers()
        val sourceKey = getApiSourceKey(args.sourceId)
        viewModel.fetchDataSourceDetail(sourceKey)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraBottomPadding = (16 * resources.displayMetrics.density).toInt()
            binding.contentScrollView.updatePadding(bottom = systemBars.bottom + extraBottomPadding)
            insets
        }
    }

    private fun getApiSourceKey(id: String): String {
        val key = id.lowercase()
        return when {
            key.contains("apple") || key.contains("watch") -> "apple_watch"
            key.contains("whoop") -> "whoop"
            key.contains("garmin") -> "garmin"
            key.contains("oura") || key.contains("ring") -> "oura"
            key.contains("polar") -> "polar"
            key.contains("strava") -> "strava"
            key.contains("ultrahuman") -> "ultrahuman"
            key.contains("google") -> "google_health"
            key.contains("env") || key.contains("environment") -> "environment"
            key.contains("calendar") || key.contains("workday") -> "calendar"
            key.contains("report") -> "reports"
            key.contains("deep") || key.contains("scan") -> "deepscan"
            key.contains("lifestyle") -> "lifestyle"
            key.contains("health") || key.contains("history") -> "health_history"
            key.contains("goal") || key.contains("symptom") -> "goals_symptoms"
            else -> id
        }
    }

    private fun setupInitialUi() {
        activeSourceName = args.sourceName
        binding.header.title.text = args.sourceName
        binding.header.title.typeface = resources.getFont(R.font.manrope_bold)
        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvDeviceName.text = args.sourceName
        binding.tvDeviceDesc.text = args.sourceDesc
        binding.ivDeviceIcon.setImageResource(args.sourceIcon)

        applyAccentColor(args.sourceColor)

        val isReports = args.sourceId.lowercase().contains("report")
        if (isReports) {
            binding.tvUseForHeader.text = "USE UPLOADED REPORTS FOR"
            binding.tvPrivacyTitle.text = "Your privacy is our priority"
            binding.tvPrivacyDesc.text = "Reports are encrypted, processed securely, and never shared. Learn more."
            binding.tvYourControlsHeader.text = "PRIVACY & CONTROL"
            binding.tvPauseTitle.text = "Pause report analysis"
            binding.tvPauseDesc.text = "Temporarily stop using uploaded reports"
            binding.tvDeleteTitle.text = "Delete all reports & data"
            binding.tvDeleteDesc.text = "Permanently remove all files and extracted data"

            binding.llManageReportsContainer.visibility = View.VISIBLE
            binding.llViewReportsButton.setOnClickListener {
                findNavController().navigate(R.id.action_fragmentDataSourceDetail_to_fragmentUploadedReports)
            }
        } else {
            binding.tvDeleteDesc.text = "Permanently remove everything from this source"
        }

        isPausedState = args.sourceStatus.equals("Paused", ignoreCase = true)
        updatePauseControlUi(isPausedState)

        binding.llPauseSource.setOnClickListener {
            showPauseConfirmationDialog()
        }

        binding.llDeleteSource.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupObservers() {
        viewModel.getDataSourceDetailLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    showShimmer(false)
                    resource.data?.data?.let { detail ->
                        bindDataSourceDetail(detail)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    showShimmer(false)
                }
                Status.LOADING -> {
                    showShimmer(true)
                }
            }
        }

        viewModel.getUpdateDataSourceUsageLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {}
                Status.ERROR, Status.EXCEPTION -> {
                    resource.error?.errorMessage?.let {
                        ToastUtils.showShort(requireContext(), it)
                    }
                }
                Status.LOADING -> {}
            }
        }

        viewModel.getPauseDataSourceLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    handlePauseApiSuccess()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.flPauseProgressOverlay.visibility = View.GONE
                    isProcessingPause = false
                    resource.error?.errorMessage?.let {
                        ToastUtils.showShort(requireContext(), it)
                    }
                }
                Status.LOADING -> {}
            }
        }

        viewModel.getDeleteDataSourceDataLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    handleDeleteApiSuccess()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.flPauseProgressOverlay.visibility = View.GONE
                    isProcessingPause = false
                    resource.error?.errorMessage?.let {
                        ToastUtils.showShort(requireContext(), it)
                    }
                }
                Status.LOADING -> {}
            }
        }
    }

    private fun showPauseConfirmationDialog() {
        val context = context ?: return
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogPauseSourceConfirmBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        if (isPausedState) {
            dialogBinding.tvDialogTitle.text = "Resume this source?"
            dialogBinding.tvDialogSubTitle.text = "We'll start using it for analysis again."
            dialogBinding.btnConfirm.text = "Resume"
        } else {
            dialogBinding.tvDialogTitle.text = "Pause this source?"
            dialogBinding.tvDialogSubTitle.text = "We'll stop using it for analysis until you resume."
            dialogBinding.btnConfirm.text = "Pause"
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            dialog.dismiss()
            startPauseProcess(!isPausedState)
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog() {
        val context = context ?: return
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogDeleteSourceConfirmBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val sourceName = if (activeSourceName.isNotBlank()) activeSourceName else args.sourceName
        dialogBinding.tvDialogTitle.text = "Delete all data?"
        dialogBinding.tvDialogSubTitle.text = "This permanently removes everything from $sourceName. This can't be undone."

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirmDelete.setOnClickListener {
            dialog.dismiss()
            startDeleteProcess()
        }

        dialog.show()
    }

    private fun startPauseProcess(targetIsPaused: Boolean) {
        isProcessingPause = true

        binding.tvStepperTitle.text = if (targetIsPaused) "Pausing" else "Resuming"

        // Step 1: Active
        binding.tvStep1Badge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_step_circle_active)
        binding.tvStep1Badge.setTextColor(Color.parseColor("#111827"))
        binding.tvStep1Text.text = "Updating preference"
        binding.tvStep1Text.setTextColor(Color.parseColor("#FFFFFF"))

        // Step 2: Inactive
        binding.tvStep2Badge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_step_circle_inactive)
        binding.tvStep2Badge.setTextColor(Color.parseColor("#A0B3AF"))
        binding.tvStep2Text.text = "Confirming"
        binding.tvStep2Text.setTextColor(Color.parseColor("#6F7E7D"))

        // Reset container views, alphas, and scale for smooth entry
        binding.llStepperContainer.alpha = 1f
        binding.llStepperContainer.visibility = View.VISIBLE

        binding.llSuccessContainer.visibility = View.GONE
        binding.llSuccessContainer.alpha = 0f
        binding.llSuccessContainer.scaleX = 0.85f
        binding.llSuccessContainer.scaleY = 0.85f

        // Smooth overlay fade in
        binding.flPauseProgressOverlay.alpha = 0f
        binding.flPauseProgressOverlay.visibility = View.VISIBLE
        binding.flPauseProgressOverlay.animate()
            .alpha(1f)
            .setDuration(250)
            .start()

        val sourceKey = getApiSourceKey(args.sourceId)
        val param = com.humotron.app.domain.modal.param.PauseDataSourceParam(
            isPaused = targetIsPaused,
            includeInAnalysis = true
        )
        viewModel.pauseDataSource(sourceKey, param)
    }

    private fun startDeleteProcess() {
        isProcessingPause = true

        binding.tvStepperTitle.text = "Deleting"

        // Step 1: Active
        binding.tvStep1Badge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_step_circle_active)
        binding.tvStep1Badge.setTextColor(Color.parseColor("#111827"))
        binding.tvStep1Text.text = "Deleting data"
        binding.tvStep1Text.setTextColor(Color.parseColor("#FFFFFF"))

        // Step 2: Inactive
        binding.tvStep2Badge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_step_circle_inactive)
        binding.tvStep2Badge.setTextColor(Color.parseColor("#A0B3AF"))
        binding.tvStep2Text.text = "Confirming"
        binding.tvStep2Text.setTextColor(Color.parseColor("#6F7E7D"))

        // Reset container views, alphas, and scale for smooth entry
        binding.llStepperContainer.alpha = 1f
        binding.llStepperContainer.visibility = View.VISIBLE

        binding.llSuccessContainer.visibility = View.GONE
        binding.llSuccessContainer.alpha = 0f
        binding.llSuccessContainer.scaleX = 0.85f
        binding.llSuccessContainer.scaleY = 0.85f

        // Smooth overlay fade in
        binding.flPauseProgressOverlay.alpha = 0f
        binding.flPauseProgressOverlay.visibility = View.VISIBLE
        binding.flPauseProgressOverlay.animate()
            .alpha(1f)
            .setDuration(250)
            .start()

        val sourceKey = getApiSourceKey(args.sourceId)
        viewModel.deleteDataSourceData(sourceKey)
    }

    private fun handlePauseApiSuccess() {
        val handler = Handler(Looper.getMainLooper())

        // Wait 350ms after API completes so Step 1 is clearly seen
        handler.postDelayed({
            // Step 2 Completed (Lime badge 2 + white text) with subtle scale pulse
            context?.let { ctx ->
                binding.tvStep2Badge.background = ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_active)
            }
            binding.tvStep2Badge.setTextColor(Color.parseColor("#111827"))
            binding.tvStep2Text.setTextColor(Color.parseColor("#FFFFFF"))

            binding.tvStep2Badge.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(120)
                .withEndAction {
                    binding.tvStep2Badge.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()

            // Hold Step 2 completed for 800ms so user can read both completed steps
            handler.postDelayed({
                // Smooth crossfade: Fade out stepper container, fade in + scale up success container
                binding.llStepperContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.llStepperContainer.visibility = View.GONE

                        binding.tvSuccessTitle.text = if (!isPausedState) "Source paused" else "Source resumed"
                        binding.llSuccessContainer.visibility = View.VISIBLE
                        binding.llSuccessContainer.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start()
                    }.start()

                // Hold Success checkmark screen for 1600ms
                handler.postDelayed({
                    // Smooth overlay fade out
                    binding.flPauseProgressOverlay.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction {
                            binding.flPauseProgressOverlay.visibility = View.GONE
                            binding.flPauseProgressOverlay.alpha = 1f
                            isPausedState = !isPausedState
                            updatePauseControlUi(isPausedState)
                            viewModel.fetchDataSources()
                            isProcessingPause = false
                        }.start()
                }, 1600)

            }, 800)

        }, 350)
    }

    private fun handleDeleteApiSuccess() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            context?.let { ctx ->
                binding.tvStep2Badge.background = ContextCompat.getDrawable(ctx, R.drawable.bg_step_circle_active)
            }
            binding.tvStep2Badge.setTextColor(Color.parseColor("#111827"))
            binding.tvStep2Text.setTextColor(Color.parseColor("#FFFFFF"))

            binding.tvStep2Badge.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(120)
                .withEndAction {
                    binding.tvStep2Badge.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }.start()

            handler.postDelayed({
                binding.llStepperContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        binding.llStepperContainer.visibility = View.GONE

                        binding.tvSuccessTitle.text = "Data deleted"
                        binding.llSuccessContainer.visibility = View.VISIBLE
                        binding.llSuccessContainer.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start()
                    }.start()

                handler.postDelayed({
                    binding.flPauseProgressOverlay.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction {
                            binding.flPauseProgressOverlay.visibility = View.GONE
                            binding.flPauseProgressOverlay.alpha = 1f
                            viewModel.fetchDataSources()
                            isProcessingPause = false
                            val sourceName = if (activeSourceName.isNotBlank()) activeSourceName else args.sourceName
                            ToastUtils.showShort(requireContext(), "Deleted all data for $sourceName")
                            findNavController().navigateUp()
                        }.start()
                }, 1400)

            }, 800)

        }, 350)
    }

    private fun updatePauseControlUi(isPaused: Boolean, statusTextOverride: String? = null) {
        val sourceName = if (activeSourceName.isNotBlank()) activeSourceName else args.sourceName
        if (isPaused) {
            binding.tvStatus.text = "Paused"
            binding.vStatusDot.visibility = View.GONE
            binding.tvStatus.setTextColor(Color.parseColor("#A0B3AF"))

            binding.ivPauseIcon.setImageResource(R.drawable.ic_play)
            binding.ivPauseIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#E7A93C"))
            binding.tvPauseTitle.text = "Resume this source"
            binding.tvPauseDesc.text = "Start using $sourceName again"
        } else {
            val rawStatus = statusTextOverride ?: args.sourceStatus
            val statusDisplay = if (rawStatus.isNotBlank() && !rawStatus.contains("scan", ignoreCase = true) && !rawStatus.contains("report", ignoreCase = true)) {
                rawStatus
            } else {
                "Connected"
            }
            binding.tvStatus.text = statusDisplay
            binding.vStatusDot.visibility = View.VISIBLE
            binding.tvStatus.setTextColor(Color.parseColor("#cfdad8"))

            binding.ivPauseIcon.setImageResource(R.drawable.ic_clock)
            binding.ivPauseIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#E7A93C"))
            binding.tvPauseTitle.text = "Pause this source"
            binding.tvPauseDesc.text = "Temporarily stop using $sourceName"
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerView.shimmerView.visibility = View.VISIBLE
            binding.shimmerView.shimmerView.startShimmer()
            binding.contentScrollView.visibility = View.GONE
        } else {
            binding.shimmerView.shimmerView.stopShimmer()
            binding.shimmerView.shimmerView.visibility = View.GONE
            binding.contentScrollView.visibility = View.VISIBLE
        }
    }

    private fun bindDataSourceDetail(detail: DataSourceDetailResponse.Data) {
        // Clear switch listeners during programmatic binding
        binding.switchInsights.setOnCheckedChangeListener(null)
        binding.switchSuggestions.setOnCheckedChangeListener(null)
        binding.switchChat.setOnCheckedChangeListener(null)
        binding.switchDeepDives.setOnCheckedChangeListener(null)

        // Make switches card visible once API data arrives
        binding.llUsedForCard.visibility = View.VISIBLE

        // Name & Description
        detail.name?.let {
            activeSourceName = it
            binding.header.title.text = it
            binding.tvDeviceName.text = it
        }
        detail.description?.let {
            binding.tvDeviceDesc.text = it
        }

        // Icon from API
        val iconRes = getIconRes(detail.icon, detail.sourceKey)
        binding.ivDeviceIcon.setImageResource(iconRes)

        // Accent Color
        detail.accentColor?.let {
            applyAccentColor(it)
        }

        val currentSourceKey = (detail.sourceKey ?: args.sourceId).lowercase()
        val isReports = currentSourceKey.contains("report")
        if (isReports) {
            binding.tvUseForHeader.text = "USE UPLOADED REPORTS FOR"
            binding.tvPrivacyTitle.text = "Your privacy is our priority"
            binding.tvPrivacyDesc.text = "Reports are encrypted, processed securely, and never shared. Learn more."
            binding.tvYourControlsHeader.text = "PRIVACY & CONTROL"
            binding.tvPauseTitle.text = "Pause report analysis"
            binding.tvPauseDesc.text = "Temporarily stop using uploaded reports"
            binding.tvDeleteTitle.text = "Delete all reports & data"
            binding.tvDeleteDesc.text = "Permanently remove all files and extracted data"

            binding.llManageReportsContainer.visibility = View.VISIBLE
            binding.llViewReportsButton.setOnClickListener {
                findNavController().navigate(R.id.action_fragmentDataSourceDetail_to_fragmentUploadedReports)
            }
        }

        // Status & Paused state
        isPausedState = detail.isPaused == true || detail.status.equals("Paused", ignoreCase = true)
        updatePauseControlUi(isPausedState, detail.status)

        // Usage Toggles
        detail.usageToggles?.forEach { toggle ->
            when (toggle.key) {
                "aiInsights" -> {
                    binding.switchInsights.isChecked = toggle.isEnabled == true
                    toggle.label?.takeIf { it.isNotBlank() }?.let { binding.tvInsightsTitle.text = it }
                    if (!toggle.description.isNullOrBlank()) {
                        binding.tvInsightsDesc.text = toggle.description
                        binding.tvInsightsDesc.visibility = View.VISIBLE
                    } else {
                        binding.tvInsightsDesc.visibility = View.GONE
                    }
                }
                "productSuggestions" -> {
                    binding.switchSuggestions.isChecked = toggle.isEnabled == true
                    toggle.label?.takeIf { it.isNotBlank() }?.let { binding.tvSuggestionsTitle.text = it }
                    if (!toggle.description.isNullOrBlank()) {
                        binding.tvSuggestionsDesc.text = toggle.description
                        binding.tvSuggestionsDesc.visibility = View.VISIBLE
                    } else {
                        binding.tvSuggestionsDesc.visibility = View.GONE
                    }
                }
                "aiChat" -> {
                    binding.switchChat.isChecked = toggle.isEnabled == true
                    toggle.label?.takeIf { it.isNotBlank() }?.let { binding.tvChatTitle.text = it }
                    if (!toggle.description.isNullOrBlank()) {
                        binding.tvChatDesc.text = toggle.description
                        binding.tvChatDesc.visibility = View.VISIBLE
                    } else {
                        binding.tvChatDesc.visibility = View.GONE
                    }
                }
                "deepDives" -> {
                    binding.clDeepDives.visibility = View.VISIBLE
                    binding.divDeepDives.visibility = View.VISIBLE
                    binding.switchDeepDives.isChecked = toggle.isEnabled == true
                    toggle.label?.takeIf { it.isNotBlank() }?.let { binding.tvDeepDivesTitle.text = it }
                    if (!toggle.description.isNullOrBlank()) {
                        binding.tvDeepDivesDesc.text = toggle.description
                        binding.tvDeepDivesDesc.visibility = View.VISIBLE
                    } else {
                        binding.tvDeepDivesDesc.visibility = View.GONE
                    }
                }
            }
        }

        // Setup switch listeners for API call on user toggle
        val sourceKey = detail.sourceKey ?: getApiSourceKey(args.sourceId)
        setupSwitchListeners(sourceKey)

        // Controls description
        binding.tvDeleteDesc.text = "Permanently remove everything from $activeSourceName"
    }

    private fun setupSwitchListeners(sourceKey: String) {
        val listener = android.widget.CompoundButton.OnCheckedChangeListener { _, _ ->
            val param = com.humotron.app.domain.modal.param.UpdateDataSourceUsageParam(
                aiChat = binding.switchChat.isChecked,
                aiInsights = binding.switchInsights.isChecked,
                productSuggestions = binding.switchSuggestions.isChecked,
                deepDives = binding.switchDeepDives.isChecked
            )
            viewModel.updateDataSourceUsage(sourceKey, param)
        }

        binding.switchInsights.setOnCheckedChangeListener(listener)
        binding.switchSuggestions.setOnCheckedChangeListener(listener)
        binding.switchChat.setOnCheckedChangeListener(listener)
        binding.switchDeepDives.setOnCheckedChangeListener(listener)
    }

    private fun applyAccentColor(colorHex: String) {
        try {
            val colorInt = Color.parseColor(colorHex)
            binding.ivDeviceIcon.imageTintList = ColorStateList.valueOf(colorInt)

            val bgTint = Color.argb(
                (255 * 0.22).toInt(),
                Color.red(colorInt),
                Color.green(colorInt),
                Color.blue(colorInt)
            )
            binding.llDeviceIcon.backgroundTintList = ColorStateList.valueOf(bgTint)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getIconRes(iconStr: String?, sourceKeyStr: String?): Int {
        val icon = iconStr?.lowercase() ?: ""
        val key = sourceKeyStr?.lowercase() ?: ""
        return when {
            icon.contains("watch") || key.contains("apple") || key.contains("watch") -> R.drawable.ic_smart_band
            icon.contains("clock") -> R.drawable.ic_clock
            icon.contains("heart") || key.contains("polar") || key.contains("health") -> R.drawable.ic_onboard_heart
            icon.contains("stress") || key.contains("whoop") -> R.drawable.ic_start_stress
            icon.contains("wrist") || icon.contains("band") || key.contains("garmin") || key.contains("oura") || key.contains("ultrahuman") -> R.drawable.ic_wrist_band
            icon.contains("scan") || key.contains("google") -> R.drawable.ic_scan_node
            icon.contains("weather") || icon.contains("sun") || key.contains("env") -> R.drawable.ic_weather
            icon.contains("calendar") || key.contains("workday") -> R.drawable.ic_appointments
            icon.contains("sheet") || icon.contains("report") || key.contains("report") -> R.drawable.ic_sheet_document
            icon.contains("target") || icon.contains("goal") -> R.drawable.ic_target
            icon.contains("menu") || key.contains("strava") || key.contains("lifestyle") -> R.drawable.ic_menu_24px
            else -> R.drawable.ic_spark
        }
    }
}

