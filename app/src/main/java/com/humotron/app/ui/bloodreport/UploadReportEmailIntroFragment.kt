package com.humotron.app.ui.bloodreport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.R
import com.humotron.app.databinding.FragmentUploadReportEmailIntroBinding
import com.humotron.app.ui.bloodTest.BloodTestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadReportEmailIntroFragment : Fragment(R.layout.fragment_upload_report_email_intro) {

    private lateinit var binding: FragmentUploadReportEmailIntroBinding
    private val viewModel: BloodTestViewModel by activityViewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                viewModel.setAccountEmail(account.email)
                findNavController().navigate(R.id.action_fragmentUploadReportEmailIntro_to_fragmentEmailImportReviewPermissions)
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Sign-in failed. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUploadReportEmailIntroBinding.bind(view)
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
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnContinue.setOnClickListener {
            showOAuthDialog()
        }
    }

    private fun showOAuthDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle(getString(R.string.oauth_dialog_title))
            .setMessage(getString(R.string.oauth_dialog_desc))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string._continue)) { dialog, _ ->
                dialog.dismiss()
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
                    .build()
                val signInClient = GoogleSignIn.getClient(requireActivity(), gso)

                // Force account selection by signing out first
                signInClient.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(signInClient.signInIntent)
                }
            }
            .show()
    }

    private fun observeViewModel() {

    }

    companion object
}