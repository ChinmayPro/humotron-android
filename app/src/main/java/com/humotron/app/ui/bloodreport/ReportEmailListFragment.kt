package com.humotron.app.ui.bloodreport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentReportEmailListBinding
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import com.humotron.app.ui.bloodTest.PdfImportAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportEmailListFragment : Fragment(R.layout.fragment_report_email_list) {

    private lateinit var binding: FragmentReportEmailListBinding
    private val viewModel: BloodTestViewModel by activityViewModels()
    private lateinit var adapter: PdfImportAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReportEmailListBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initClicks()
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.email_import)
    }

    private fun setupRecyclerView() {
        adapter = PdfImportAdapter(emptyList()) { selectedCount ->
            binding.tvSelectedCountNumber.text = String.format(java.util.Locale.getDefault(), "%02d", selectedCount)
            binding.btnExtractMetrics.isEnabled = selectedCount > 0
            if (selectedCount > 0) {
                binding.btnExtractMetrics.setTextColor(resources.getColor(R.color.black, null))
                binding.btnExtractMetrics.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(resources.getColor(R.color.lime, null))
            } else {
                binding.btnExtractMetrics.setTextColor(resources.getColor(R.color.ink4, null))
                binding.btnExtractMetrics.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(resources.getColor(R.color.white10, null))
            }
        }
        binding.rvEmails.adapter = adapter
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnExtractMetrics.setOnClickListener {
            val selectedPdfs = adapter.getSelectedItems()
            if (selectedPdfs.isNotEmpty()) {
                val action = ReportEmailListFragmentDirections.actionFragmentReportEmailListToFragmentUploadEmailReport(selectedPdfs.toTypedArray())
                findNavController().navigate(action)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.pdfResults.observe(viewLifecycleOwner) { results ->
            binding.tvTotalEmail.text = (results?.size ?: 0).toString()
            adapter.updateData(results ?: emptyList())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnExtractMetrics.isEnabled =
                !isLoading && adapter.getSelectedItems().isNotEmpty()
            if (isLoading) {
                binding.btnExtractMetrics.text =
                    getString(R.string.searching)
            } else {
                binding.btnExtractMetrics.text = getString(R.string.extract_metrics)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }
}
