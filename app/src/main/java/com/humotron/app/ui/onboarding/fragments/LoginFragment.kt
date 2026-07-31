package com.humotron.app.ui.onboarding.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.humotron.app.R
import com.humotron.app.core.AppConstant
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentLoginBinding
import com.humotron.app.domain.modal.param.LoginParam
import com.humotron.app.domain.modal.param.SendOtpParam
import com.humotron.app.ui.MainActivity
import com.humotron.app.ui.onboarding.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    private var isLoginMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.etEmail.setText(prefUtils.getString(Preference.LOGIN_USER_EMAIL) ?: "")

        val termsHtml = "By continuing you agree to our <font color='#C4F23E'>Terms</font> and <font color='#C4F23E'>Privacy Policy</font>."
        binding.tvTerms.text = HtmlCompat.fromHtml(termsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

        binding.rgAuthType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbLogin) {
                isLoginMode = true
                binding.tvTitle.text = getString(R.string.welcome_back)
                binding.tvSubTitle.text = getString(R.string.pick_up_exactly)
            } else {
                isLoginMode = false
                binding.tvTitle.text = getString(R.string.start_your_read)
                binding.tvSubTitle.text = getString(R.string.two_minutes_to_set_up)
            }
        }

        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (isLoginMode) {
                viewModel.loginUser(
                    LoginParam(
                        userType = "USER",
                        mode = "NORMAL",
                        loginType = "Mobile",
                        email = email
                    )
                )
            } else {
                viewModel.sendOtp(SendOtpParam(email))
            }
        }

        binding.btnGoogle.setOnClickListener {
            performGoogleSignIn()
        }

        subscribeToObserver()
    }

    private fun performGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(AppConstant.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(requireContext())

        lifecycleScope.launch {
            try {
                showProgress()
                val result = credentialManager.getCredential(requireContext(), request)
                when (val credential = result.credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            val email = googleIdTokenCredential.id

                            viewModel.loginWithGoogle(
                                LoginParam(
                                    email = email,
                                    googleToken = idToken,
                                    loginType = "Google",
                                    userType = "USER",
                                    mode = "NORMAL"
                                )
                            )
                        } else {
                            hideProgress()
                            Toast.makeText(requireContext(), "Unexpected credential type", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        hideProgress()
                        Toast.makeText(requireContext(), "Unrecognized credential", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                hideProgress()
                Log.d("LoginFragment", "Google Sign-In cancelled by user")
            } catch (e: Exception) {
                hideProgress()
                Log.e("LoginFragment", "Google Sign-In error", e)
                Toast.makeText(requireContext(), e.localizedMessage ?: "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun subscribeToObserver() {
        viewModel._validationError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loginData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data?.data
                    if (data?.token != null) {
                        prefUtils.setAuthToken(data.token ?: "")
                        prefUtils.setString(Preference.LOGIN_USER_EMAIL, data.user?.email ?: "")
                        data.user?.let { prefUtils.setLoginResponse(it) }

                        if (!prefUtils.getBoolean(Preference.ONBOARD_PRIVACY)) {
                            findNavController().navigate(R.id.onBoardPrivacyFragment)
                        } else if (data.user?.isOnBoarding == true) {
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finish()
                        } else {
                            val user = data.user
                            if (user?.name.isNullOrEmpty()) {
                                findNavController().navigate(R.id.personalizeFragment)
                            } else if (user.height.isNullOrEmpty()) {
                                val bundle = Bundle().apply {
                                    putInt("position", 1)
                                }
                                findNavController().navigate(R.id.personalizeFragment, bundle)
                            } else {
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                requireActivity().finish()
                            }
                        }
                    } else {
                        viewModel.sendOtp(SendOtpParam(binding.etEmail.text.toString()))
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                }
                Status.EXCEPTION -> {
                    hideProgress()
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.sendOtp().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val bundle = Bundle().apply {
                        putString("email", binding.etEmail.text.toString())
                    }
                    findNavController().navigate(R.id.verifyOtpFragment, bundle)
                }
                Status.ERROR -> {
                    hideProgress()
                }
                Status.EXCEPTION -> {
                    hideProgress()
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }
}