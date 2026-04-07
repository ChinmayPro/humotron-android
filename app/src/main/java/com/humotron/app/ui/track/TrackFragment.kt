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
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable
import com.humotron.app.domain.modal.response.MedicalPdf
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.ui.assesment.AssessmentActivity
import com.humotron.app.ui.bloodTest.BloodTestActivity
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import com.humotron.app.ui.assesment.CardiovascularAssessmentBottomSheet
import com.humotron.app.ui.connect.dialog.DeviceSelectionBottomSheet
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.domain.modal.response.toPdfReportData
import androidx.fragment.app.activityViewModels
import com.humotron.app.ui.dialogs.DeleteConfirmationBottomSheet
import com.humotron.app.util.DialogUtils
import com.humotron.app.util.fadeIn
import com.humotron.app.util.showWithFade
import com.pluto.utilities.extensions.toast
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackFragment : BaseFragment(R.layout.fragment_track), OnClickListener {

    private lateinit var binding: FragmentTrackBinding
    private val viewModel: DeviceViewModel by viewModels()
    private val bloodTestViewModel: BloodTestViewModel by activityViewModels()
    private var wearableAdapter: WearableAdapter? = null
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
        prefUtils.getString(Preference.WEARABLE_RING).apply {
            if (isNullOrEmpty()) {
                binding.dsvWearables.isVisible = false
                viewModel.getHardwareList()
            } else {
                viewModel.getDeviceData()
                binding.dsvWearables.isVisible = true
            }
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
        binding.ivAdd.setOnClickListener(this)
        binding.tvUpload.setOnClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refreshUserDeviceData(true)
            viewModel.getMergedAssessmentList(true)
            viewModel.getMedicalPdfList(true)
        }
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
                    if (!data.wearables.isNullOrEmpty()) {
                        setupDiscreteScrollView(data.wearables)
                        binding.dsvWearables.showWithFade { }
                    } else {
                        binding.dsvWearables.isVisible = false
                        binding.clNoDeviceFound.showWithFade { }
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
                    if (wearableAdapter == null || wearableAdapter?.itemCount == 0) {
                        showProgress()
                    }
                }
            }
        }

        viewModel.getHardwareListData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = it.data?.data ?: return@observe
                    data.hardwareDetails?.firstOrNull { it.hardwareType == "HumotronRing" }?.let {
                        prefUtils.setHardwareData(it)
                        it.userHardwareUUID?.let { value ->
                            prefUtils.setString(
                                Preference.WEARABLE_RING,
                                value
                            )
                        }
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
                    if (wearableAdapter == null || wearableAdapter?.itemCount == 0) {
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

    private fun setupDiscreteScrollView(wearables: List<Wearable>) {
        wearableAdapter = WearableAdapter(wearables) { wearable ->
            findNavController().navigate(R.id.fragmentDeviceData, Bundle().apply {
                putParcelable("wearable", wearable)
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
            healthReportAdapter = HealthReportAdapter(requireActivity(), reports) { report, action ->
                when (action) {
                    HealthReportAdapter.Action.VIEW, HealthReportAdapter.Action.ITEM_CLICK -> {
                        // Map all current PDFs to PdfReportData for the carousel detail view
                        val pdfReportDataList = reports.map { it.toPdfReportData() }
                        val extractMetricsResponse = com.humotron.app.domain.modal.response.ExtractMetricsResponse(
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
                            android.os.Bundle().apply { putBoolean("isFromTrack", true) }
                        )
                    }
                    HealthReportAdapter.Action.DELETE -> {
                        val bottomSheet = DeleteConfirmationBottomSheet.newInstance {
                            report.id.let { pdfId ->
                                viewModel.removePdfByPdfId(pdfId)
                            }
                        }
                        bottomSheet.show(childFragmentManager, DeleteConfirmationBottomSheet.TAG)
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
            binding.ivAdd -> {
                val bottomSheet = DeviceSelectionBottomSheet()
                bottomSheet.setDeviceSelectionListener { deviceInfo ->
                    bottomSheet.dismiss()
                    findNavController().navigate(R.id.fragmentConnectInfo, Bundle().apply {
                        putParcelable("deviceInfo", deviceInfo)
                    })
                }
                bottomSheet.show(childFragmentManager, "device_list")
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
