package com.humotron.app.ui.track

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.humotron.app.R
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentTrackBinding
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.AllMetricsResponse
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.domain.modal.response.MedicalPdf
import com.humotron.app.domain.modal.response.MedicalPdfData
import com.humotron.app.domain.modal.response.MedicalPdfResponse
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.domain.modal.response.toPdfReportData
import com.humotron.app.ui.assessment.AssessmentAdapter
import com.humotron.app.ui.bloodTest.BloodTestActivity
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import com.humotron.app.ui.connect.adapter.DeviceInfo
import com.humotron.app.ui.connect.dialog.DeviceSelectionBottomSheet
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.ui.dialogs.DeleteConfirmationBottomSheet
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.fadeIn
import com.humotron.app.util.showWithFade
import com.humotron.app.util.toast
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackFragment : BaseFragment(R.layout.fragment_track), OnClickListener {

    private lateinit var binding: FragmentTrackBinding
    private val viewModel: DeviceViewModel by viewModels()
    private val bloodTestViewModel: BloodTestViewModel by activityViewModels()
    private var deviceAdapter: DeviceAdapter? = null
    private var wearableProviderAdapter: WearableProviderAdapter? = null
    private var healthReportAdapter: HealthReportAdapter? = null
    private var healthReportTrackAdapter: HealthReportTrackAdapter? = null
    private var assessmentAdapter: AssessmentAdapter? = null
    private var trackingAdapter: TrackingAdapter? = null
    private var trackingGroupedAdapter: TrackingGroupedAdapter? = null
    private var yetToTrackAdapter: YetToTrackAdapter? = null
    private var yetToTrackGroupedAdapter: YetToTrackGroupedAdapter? = null

    private var selectedMainTabId = R.id.btnSources
    private var selectedSourcesSubTabId = R.id.tvLiveStreaming
    private var selectedMetricsSubTabId = R.id.llTabTracking

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrackBinding.bind(view)

        initClicks()
        initViews()
        initObservers()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        syncSmartSuggestions()
    }

    private fun initViews() {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("EEEE, MMMM dd", java.util.Locale.getDefault())
        binding.tvTrackDate.text = dateFormat.format(calendar.time)
        binding.tvGreeting.text = "Hello, ${prefUtils.getLoginResponse().firstName ?: "User"}"

        // Sync UI with persisted state
        when (selectedMainTabId) {
            R.id.btnMetrics -> showMetricsTab()
            else -> showSourcesTab()
        }

        when (selectedSourcesSubTabId) {
            R.id.tvTestCheckIns -> showTestCheckInsTab()
            else -> showLiveStreamingTab()
        }

        when (selectedMetricsSubTabId) {
            R.id.llTabYetToTrack -> showYetToTrackTab()
            else -> showTrackingTab()
        }

        deviceAdapter = DeviceAdapter(emptyList()) { userDevice ->
            findNavController().navigate(R.id.fragmentDeviceData, Bundle().apply {
                putParcelable(NavKeys.WEARABLE, userDevice)
            })
        }
        binding.rvDevices.adapter = deviceAdapter


        wearableProviderAdapter = WearableProviderAdapter(emptyList()) { provider ->
            // handle click if needed
        }
        binding.rvWearableDevices.adapter = wearableProviderAdapter

        assessmentAdapter =
            AssessmentAdapter(requireContext(), emptyList<MergedAssessment>()) { assessment ->
                handleAssessmentClick(assessment)
            }
        binding.rvAssessments.adapter = assessmentAdapter


        trackingAdapter = TrackingAdapter(emptyList()) { metric ->

            val deviceId = metric.deviceId?.firstOrNull()
            val deviceName = metric.deviceName
            val deviceType = DeviceType.from(deviceName).value
            val dateTime = metric.metricValue?.timestamp ?: ""

            val mappedMetric = AllMetricsResponse.Data.Metric(
                id = metric.id,
                metricName = metric.metricName,
                metricUnit = metric.metricUnit,
                metricUserFacingName = metric.metricUserFacingName,
                deviceId = metric.deviceId,
                status = metric.status,
                metricWhat = metric.metricWhat,
                metricWhy = metric.metricWhy,
                observationLens = metric.observationLens,
                metricOrder = metric.metricOrder,
                allMetrics = metric.allMetrics?.let {
                    AllMetricsResponse.Data.Metric.DisplayConfig(it.enabled, it.displayType)
                },
                categoryId = metric.categoryId,
                metricType = metric.metricType,
                metricReading = metric.metricReading,
                metricValue = metric.metricValue?.let {
                    AllMetricsResponse.Data.Metric.MetricValue(
                        it.fieldLabel,
                        it.value,
                        it.timestamp
                    )
                },
                insightCount = metric.insightCount,
                supplementCount = metric.supplementCount,
                recipeCount = metric.recipeCount
            )

            findNavController().navigate(
                R.id.fragmentMetric,
                bundleOf(
                    NavKeys.KEY_DEVICE_ID to deviceId,
                    NavKeys.KEY_DATE_TIME to dateTime,
                    NavKeys.KEY_METRIC to mappedMetric,
                    NavKeys.KEY_DEVICE_NAME to deviceName,
                    NavKeys.KEY_DEVICE_TYPE to deviceType
                )
            )
        }
        binding.rvTracking.adapter = trackingAdapter


        trackingGroupedAdapter = TrackingGroupedAdapter(emptyList()) { groupedMetric ->
            findNavController().navigate(R.id.fragmentGroupedMetricsDetails, Bundle().apply {
                putParcelable(NavKeys.GROUPED_METRIC, groupedMetric)
            })
        }
        binding.rvTrackingGrouped.adapter = trackingGroupedAdapter


        yetToTrackAdapter = YetToTrackAdapter(emptyList()) { metric ->
            findNavController().navigate(R.id.fragmentYetToTrackDetails, Bundle().apply {
                putParcelable(NavKeys.KEY_METRIC, metric)
            })
        }
        binding.rvYetToTrack.adapter = yetToTrackAdapter


        yetToTrackGroupedAdapter = YetToTrackGroupedAdapter(emptyList()) { groupedMetric ->
            findNavController().navigate(R.id.fragmentGroupedMetricsDetails, Bundle().apply {
                putParcelable(NavKeys.GROUPED_METRIC, groupedMetric)
            })
        }
        binding.rvYetToTrackGrouped.adapter = yetToTrackGroupedAdapter

        if (prefUtils.getHardwareDetailsList().isEmpty()) {
            // binding.dsvWearables.isVisible = false
            // binding.dsvHealthMonitoring.isVisible = false
            viewModel.getHardwareList()
        } else {
            viewModel.getDeviceData()
            // binding.dsvWearables.isVisible = true
            // binding.dsvHealthMonitoring.isVisible = true
        }

        viewModel.getMergedAssessmentList()
        viewModel.getMedicalPdfList()
        viewModel.getHealthMetricTrackingByUserId()
        viewModel.getUntrackedMetricByUserId()
    }

    override fun onResume() {
        super.onResume()
        binding.swipeRefreshMetrics.isRefreshing = false
        viewModel.refreshUserDeviceData(true)
        viewModel.refreshWearableDeviceData(true)
        // viewModel.getMergedAssessmentList(true)
        viewModel.getMedicalPdfList(true)
        viewModel.getHealthMetricTrackingByUserId()
        viewModel.getUntrackedMetricByUserId()
    }

    private fun initClicks() {
        binding.toggleGroupMainTabs.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnSources -> showSourcesTab()
                    R.id.btnMetrics -> showMetricsTab()
                }
            }
        }
        binding.tvLiveStreaming.setOnClickListener(this)
        binding.tvTestCheckIns.setOnClickListener(this)
        binding.llTabTracking.setOnClickListener(this)
        binding.llTabYetToTrack.setOnClickListener(this)
        binding.layoutAddSourceRow.setOnClickListener(this)
        binding.tvUpload.setOnClickListener(this)
        binding.swipeRefreshMetrics.setOnRefreshListener {
            binding.swipeRefreshMetrics.isRefreshing = false
            viewModel.refreshUserDeviceData(true)
            //viewModel.refreshWearableDeviceData(true)
            // viewModel.getMergedAssessmentList(true)
            viewModel.getMedicalPdfList(true)
            viewModel.getHealthMetricTrackingByUserId()
            viewModel.getUntrackedMetricByUserId()
        }
        binding.swipeRefreshSources.setOnRefreshListener {
            binding.swipeRefreshSources.isRefreshing = false
            viewModel.refreshUserDeviceData(true)
            viewModel.refreshWearableDeviceData(true)
            // viewModel.getMergedAssessmentList(true)
            viewModel.getMedicalPdfList(true)
            viewModel.getHealthMetricTrackingByUserId()
            viewModel.getUntrackedMetricByUserId()
        }
        binding.llUploadSyncReport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentTrack_to_fragmentUploadReportIntro)
        }
        binding.switchSmartSuggestionsYetToTrack.setOnClickListener {
            binding.llGuidancePlan.isVisible = binding.switchSmartSuggestionsYetToTrack.isChecked
        }
        binding.switchSmartSuggestionsTracking.setOnClickListener {
            trackingAdapter?.setSmartSuggestionsEnabled(binding.switchSmartSuggestionsTracking.isChecked)
        }
        binding.tvBookPill.setOnClickListener {
            findNavController().navigate("humotron://shop/test_detail".toUri())
        }
    }

    private fun showSourcesTab() {
        selectedMainTabId = R.id.btnSources
        binding.clTabSources.isVisible = true
        binding.clTabMetrics.isVisible = false
        if (binding.toggleGroupMainTabs.checkedButtonId != R.id.btnSources) {
            binding.toggleGroupMainTabs.check(R.id.btnSources)
        }
    }

    private fun showMetricsTab() {
        selectedMainTabId = R.id.btnMetrics
        binding.clTabSources.isVisible = false
        binding.clTabMetrics.isVisible = true
        if (binding.toggleGroupMainTabs.checkedButtonId != R.id.btnMetrics) {
            binding.toggleGroupMainTabs.check(R.id.btnMetrics)
        }
    }

    private fun showLiveStreamingTab() {
        selectedSourcesSubTabId = R.id.tvLiveStreaming
        binding.llLiveStreamingContent.isVisible = true
        binding.llTestCheckInsContent.isVisible = false

        binding.tvLiveStreaming.setBackgroundResource(R.drawable.bg_track_chip_selected)
        binding.tvLiveStreaming.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.lime)
        )
        binding.tvTestCheckIns.setBackgroundResource(R.drawable.bg_track_chip_unselected)
        binding.tvTestCheckIns.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.ink3)
        )
    }

    private fun showTestCheckInsTab() {
        selectedSourcesSubTabId = R.id.tvTestCheckIns
        binding.llTestCheckInsContent.isVisible = true
        binding.llLiveStreamingContent.isVisible = false

        binding.tvLiveStreaming.setBackgroundResource(R.drawable.bg_track_chip_unselected)
        binding.tvLiveStreaming.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.ink3)
        )
        binding.tvTestCheckIns.setBackgroundResource(R.drawable.bg_track_chip_selected)
        binding.tvTestCheckIns.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.lime)
        )
    }

    private fun showTrackingTab() {
        selectedMetricsSubTabId = R.id.llTabTracking
        binding.llTrackContent.isVisible = true
        binding.llYetToTrackContent.isVisible = false
        binding.llTabTracking.setBackgroundResource(R.drawable.bg_metrics_chip_selected)
        binding.llTabYetToTrack.setBackgroundResource(R.drawable.bg_metrics_chip_unselected)
        binding.viewTracking.setBackgroundResource(R.drawable.bg_track_progress_fill_lime)
        binding.viewYetToTrack.setBackgroundResource(R.drawable.bg_track_progress_track)

        binding.tvTracking.setTextColor(ContextCompat.getColor(requireContext(), R.color.lime))
        binding.tvYetToTrack.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink4))
    }

    private fun showYetToTrackTab() {
        selectedMetricsSubTabId = R.id.llTabYetToTrack
        binding.llYetToTrackContent.isVisible = true
        binding.llTrackContent.isVisible = false
        binding.llTabTracking.setBackgroundResource(R.drawable.bg_metrics_chip_unselected)
        binding.llTabYetToTrack.setBackgroundResource(R.drawable.bg_metrics_chip_selected)

        binding.viewYetToTrack.setBackgroundResource(R.drawable.bg_track_progress_fill_lime)
        binding.viewTracking.setBackgroundResource(R.drawable.bg_track_progress_track)

        binding.tvTracking.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.ink4)
        )
        binding.tvYetToTrack.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.lime)
        )
    }

    private fun initObservers() {
        viewModel.observeUserDeviceData()

        viewModel.getWearableProviderListData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = it.data?.data?.devices ?: return@observe
                    if (data.isNotEmpty()) {
                        wearableProviderAdapter?.updateData(data)
                        binding.rvWearableDevices.showWithFade { }
                    } else {
                        binding.rvWearableDevices.isVisible = false
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    if (wearableProviderAdapter?.itemCount == 0) {
                        showProgress()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.deviceData.collect {
                /*it.hrvMapper.let {
                    binding.tvHrv.text = "${it.hrv}"
                    if (it.milisecond > 0) {
                        binding.tvLastSync.text = getTimeAgo(it.milisecond)
                    } else {
                        binding.tvLastSync.text = "-"
                    }

                }
                it.hrMapper.let {
                    binding.tvHr.text = it.hr.toString()
                }*/
            }
        }

        viewModel.getDeviceListData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = it.data?.data ?: return@observe
                    val allDevices = mutableListOf<UserDevice>()
                    data.wearables?.let { allDevices.addAll(it) }
                    data.health?.let { allDevices.addAll(it) }

                    if (allDevices.isNotEmpty()) {
                        deviceAdapter?.updateData(allDevices)
                        binding.rvDevices.showWithFade { }
                        binding.tvSourcesSynced.text =
                            "${allDevices.size} sources feeding Track · synced just now"
                    } else {
                        binding.rvDevices.isVisible = false
                        binding.tvSourcesSynced.text = "No sources feeding Track"
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    // Only show progress if we don't have any wearables yet
                    if ((deviceAdapter == null || deviceAdapter?.itemCount == 0)) {
                        showProgress()
                    }
                }
            }
        }

        viewModel.getHardwareListData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = it.data?.data ?: return@observe
                    data.hardwareDetails?.let { list ->
                        prefUtils.setHardwareDetailsList(list)
                    }
                    data.hardwareDetails?.firstOrNull { it.hardwareType == DeviceType.RING.value }
                        ?.let {
                            prefUtils.setRingHardwareData(it)
                            it.userHardwareUUID?.let { value ->
                                prefUtils.setString(
                                    Preference.WEARABLE_RING,
                                    value
                                )
                            }
                        }
                    data.hardwareDetails?.firstOrNull { it.hardwareType == DeviceType.BAND.value }
                        ?.let {
                            prefUtils.setBandHardwareData(it)
                            it.userHardwareUUID?.let { value ->
                                prefUtils.setString(
                                    Preference.WEARABLE_BAND,
                                    value
                                )
                            }
                        }
                    data.hardwareDetails?.firstOrNull { it.hardwareType == DeviceType.SMART_CUFF.value }
                        ?.let {
                            prefUtils.setBpHardwareData(it)
                            it.userHardwareUUID?.let { value ->
                                prefUtils.setString(
                                    Preference.BP_MACHINE,
                                    value
                                )
                            }
                        }
                    data.hardwareDetails?.firstOrNull { it.hardwareType == DeviceType.WEIGHT_MACHINE.value }
                        ?.let {
                            prefUtils.setWeightHardwareData(it)
                            it.userHardwareUUID?.let { value ->
                                prefUtils.setString(
                                    Preference.WEIGHT_SCALE,
                                    value
                                )
                            }
                        }
                    if (!data.hardwareDetails.isNullOrEmpty()) {
                        viewModel.refreshUserDeviceData(true)
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    // Only show progress if we don't have any wearables yet
                    if ((deviceAdapter == null || deviceAdapter?.itemCount == 0)
                    ) {
                        showProgress()
                    }
                }
            }
        }

        viewModel.mergedAssessmentListLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = it.data?.data ?: return@observe
                    if (data.isNotEmpty()) {
                        /*binding.dsvAssessments.fadeIn()
                        binding.clNoPictureData.isVisible = false*/
                        assessmentAdapter?.updateData(data)
                    } else {
                        /*binding.dsvAssessments.isVisible = false
                        binding.clNoPictureData.fadeIn()*/
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    //showProgress()
                }
            }
        }

        viewModel.medicalPdfListLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = it.data?.data?.pdfData ?: return@observe
                    if (data.isNotEmpty()) {
                        // setupHealthReportRecyclerView(data)
                        setupHealthReportTrackRecyclerView(data)
                        binding.clHealthReportsHeader.fadeIn()
                    } else {
                        binding.clHealthReportsHeader.isVisible = false
                    }
                    // History Matters should always be visible below according to the user's request
                    binding.tvTitleHistoryMatters.isVisible = true
                    binding.tvDescription.isVisible = true
                    binding.clHistMatter.isVisible = true
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    binding.clHealthReportsHeader.isVisible = false
                }

                Status.LOADING -> {
                    // Only show progress if we don't have any health reports yet
                    if (healthReportAdapter == null || healthReportAdapter?.itemCount == 0) {
                        showProgress()
                    }
                }
            }
        }

        viewModel.healthMetricTrackingLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.shimmerTracking.isVisible = false
                    val data = it.data?.data ?: return@observe

                    val individualMetrics = data.individualMetrics
                    if (!individualMetrics.isNullOrEmpty()) {
                        binding.rvTracking.isVisible = true
                        trackingAdapter?.updateData(individualMetrics)
                    } else {
                        binding.rvTracking.isVisible = false
                    }

                    val groupedMetrics = data.groupedMetrics
                    if (!groupedMetrics.isNullOrEmpty()) {
                        binding.rvTrackingGrouped.isVisible = true
                        trackingGroupedAdapter?.updateData(groupedMetrics)
                    } else {
                        binding.rvTrackingGrouped.isVisible = false
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    binding.shimmerTracking.isVisible = false
                }

                Status.LOADING -> {
                    binding.shimmerTracking.isVisible = true
                    binding.rvTracking.isVisible = false
                    binding.rvTrackingGrouped.isVisible = false
                }
            }
        }

        viewModel.untrackedMetricLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.shimmerYetToTrack.isVisible = false
                    val data = it.data?.data ?: return@observe

                    val individualMetrics = data.individualMetrics
                    if (!individualMetrics.isNullOrEmpty()) {
                        binding.rvYetToTrack.isVisible = true
                        yetToTrackAdapter?.updateData(individualMetrics)
                    } else {
                        binding.rvYetToTrack.isVisible = false
                    }

                    val groupedMetrics = data.groupedMetrics
                    if (!groupedMetrics.isNullOrEmpty()) {
                        binding.rvYetToTrackGrouped.isVisible = true
                        yetToTrackGroupedAdapter?.updateData(groupedMetrics)
                    } else {
                        binding.rvYetToTrackGrouped.isVisible = false
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    binding.shimmerYetToTrack.isVisible = false
                }

                Status.LOADING -> {
                    binding.shimmerYetToTrack.isVisible = true
                    binding.rvYetToTrack.isVisible = false
                    binding.rvYetToTrackGrouped.isVisible = false
                }
            }
        }

        viewModel.removePdfLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    toast(it.data?.message ?: "Report deleted successfully")
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    toast(it.error?.errorMessage ?: "Failed to delete report")
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }


    private fun handleAssessmentClick(assessment: MergedAssessment) {
        when (assessment.status) {
            "Completed" -> {
                toast("The assessment is complete.")
            }

            "Resume" -> {
                findNavController().navigate(R.id.assessmentFragment, Bundle().apply {
                    putParcelable(NavKeys.ASSESSMENT, assessment)
                })
            }

            "Start Now" -> {
                findNavController().navigate(R.id.fragmentAssessmentInstruction, Bundle().apply {
                    putParcelable(NavKeys.ASSESSMENT, assessment)
                })
            }
        }
    }

    private fun setupHealthReportTrackRecyclerView(reports: List<MedicalPdf>) {
        if (healthReportTrackAdapter == null) {
            healthReportTrackAdapter =
                HealthReportTrackAdapter(
                    requireContext(),
                    reports
                ) { report, position, currentList ->
                    val extractMetricsResponse =
                        MedicalPdfResponse(
                            status = "success",
                            message = "Data found",
                            data = MedicalPdfData(
                                pdfData = currentList,
                                userId = "",
                                uploadType = "MANUAL",
                                pdfCount = currentList.size,
                                id = ""
                            )
                        )
                    findNavController().navigate(
                        R.id.action_fragmentTrack_to_fragmentPDFReportList,
                        Bundle().apply {
                            putParcelable("extractMetricsResponse", extractMetricsResponse)
                            putInt("selectedPosition", position)
                        }
                    )
                }
        } else {
            healthReportTrackAdapter?.updateData(reports)
        }
        binding.rvReportsTrack.adapter = healthReportTrackAdapter
    }


    private fun setupHealthReportRecyclerView(reports: List<MedicalPdf>) {
        if (healthReportAdapter == null) {
            healthReportAdapter =
                HealthReportAdapter(requireActivity(), reports) { report, action ->
                    when (action) {
                        HealthReportAdapter.Action.VIEW, HealthReportAdapter.Action.ITEM_CLICK -> {
                            // Map all current PDFs to PdfReportData for the carousel detail view
                            val pdfReportDataList = reports.map { it.toPdfReportData() }
                            val extractMetricsResponse =
                                MedicalPdfResponse(
                                    status = "success",
                                    message = "Data found",
                                    data = MedicalPdfData(
                                        pdfData = /*pdfReportDataList*/reports,
                                        userId = "",
                                        uploadType = "MANUAL",
                                        pdfCount = pdfReportDataList.size,
                                        id = ""
                                    )
                                )
                            val clickedIndex = reports.indexOf(report).coerceAtLeast(0)
                            bloodTestViewModel.setUploadResult(extractMetricsResponse, clickedIndex)
                            findNavController().navigate(
                                R.id.action_fragmentTrack_to_fragmentUploadedReports,
                                Bundle().apply { putBoolean("isFromTrack", true) }
                            )
                        }

                        HealthReportAdapter.Action.DELETE -> {
                            val bottomSheet = DeleteConfirmationBottomSheet.newInstance {
                                report.id.let { pdfId ->
                                    viewModel.removePdfByPdfId(pdfId)
                                }
                            }
                            bottomSheet.show(
                                childFragmentManager,
                                DeleteConfirmationBottomSheet.TAG
                            )
                        }
                    }
                }
        } else {
            healthReportAdapter?.updateData(reports)
        }

        // Always re-set adapter and transformer for the new view instance
        binding.dsvHealthReports.adapter = healthReportAdapter
        binding.dsvHealthReports.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build()
        )
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.llTabTracking -> showTrackingTab()
            binding.llTabYetToTrack -> showYetToTrackTab()
            binding.tvLiveStreaming -> showLiveStreamingTab()
            binding.tvTestCheckIns -> showTestCheckInsTab()
            binding.layoutAddSourceRow -> {
                if (v == binding.layoutAddSourceRow) {
                    findNavController().navigate(R.id.fragmentAddSource)
                    return
                }

                val wearableDevices = arrayListOf(
                    DeviceInfo(
                        R.drawable.ic_bg_ring,
                        "Humotron Smart Ring",
                        "Connect to sync sleep & recovery metrics",
                        deviceType = DeviceType.RING
                    ), DeviceInfo(
                        R.drawable.ic_smart_band,
                        "Humotron Wrist Band",
                        "Health tracking smart band",
                        deviceType = DeviceType.BAND
                    ),
                    DeviceInfo(
                        R.drawable.ic_bp_machine_setup,
                        "BP Monitor",
                        "One-click arm BP measurement",
                        deviceType = DeviceType.SMART_CUFF
                    ), DeviceInfo(
                        R.drawable.ic_weight_scale,
                        "Humotron Weight Scale",
                        "Connect to track weight & body mass metrics",
                        deviceType = DeviceType.WEIGHT_MACHINE
                    )
                )

                val bottomSheet = DeviceSelectionBottomSheet.newInstance(wearableDevices)
                bottomSheet.setDeviceSelectionListener { deviceInfo ->
                    bottomSheet.dismiss()

                    findNavController().navigate(R.id.fragmentConnectInfo, Bundle().apply {
                        putParcelable(NavKeys.DEVICE_INFO, deviceInfo)
                    })
                }
                bottomSheet.show(childFragmentManager, DeviceSelectionBottomSheet.TAG)
            }

            binding.tvUpload -> {
                startActivity(Intent(requireContext(), BloodTestActivity::class.java))
            }
        }
    }

    private fun syncSmartSuggestions() {
        trackingAdapter?.setSmartSuggestionsEnabled(binding.switchSmartSuggestionsTracking.isChecked)
        binding.llGuidancePlan.isVisible = binding.switchSmartSuggestionsYetToTrack.isChecked
    }

    fun getTimeAgo(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeInMillis

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7

        return when {
            seconds < 60 -> "$seconds seconds ago"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days < 7 -> "$days days ago"
            else -> "$weeks weeks ago"
        }
    }
}
