package com.humotron.app.ui.bloodreport

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentUploadPdfFromDeviceBinding
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadPDFFromDeviceFragment : Fragment(R.layout.fragment_upload_pdf_from_device) {

    private lateinit var binding: FragmentUploadPdfFromDeviceBinding
    private val viewModel: BloodTestViewModel by activityViewModels()

    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.setDevicePdfs(uris, requireContext())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUploadPdfFromDeviceBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.device_upload)
    }

    private fun initClicks() {
        binding.mcvUploadDeviceFilePicker.setOnClickListener {
            pdfPickerLauncher.launch("application/pdf")
        }
    }

    private fun observeViewModel() {
        viewModel.navigateToImport.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                viewModel.onImportNavigated()
                findNavController().navigate(R.id.action_fragmentUploadPDFFromDevice_to_fragmentUploadPDFReport)
            }
        }
    }

    companion object {

    }
}