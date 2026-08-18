package com.humotron.app.ui.bloodreport

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentUploadPdfReportBinding
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadPDFReportFragment : Fragment(R.layout.fragment_upload_pdf_report) {

    private lateinit var binding: FragmentUploadPdfReportBinding
    private val viewModel: BloodTestViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUploadPdfReportBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()

        startUpload()
    }

    private fun startUpload() {
        val results = viewModel.pdfResults.value
        if (!results.isNullOrEmpty()) {
            viewModel.uploadSelectedPdfs(requireContext(), results)
        }
    }

    private fun initViews() {
    }

    private fun initClicks() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing → Back is disabled for this Fragment
                }
            }
        )
    }

    private fun observeViewModel() {
        viewModel.uploadState.observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe

            when (resource.status) {
                com.humotron.app.data.network.Status.LOADING -> {
                    // Show progress if needed
                }

                com.humotron.app.data.network.Status.SUCCESS -> {
                    resource.data?.let { response ->
                        val action = UploadPDFReportFragmentDirections.actionFragmentUploadPDFReportToFragmentPDFReportList(response)
                        findNavController().navigate(action)
                    }
                }

                com.humotron.app.data.network.Status.ERROR -> {
                    val errorMessage = resource.error?.errorMessage ?: "Upload failed"
                    android.widget.Toast.makeText(
                        requireContext(),
                        errorMessage,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetUploadState()
                    findNavController().popBackStack()
                }

                else -> {

                }
            }
        }
    }

    companion object {

    }
}