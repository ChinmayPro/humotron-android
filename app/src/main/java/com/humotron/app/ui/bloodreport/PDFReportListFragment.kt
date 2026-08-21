package com.humotron.app.ui.bloodreport

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentPdfReportListBinding
import com.humotron.app.domain.modal.response.MedicalPdf
import com.humotron.app.domain.modal.response.MedicalPdfMetric
import com.humotron.app.domain.modal.response.PdfReportData
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import com.humotron.app.util.getTimeAgo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PDFReportListFragment : Fragment(R.layout.fragment_pdf_report_list) {

    private lateinit var binding: FragmentPdfReportListBinding
    private val args: PDFReportListFragmentArgs by navArgs()
    private val viewModel: BloodTestViewModel by activityViewModels()
    private var reportAdapter: ReportAdapter? = null
    private var metricsAdapter: MetricsReportAdapter? = null

    private var reportsList: List<MedicalPdf> = emptyList()
    private var currentPosition: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPdfReportListBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()

        loadReports()
    }

    private fun loadReports() {
        reportsList = args.extractMetricsResponse.data.pdfData
        if (reportsList.isNotEmpty()) {
            currentPosition = args.selectedPosition.coerceIn(0, reportsList.size - 1)
            reportAdapter?.updateData(reportsList, currentPosition)
            setReportInfo(reportsList[currentPosition])
            binding.rvReports.scrollToPosition(currentPosition)
        }
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.health_reports_title)
        val startSpacing = resources.getDimensionPixelSize(R.dimen._20dp)
        val itemSpacing = resources.getDimensionPixelSize(R.dimen._10dp)

        reportAdapter = ReportAdapter(emptyList()) { selectedReport ->
            currentPosition = reportsList.indexOfFirst { it.id == selectedReport.id }.coerceAtLeast(0)
            setReportInfo(selectedReport)
        }

        metricsAdapter = MetricsReportAdapter()
        binding.rvReadings.adapter = metricsAdapter

        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = reportAdapter

            addItemDecoration(object : RecyclerView.ItemDecoration() {

                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State,
                ) {
                    val position = parent.getChildAdapterPosition(view)

                    if (position == 0) {
                        outRect.left = startSpacing
                    } else {
                        outRect.left = itemSpacing
                    }
                }
            })
        }
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.metricState.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.SUCCESS -> {
                    binding.shimmerReadings.visibility = View.GONE
                    val metricsData = resource.data?.data?.metricData ?: emptyList()
                    val pdfId = resource.data?.data?.pdfData?.pdfId
                    
                    if (metricsData.isNotEmpty()) {
                        val mappedMetrics = metricsData.map {
                            MedicalPdfMetric(
                                metricName = it.metricName,
                                metricReading = it.metricReading,
                                metricUnit = null,
                                metricDate = it.metricLastReadingDate
                            )
                        }

                        // Update the local list and adapter so that the metrics are cached
                        if (pdfId != null) {
                            reportsList = reportsList.map {
                                if (it.id == pdfId) it.copy(metrics = mappedMetrics) else it
                            }
                            // Keep the same currentPosition when updating data
                            reportAdapter?.updateData(reportsList, currentPosition)
                        }

                        binding.tvExtractedSummary.text =
                            getString(R.string.data_points_extracted, mappedMetrics.size)
                        binding.rvReadings.visibility = View.VISIBLE
                        metricsAdapter?.updateData(mappedMetrics)
                    } else {
                        binding.rvReadings.visibility = View.GONE
                    }
                }
                Status.LOADING -> {
                    binding.rvReadings.visibility = View.GONE
                    binding.shimmerReadings.visibility = View.VISIBLE
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.shimmerReadings.visibility = View.GONE
                    binding.rvReadings.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    private fun setReportInfo(selectedReport: MedicalPdf) {
        binding.tvExtractedSummary.text = getString(R.string.extracting_data_points)
        binding.tvReportFilename.text = selectedReport.fileName
        val timeAgo = getTimeAgo(selectedReport.uploadedAt * 1000)
        binding.tvSyncedStatus.text = getString(R.string.lab_pdf_synced, timeAgo)

        if (!selectedReport.metrics.isNullOrEmpty()) {
            binding.tvExtractedSummary.text =
                getString(R.string.data_points_extracted, selectedReport.metrics.size)
            binding.shimmerReadings.visibility = View.GONE
            binding.rvReadings.visibility = View.VISIBLE
            metricsAdapter?.updateData(selectedReport.metrics)
        } else {
            binding.rvReadings.visibility = View.GONE
            binding.shimmerReadings.visibility = View.VISIBLE
            viewModel.generateMetricByPdfId(selectedReport.id)
        }
    }

    companion object {

    }
}