package com.humotron.app.ui.bloodTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.databinding.FragmentUploadedReportsBinding
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.domain.modal.response.GenerateMetricResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadedReportsFragment : Fragment() {

    private var _binding: FragmentUploadedReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BloodTestViewModel by activityViewModels()
    private var adapter: UploadedReportAdapter? = null
    private var keyReadingAdapter: KeyReadingAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadedReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar()
        initViews()
        observeViewModel()
    }

    private fun setupStatusBar() {
        requireActivity().window.apply {
            statusBarColor = android.graphics.Color.BLACK
            androidx.core.view.WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = false
        }
    }



    private fun initViews() {
        // Handle status bar insets only if NOT opened from TrackFragment
        val isFromTrack = arguments?.getBoolean("isFromTrack") ?: false
        if (!isFromTrack) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
                val statusBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                val params = view.layoutParams as android.view.ViewGroup.MarginLayoutParams
                params.topMargin = statusBars.top
                view.layoutParams = params
                insets
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            if (findNavController().previousBackStackEntry == null) {
                requireActivity().finish()
            } else {
                findNavController().navigateUp()
            }
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = UploadedReportAdapter(emptyList()) { item ->
            // Carousel item click handled here
            item.pdfId?.let { pdfId ->
                viewModel.generateMetricByPdfId(pdfId)
            }
        }
        binding.dsvUploadedReports.adapter = adapter
        binding.dsvUploadedReports.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build()
        )
        
        binding.dsvUploadedReports.addOnItemChangedListener { viewHolder, adapterPosition ->
            if (adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                val pdf = adapter?.getItem(adapterPosition)
                pdf?.pdfId?.let { pdfId ->
                    viewModel.generateMetricByPdfId(pdfId)
                }
            }
        }

        keyReadingAdapter = KeyReadingAdapter()
        binding.rvKeyReadings.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvKeyReadings.adapter = keyReadingAdapter
    }

    private fun observeViewModel() {
        viewModel.uploadState.observe(viewLifecycleOwner) { resource ->
            if (resource?.status == Status.SUCCESS) {
                val pdfList = resource.data?.data?.pdfData ?: emptyList()
                if (pdfList.isNotEmpty()) {
                    adapter?.updateData(pdfList)

                    val initialIndex = viewModel.selectedReportIndex.value ?: 0
                    if (initialIndex < pdfList.size) {
                        binding.dsvUploadedReports.scrollToPosition(initialIndex)
                        
                        // Automatically trigger parsing for the initially selected report
                        pdfList[initialIndex].pdfId?.let { pdfId ->
                            viewModel.generateMetricByPdfId(pdfId)
                        }
                    }
                }
            }
        }

        viewModel.metricState.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.SUCCESS -> {
                    binding.shimmerKeyReadings.stopShimmer()
                    binding.shimmerKeyReadings.visibility = View.GONE
                    
                    val metrics = resource.data?.data?.metricData ?: emptyList()
                    keyReadingAdapter?.updateData(metrics)
                    
                    binding.tvKeyReadingsTitle.visibility = View.VISIBLE
                    binding.tvKeyReadingsDesc.visibility = View.VISIBLE
                    binding.rvKeyReadings.visibility = if (metrics.isNotEmpty()) View.VISIBLE else View.GONE
                }
                Status.LOADING -> {
                    binding.tvKeyReadingsTitle.visibility = View.VISIBLE
                    binding.tvKeyReadingsDesc.visibility = View.VISIBLE
                    binding.rvKeyReadings.visibility = View.GONE
                    binding.shimmerKeyReadings.visibility = View.VISIBLE
                    binding.shimmerKeyReadings.startShimmer()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.shimmerKeyReadings.stopShimmer()
                    binding.shimmerKeyReadings.visibility = View.GONE
                    keyReadingAdapter?.updateData(emptyList())
                    binding.tvKeyReadingsTitle.visibility = View.VISIBLE
                    binding.tvKeyReadingsDesc.visibility = View.VISIBLE
                    binding.rvKeyReadings.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

