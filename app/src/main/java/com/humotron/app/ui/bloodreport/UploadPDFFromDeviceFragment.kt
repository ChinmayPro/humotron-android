package com.humotron.app.ui.bloodreport

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.humotron.app.R
import com.humotron.app.databinding.FragmentUploadPdfFromDeviceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadPDFFromDeviceFragment : Fragment(R.layout.fragment_upload_pdf_from_device) {

    private lateinit var binding: FragmentUploadPdfFromDeviceBinding

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
        binding.header.tvTitle.text = getString(R.string.track_connect_wearable_title)
    }

    private fun initClicks() {
        binding.btnChooseDevice.setOnClickListener {

        }
    }

    private fun observeViewModel() {

    }

    companion object {

    }
}