package com.humotron.app.ui.track

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.humotron.app.R
import com.humotron.app.core.AppConstant.ASSESSMENT
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentTrackBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.domain.modal.response.MedicalPdf
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.ui.assesment.AssessmentActivity
import com.humotron.app.ui.bloodTest.BloodTestActivity
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import com.humotron.app.ui.assesment.CardiovascularAssessmentBottomSheet
import com.humotron.app.ui.connect.adapter.DeviceInfo
import com.humotron.app.ui.connect.dialog.DeviceSelectionBottomSheet
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.domain.modal.response.toPdfReportData
import androidx.fragment.app.activityViewModels
import com.humotron.app.ui.dialogs.DeleteConfirmationBottomSheet
import com.humotron.app.util.fadeIn
import com.humotron.app.util.showWithFade
import com.humotron.app.util.toast
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.widget.TextView
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class TrackFragment : BaseFragment(R.layout.fragment_track), OnClickListener {

    private lateinit var binding: FragmentTrackBinding
    private val viewModel: DeviceViewModel by viewModels()
    private val bloodTestViewModel: BloodTestViewModel by activityViewModels()
    private var deviceAdapter: DeviceAdapter? = null
    private var wearableAdapter: WearableAdapter? = null

    private var healthAdapter: WearableAdapter? = null
    private var assessmentAdapter: AssessmentAdapter? = null
    private var healthReportAdapter: HealthReportAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrackBinding.bind(view)

        initClicks()
        initViews()
        initObservers()
    }

    private fun initViews() {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("EEEE, MMMM dd", java.util.Locale.getDefault())
        binding.tvTrackDate.text = dateFormat.format(calendar.time)
        binding.tvGreeting.text = "Hello, ${prefUtils.getLoginResponse().firstName ?: "User"}"
        showSourcesTab()

        binding.rvDevices.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        deviceAdapter = DeviceAdapter(emptyList()) { userDevice ->
            findNavController().navigate(R.id.fragmentDeviceData, Bundle().apply {
                putParcelable(NavKeys.WEARABLE, userDevice)
            })
        }
        binding.rvDevices.adapter = deviceAdapter

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
    }

    override fun onResume() {
        super.onResume()
        binding.swipeRefreshLayout.isRefreshing = false
        viewModel.refreshUserDeviceData(true)
        viewModel.getMergedAssessmentList(true)
        viewModel.getMedicalPdfList(true)
    }

    private fun initClicks() {
        binding.btnSources.setOnClickListener(this)
        binding.btnMetrics.setOnClickListener(this)
        binding.tvLiveStreaming.setOnClickListener(this)
        binding.tvTestCheckIns.setOnClickListener(this)
        binding.btnAddWearables.setOnClickListener(this)
        binding.llTabTracking.setOnClickListener(this)
        binding.llTabYetToTrack.setOnClickListener(this)
        binding.btnAddHealthMonitoring.setOnClickListener(this)
        binding.layoutAddSourceRow.setOnClickListener(this)
        binding.tvUpload.setOnClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refreshUserDeviceData(true)
            viewModel.getMergedAssessmentList(true)
            viewModel.getMedicalPdfList(true)
        }
        binding.swipeRefreshSources.setOnRefreshListener {
            binding.swipeRefreshSources.isRefreshing = false
            viewModel.refreshUserDeviceData(true)
            viewModel.getMergedAssessmentList(true)
            viewModel.getMedicalPdfList(true)
        }
    }

    private fun showSourcesTab() {
        binding.clTabSources.isVisible = true
        binding.clTabMetrics.isVisible = false
    }

    private fun showMetricsTab() {
        binding.clTabSources.isVisible = false
        binding.clTabMetrics.isVisible = true
    }

    private fun showLiveStreamingTab() {
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
        binding.llTrackContent.isVisible = true
        binding.llYetToTrackContent.isVisible = false
        binding.llTabTracking.setBackgroundResource(R.drawable.bg_metrics_chip_selected)
        binding.llTabYetToTrack.setBackgroundResource(R.drawable.bg_metrics_chip_unselected)
        binding.viewTracking.setBackgroundResource(R.drawable.bg_track_progress_fill_lime)
        binding.viewYetToTrack.setBackgroundResource(R.drawable.bg_track_progress_track)
        binding.tvYetToTrack.setBackgroundResource(0)

        binding.tvTracking.setTextColor(ContextCompat.getColor(requireContext(), R.color.lime))
        binding.tvYetToTrack.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink4))
    }

    private fun showYetToTrackTab() {
        binding.llYetToTrackContent.isVisible = true
        binding.llTrackContent.isVisible = false
        binding.llTabTracking.setBackgroundResource(R.drawable.bg_metrics_chip_unselected)
        binding.llTabYetToTrack.setBackgroundResource(R.drawable.bg_metrics_chip_selected)

        binding.viewYetToTrack.setBackgroundResource(R.drawable.bg_track_progress_fill_lime)
        binding.viewTracking.setBackgroundResource(R.drawable.bg_track_progress_track)

        binding.tvYetToTrack.setBackgroundResource(R.drawable.bg_track_progress_track)

        binding.tvTracking.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.ink4)
        )
        binding.tvYetToTrack.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.lime)
        )
    }

    private fun initObservers() {
        viewModel.observeUserDeviceData()

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
                        binding.rvDevices.isVisible = true
                        binding.tvSourcesSynced.text =
                            "${allDevices.size} sources feeding Track · synced just now"
                    } else {
                        binding.rvDevices.isVisible = false
                        binding.tvSourcesSynced.text = "No sources feeding Track"
                    }

                    if (!data.wearables.isNullOrEmpty()) {
                        setupWearablesDiscreteScrollView(data.wearables)
                        binding.dsvWearables.showWithFade { }
                    } else {
                        binding.dsvWearables.isVisible = false
                        binding.clNoDeviceFound.showWithFade { }
                    }

                    if (!data.health.isNullOrEmpty()) {
                        setupHealthMonitoringDiscreteScrollView(data.health)
                        binding.dsvHealthMonitoring.showWithFade { }
                    } else {
                        binding.dsvHealthMonitoring.isVisible = false
                        binding.clNoHealthMonitoringFound.showWithFade { }
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
                    if ((wearableAdapter == null || wearableAdapter?.itemCount == 0) &&
                        (healthAdapter == null || healthAdapter?.itemCount == 0)
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
                        setupAssessmentRecyclerView(data)
                        binding.dsvAssessments.fadeIn()
                        binding.clNoPictureData.isVisible = false
                    } else {
                        binding.dsvAssessments.isVisible = false
                        binding.clNoPictureData.fadeIn()
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
                        setupHealthReportRecyclerView(data)
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

    private fun setupWearablesDiscreteScrollView(userDevices: List<UserDevice>) {
        wearableAdapter = WearableAdapter(userDevices) { userDevice ->
            findNavController().navigate(R.id.fragmentDeviceData, Bundle().apply {
                putParcelable(NavKeys.WEARABLE, userDevice)
            })
        }
        binding.dsvWearables.adapter = wearableAdapter
        binding.dsvWearables.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build()
        )
    }

    private fun setupHealthMonitoringDiscreteScrollView(userDevices: List<UserDevice>) {
        healthAdapter = WearableAdapter(userDevices) { userDevice ->
            findNavController().navigate(R.id.fragmentDeviceData, Bundle().apply {
                putParcelable(NavKeys.WEARABLE, userDevice)
            })
        }
        binding.dsvHealthMonitoring.adapter = healthAdapter
        binding.dsvHealthMonitoring.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build()
        )
    }

    private fun showAssessmentSheet(assessment: MergedAssessment) {

        val json = Gson().toJson(assessment)
        Log.e("TAG", "showAddddssessmentSheet: ${json} ")
        Log.e("TAG", "showAddddssessmentSheet00: ${assessment} ")

        val sheet = CardiovascularAssessmentBottomSheet.newInstance(json)

        sheet.onProceedClicked = {

            if (isAdded) {
                val intent = Intent(requireContext(), AssessmentActivity::class.java)
                intent.putExtra(ASSESSMENT, json)
                startActivity(intent)
            }
        }

        sheet.show(parentFragmentManager, CardiovascularAssessmentBottomSheet.TAG)
    }

    private fun setupAssessmentRecyclerView(assessments: List<MergedAssessment>) {
        if (assessmentAdapter == null) {
            assessmentAdapter = AssessmentAdapter(requireActivity(), assessments) { assessment ->
                // ... click handling remains same
                when (assessment.status) {
                    "Completed" -> toast("the assessment is completed")
                    "Resume" -> {
                        if (isAdded) {
                            val json = Gson().toJson(assessment)
                            val intent = Intent(requireContext(), AssessmentActivity::class.java)
                            intent.putExtra(ASSESSMENT, json)
                            startActivity(intent)
                        }
                    }

                    "Start Now" -> showAssessmentSheet(assessment)
                }
            }
        } else {
            assessmentAdapter?.updateData(assessments)
        }

        // Always re-set adapter and transformer for the new view instance
        binding.dsvAssessments.adapter = assessmentAdapter
        binding.dsvAssessments.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build()
        )
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
                                com.humotron.app.domain.modal.response.ExtractMetricsResponse(
                                    status = "success",
                                    message = "Data found",
                                    data = com.humotron.app.domain.modal.response.MetricsData(
                                        pdfData = pdfReportDataList,
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
            binding.btnSources -> showSourcesTab()
            binding.btnMetrics -> showMetricsTab()
            binding.tvLiveStreaming -> showLiveStreamingTab()
            binding.tvTestCheckIns -> showTestCheckInsTab()
            binding.btnAddWearables, binding.layoutAddSourceRow -> {
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
                        deviceType = DeviceType.BP_MACHINE
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

            binding.btnAddHealthMonitoring -> {
                val healthMonitoringDevices = arrayListOf(
                    DeviceInfo(
                        R.drawable.ic_bp_machine_setup,
                        "BP Monitor",
                        "One-click arm BP measurement",
                        deviceType = DeviceType.BP_MACHINE
                    ), DeviceInfo(
                        R.drawable.ic_weight_scale,
                        "Humotron Weight Scale",
                        "Connect to track weight & body mass metrics",
                        deviceType = DeviceType.WEIGHT_MACHINE
                    )
                )
                val bottomSheet = DeviceSelectionBottomSheet.newInstance(healthMonitoringDevices)
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
