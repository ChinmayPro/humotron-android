package com.humotron.app.ui.bloodreport

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.humotron.app.R
import com.humotron.app.domain.modal.GmailSearchFilters
import com.humotron.app.databinding.FragmentEmailImportReviewPermissionsBinding
import com.humotron.app.databinding.ItemBloodTestChipSelectedBinding
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import com.humotron.app.ui.bloodTest.dialog.SingleSelectBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailImportReviewPermissionsFragment :
    Fragment(R.layout.fragment_email_import_review_permissions) {

    private lateinit var binding: FragmentEmailImportReviewPermissionsBinding
    private val viewModel: BloodTestViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEmailImportReviewPermissionsBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initClicks()
        observeViewModel()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.email_import)
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardFilterKeywords.setOnClickListener {
            showAddKeywordDialog()
        }

        binding.cardFilterAttachments.setOnClickListener {
            showAttachmentsPicker()
        }

        binding.cardFilterDateRange.setOnClickListener {
            showDateRangePicker()
        }

        binding.btnStartRetrieval.setOnClickListener {
            startRetrieval()
        }
    }

    private fun observeViewModel() {
        // Observers for loading and navigation are now handled in SearchingEmailReportsFragment
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAddKeywordDialog() {
        val editText = android.widget.EditText(requireContext())
        editText.hint = getString(R.string.enter_keywords)
        val padding = resources.getDimensionPixelSize(R.dimen._20dp)

        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(padding, padding / 2, padding, 0)
        editText.layoutParams = params
        container.addView(editText)

        AlertDialog.Builder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(getString(R.string.keywords))
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val keyword = editText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    addChip(keyword)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addChip(text: String) {
        val chipBinding = ItemBloodTestChipSelectedBinding.inflate(layoutInflater)
        chipBinding.tvChipLabel.text = text

        val chipView = chipBinding.root

        chipBinding.btnChipRemove.setOnClickListener {
            binding.cgKeywords.removeView(chipView)
            updateChipsVisibility()
        }

        binding.cgKeywords.addView(chipView, 0)
        updateChipsVisibility()
    }

    private fun updateChipsVisibility() {
        binding.hsvChips.visibility =
            if (binding.cgKeywords.childCount > 0) View.VISIBLE else View.GONE
    }

    private fun showAttachmentsPicker() {
        val options = listOf(getString(R.string.yes), getString(R.string.no))
        val current = binding.tvAttachmentsValue.text.toString()

        val bottomSheet = SingleSelectBottomSheet.newInstance(
            title = getString(R.string.attachments),
            subtitle = getString(R.string.attachments_question),
            options = options,
            selectedItem = current
        ) { selected ->
            binding.tvAttachmentsValue.text = selected
        }
        bottomSheet.show(childFragmentManager, SingleSelectBottomSheet.TAG)
    }

    private fun showDateRangePicker() {
        val options = listOf(
            getString(R.string.past_2_years),
            getString(R.string.past_2_5_years),
            getString(R.string.past_5_10_years),
            getString(R.string.past_10_more_years)
        )
        val current = binding.tvDateRangeValue.text.toString()

        val bottomSheet = SingleSelectBottomSheet.newInstance(
            title = getString(R.string.date_range_label),
            subtitle = getString(R.string.date_range_picker_subtitle),
            options = options,
            selectedItem = current
        ) { selected ->
            binding.tvDateRangeValue.text = selected
        }
        bottomSheet.show(childFragmentManager, SingleSelectBottomSheet.TAG)
    }

    private fun startRetrieval() {
        val keywords = mutableListOf<String>()
        for (i in 0 until binding.cgKeywords.childCount) {
            val chipBinding =
                ItemBloodTestChipSelectedBinding.bind(binding.cgKeywords.getChildAt(i))
            keywords.add(chipBinding.tvChipLabel.text.toString())
        }

        val hasAttachments = binding.tvAttachmentsValue.text.toString() == getString(R.string.yes)
        val dateRange = binding.tvDateRangeValue.text.toString()

        val filters = GmailSearchFilters(
            keywords = keywords,
            hasAttachments = hasAttachments,
            dateRange = dateRange
        )

        val action = EmailImportReviewPermissionsFragmentDirections
            .actionFragmentEmailImportReviewPermissionsToFragmentSearchingEmailReports(filters)
        findNavController().navigate(action)
    }
}
