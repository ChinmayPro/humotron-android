package com.humotron.app.ui.onboarding.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.humotron.app.BuildConfig
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentLoginMethodBinding
import com.humotron.app.domain.modal.param.LoginParam
import com.humotron.app.ui.MainActivity
import com.humotron.app.ui.onboarding.OAuth
import com.humotron.app.ui.onboarding.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginMethodFragment : BaseFragment(R.layout.fragment_login_method) {

    private lateinit var binding: FragmentLoginMethodBinding
    private val viewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val serverAuthCode = account?.serverAuthCode
            if (serverAuthCode != null) {
                exchangeCodeForToken(serverAuthCode)
            } else {
                setGoogleLoading(false)
                Toast.makeText(requireContext(), "Failed to get Server Auth Code", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            setGoogleLoading(false)
            Log.e("GoogleAuth", "Sign-in failed: ${e.message}")
            if (e.statusCode != 12501) { // 12501 is SIGN_IN_CANCELLED
                Toast.makeText(requireContext(), "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setGoogleLoading(isLoading: Boolean) {
        binding.btnGoogle.isEnabled = !isLoading
        binding.tvGoogle.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.pbGoogle.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun exchangeCodeForToken(serverAuthCode: String) {
        lifecycleScope.launch {
            try {
                val response = OAuth.googleAuthApi.getAccessToken(
                    code = serverAuthCode,
                    clientId = BuildConfig.GOOGLE_CLIENT_ID,
                    clientSecret = BuildConfig.GOOGLE_CLIENT_SECRET,
                    redirectUri = ""
                )
                viewModel.loginWithGoogle(
                    LoginParam(
                        mode = "GOOGLE",
                        googleToken = response.accessToken,
                        platform = "android"
                    )
                )
            } catch (e: Exception) {
                setGoogleLoading(false)
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("GoogleAuth", "Token exchange failed (HTTP ${e.code()}): $errorBody")
                    Toast.makeText(requireContext(), "Token exchange failed: $errorBody", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("GoogleAuth", "Token exchange failed: ", e)
                    Toast.makeText(requireContext(), "Token exchange failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginMethodBinding.bind(view)

        binding.btnEmail.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        binding.btnGoogle.setOnClickListener {
            setGoogleLoading(true)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode(BuildConfig.GOOGLE_CLIENT_ID)
                .build()
            
            val signInClient = GoogleSignIn.getClient(requireActivity(), gso)
            
            // Sign out first to force account selection
            signInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(signInClient.signInIntent)
            }
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver() {
        viewModel.loginData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    setGoogleLoading(false)
                    hideProgress()
                    val data = networkStatus.data ?: return@observe
                    if (data.data != null) {
                        prefUtils.setAuthToken(data.data.token ?: "")
                        data.data.user?.let { prefUtils.setLoginResponse(it) }
                        if (data.data.user?.isOnBoarding == true) {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finish()
                        } else {
                            val user = data.data.user
                            if (user?.name.isNullOrEmpty()) {
                                findNavController().navigate(R.id.accountCreateFragment)
                            } else if (user.height.isNullOrEmpty()) {
                                val bundle = Bundle().apply {
                                    putInt("position", 1)
                                }
                                findNavController().navigate(R.id.personalizeFragment, bundle)
                            } else if (user.isOnBoarding == false) {
                                val bundle = Bundle().apply {
                                    putInt("position", 3)
                                }
                                findNavController().navigate(R.id.personalizeFragment, bundle)
                            }
                        }

                    }
                }

                Status.ERROR -> {
                    setGoogleLoading(false)
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    setGoogleLoading(false)
                    hideProgress()
                }

                Status.LOADING -> {
                    // showProgress()
                }
            }
        }
    }
}

