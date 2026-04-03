package com.humotron.app.ui.bloodTest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.humotron.app.R
import com.humotron.app.databinding.FragmentBloodTestGmailInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BloodTestGmailInfoFragment : Fragment() {

    private var _binding: FragmentBloodTestGmailInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BloodTestViewModel by activityViewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null) {
                android.util.Log.d("GmailAuth", "Sign-in successful: ${account.email}")
                viewModel.setAccountEmail(account.email)
                findNavController().navigate(R.id.action_fragmentBloodTestGmailInfo_to_fragmentBloodTestEmailSearch)
            }
        } catch (e: Exception) {
            android.util.Log.e("GmailAuth", "Sign-in failed: ${e.message}")
            android.widget.Toast.makeText(requireContext(), "Sign-in failed. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBloodTestGmailInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar()
        applyInsets()
        initClicks()
    }

    private fun setupStatusBar() {
        requireActivity().window.apply {
            statusBarColor = Color.BLACK
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = false
        }
    }

    private fun initClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnContinue.setOnClickListener {
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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, if (ime.bottom > bars.bottom) ime.bottom else bars.bottom)
            insets
        }
    }
}
