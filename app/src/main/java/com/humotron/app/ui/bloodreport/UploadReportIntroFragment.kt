package com.humotron.app.ui.bloodreport

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentUploadReportIntroBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadReportIntroFragment : Fragment(R.layout.fragment_upload_report_intro) {

    private lateinit var binding: FragmentUploadReportIntroBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUploadReportIntroBinding.bind(view)
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
        binding.header.tvTitle.text = getString(R.string.upload_blood_test)
        setupNudgeText()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.cardUploadPdf.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentUploadReportIntro_to_fragmentUploadPDFFromDevice)
        }
        binding.cardImportEmail.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentUploadReportIntro_to_fragmentUploadReportEmailIntro)
        }
        binding.llDeepScanNudge.setOnClickListener {

        }
    }

    private fun observeViewModel() {

    }

    private fun setupNudgeText() {
        val fullText = getString(R.string.nudge_deepscan_upload)
        val boldPart = "No recent report?"
        val spannable = SpannableString(fullText)

        val boldStart = fullText.indexOf(boldPart)
        if (boldStart != -1) {
            val boldEnd = boldStart + boldPart.length

            // Bold and White for "Want hard numbers too?"
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                boldStart,
                boldEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white)),
                boldStart,
                boldEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Normal and Ink2 for the rest
            if (boldEnd < fullText.length) {
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.ink2)),
                    boldEnd,
                    fullText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        binding.tvNudgeText.text = spannable
    }

    companion object {

    }
}