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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.humotron.app.R
import com.humotron.app.databinding.FragmentSearchingEmailReportsBinding
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchingEmailReportsFragment : Fragment(R.layout.fragment_searching_email_reports) {

    private lateinit var binding: FragmentSearchingEmailReportsBinding
    private val viewModel: BloodTestViewModel by activityViewModels()
    private val args: SearchingEmailReportsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchingEmailReportsBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        observeViewModel()
        startRetrieval()
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

    private fun startRetrieval() {
        val accountEmail = viewModel.accountEmail.value ?: GoogleSignIn.getLastSignedInAccount(
            requireContext()
        )?.email

        if (accountEmail != null) {
            viewModel.searchGmail(
                requireContext(),
                accountEmail,
                args.filters.keywords,
                emptyList(),
                args.filters.dateRange,
                args.filters.hasAttachments
            )
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_account_selected_error),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.navigateToImport.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                viewModel.onImportNavigated()
                findNavController().navigate(R.id.action_fragmentSearchingEmailReports_to_fragmentReportEmailList)
            }
        }

        viewModel.noResultsEvent.observe(viewLifecycleOwner) { hasNoResults ->
            if (hasNoResults) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_matching_pdfs_found_with_filters),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.onNoResultsShown()
                findNavController().popBackStack()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }
    }
}
