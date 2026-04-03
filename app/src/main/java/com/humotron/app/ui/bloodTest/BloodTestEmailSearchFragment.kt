package com.humotron.app.ui.bloodTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.humotron.app.R
import com.humotron.app.databinding.FragmentBloodTestEmailSearchBinding
import com.humotron.app.databinding.ItemBloodTestChipSelectedBinding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

@AndroidEntryPoint
class BloodTestEmailSearchFragment : Fragment() {

    private var _binding: FragmentBloodTestEmailSearchBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BloodTestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBloodTestEmailSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            v.setPadding(bars.left, bars.top, bars.right, if (ime.bottom > bars.bottom) ime.bottom else bars.bottom)
            
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!isImeVisible) {
                binding.etKeywords.clearFocus()
                binding.etLabels.clearFocus()
            }
            
            insets
        }
        
        setupUI()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // You might want to add a ProgressBar to fragment_blood_test_email_search.xml
                // For now, we'll just log and could potentially disable the button
                binding.btnStartRetrieval.isEnabled = false
                binding.btnStartRetrieval.text = getString(R.string.searching)
            } else {
                binding.btnStartRetrieval.isEnabled = true
                binding.btnStartRetrieval.text = getString(R.string.start_retrieval)
            }
        }

        viewModel.navigateToImport.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                viewModel.onImportNavigated()
                findNavController().navigate(R.id.action_fragmentBloodTestEmailSearch_to_fragmentBloodTestEmailImport)
            }
        }

        viewModel.noResultsEvent.observe(viewLifecycleOwner) { hasNoResults ->
            if (hasNoResults) {
                android.widget.Toast.makeText(requireContext(), getString(R.string.no_matching_pdfs_found_with_filters), android.widget.Toast.LENGTH_LONG).show()
                viewModel.onNoResultsShown()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddKeyword.setOnClickListener {
            val keyword = binding.etKeywords.text.toString().trim()
            if (keyword.isNotEmpty()) {
                addChip(keyword, true)
                binding.etKeywords.text.clear()
            }
            hideKeyboard()
        }

        binding.btnAddLabel.setOnClickListener {
            val label = binding.etLabels.text.toString().trim()
            if (label.isNotEmpty()) {
                addChip(label, false)
                binding.etLabels.text.clear()
            }
            hideKeyboard()
        }

        binding.llAttachments.setOnClickListener {
            showAttachmentsPicker()
        }

        binding.llDateRange.setOnClickListener {
            showDateRangePicker()
        }

        binding.btnStartRetrieval.setOnClickListener {
            hideKeyboard()
            val keywords = mutableListOf<String>()
            for (i in 0 until binding.cgKeywords.childCount) {
                val chipBinding = ItemBloodTestChipSelectedBinding.bind(binding.cgKeywords.getChildAt(i))
                keywords.add(chipBinding.tvChipLabel.text.toString())
            }

            val labels = mutableListOf<String>()
            for (i in 0 until binding.cgLabels.childCount) {
                val chipBinding = ItemBloodTestChipSelectedBinding.bind(binding.cgLabels.getChildAt(i))
                labels.add(chipBinding.tvChipLabel.text.toString())
            }

            val hasAttachments = binding.tvAttachmentsValue.text.toString() == getString(R.string.yes)
            val dateRange = binding.tvDateRangeValue.text.toString()

            val accountEmail = viewModel.accountEmail.value ?: GoogleSignIn.getLastSignedInAccount(requireContext())?.email
            if (accountEmail != null) {
                Log.d("EmailSearch", "Starting Gmail search for account: $accountEmail")
                viewModel.searchGmail(
                    requireContext(),
                    accountEmail,
                    keywords,
                    labels,
                    dateRange,
                    hasAttachments
                )
            } else {
                android.widget.Toast.makeText(requireContext(), getString(R.string.no_account_selected_error), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAttachmentsPicker() {
        val options = listOf(getString(R.string.yes), getString(R.string.no))
        val current = binding.tvAttachmentsValue.text.toString()
        
        val bottomSheet = com.humotron.app.ui.bloodTest.dialog.SingleSelectBottomSheet.newInstance(
            title = getString(R.string.attachments),
            subtitle = getString(R.string.attachments_question),
            options = options,
            selectedItem = current
        ) { selected ->
            binding.tvAttachmentsValue.text = selected
        }
        bottomSheet.show(childFragmentManager, com.humotron.app.ui.bloodTest.dialog.SingleSelectBottomSheet.TAG)
    }

    private fun showDateRangePicker() {
        val options = listOf(
            getString(R.string.past_2_years),
            getString(R.string.past_2_5_years),
            getString(R.string.past_5_10_years),
            getString(R.string.past_10_more_years)
        )
        val current = binding.tvDateRangeValue.text.toString()

        val bottomSheet = com.humotron.app.ui.bloodTest.dialog.SingleSelectBottomSheet.newInstance(
            title = getString(R.string.date_range_label),
            subtitle = getString(R.string.date_range_picker_subtitle),
            options = options,
            selectedItem = current
        ) { selected ->
            binding.tvDateRangeValue.text = selected
        }
        bottomSheet.show(childFragmentManager, com.humotron.app.ui.bloodTest.dialog.SingleSelectBottomSheet.TAG)
    }

    private fun addChip(text: String, isKeyword: Boolean) {
        val chipBinding = ItemBloodTestChipSelectedBinding.inflate(layoutInflater)
        chipBinding.tvChipLabel.text = text
        
        val chipView = chipBinding.root
        val targetGroup = if (isKeyword) binding.cgKeywords else binding.cgLabels
        
        chipBinding.btnChipRemove.setOnClickListener {
            targetGroup.removeView(chipView)
        }
        
        targetGroup.addView(chipView, 0)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
