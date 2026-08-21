package com.humotron.app.ui.bloodreport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.databinding.FragmentUploadEmailReportBinding
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadEmailReportFragment : Fragment(R.layout.fragment_upload_email_report) {

    private lateinit var binding: FragmentUploadEmailReportBinding
    private val viewModel: BloodTestViewModel by activityViewModels()
    private val args: UploadEmailReportFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUploadEmailReportBinding.bind(view)
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
        val selectedPdfs = args.selectedPdfs.toList()
        if (selectedPdfs.isNotEmpty()) {
            viewModel.uploadSelectedPdfs(requireContext(), selectedPdfs)
        } else {
            findNavController().popBackStack()
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
                        val action =
                            UploadEmailReportFragmentDirections.actionFragmentUploadEmailReportToFragmentPDFReportList(
                                response
                            )
                        findNavController().navigate(action)
                    }
                }

                com.humotron.app.data.network.Status.ERROR, com.humotron.app.data.network.Status.EXCEPTION -> {
                    val errorMessage = resource.error?.errorMessage ?: "Upload failed"
                    Toast.makeText(
                        requireContext(),
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.resetUploadState()
                    findNavController().popBackStack()
                }
            }
        }
    }

    companion object {

    }
}
