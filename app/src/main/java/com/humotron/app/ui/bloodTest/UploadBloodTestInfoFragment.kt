package com.humotron.app.ui.bloodTest

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentUploadBloodtestInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.activityViewModels
import androidx.activity.result.contract.ActivityResultContracts

@AndroidEntryPoint
class UploadBloodTestInfoFragment : BaseFragment(R.layout.fragment_upload_bloodtest_info) {

    private lateinit var binding: FragmentUploadBloodtestInfoBinding
    private val viewModel: BloodTestViewModel by activityViewModels()

    private val pdfPickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.setDevicePdfs(uris, requireContext())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar()
        binding = FragmentUploadBloodtestInfoBinding.bind(view)
        applyInsets()
        initClicks()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigateToImport.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                viewModel.onImportNavigated()
                findNavController().navigate(R.id.action_fragmentUploadBloodTestInfo_to_fragmentBloodTestEmailImport)
            }
        }
    }

    private fun setupStatusBar() {
        requireActivity().window.apply {
            statusBarColor = Color.BLACK
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = false
        }
    }

    private fun initClicks() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContinue.setOnClickListener {
            val bottomSheet = com.humotron.app.ui.bloodTest.dialog.ChooseUploadMethodBottomSheet()
            bottomSheet.setMethodSelectionListener { method ->
                if (method == "email") {
                    findNavController().navigate(R.id.action_fragmentUploadBloodTestInfo_to_fragmentBloodTestGmailInfo)
                } else if (method == "device") {
                    pdfPickerLauncher.launch("application/pdf")
                }
            }
            bottomSheet.show(childFragmentManager, com.humotron.app.ui.bloodTest.dialog.ChooseUploadMethodBottomSheet.TAG)
        }

        binding.tvFooter.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentUploadBloodTestInfo_to_fragmentBloodTestInfo)
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
