package com.humotron.app.ui.onboarding.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentLoginMethodBinding
import com.humotron.app.domain.modal.param.LoginParam
import com.humotron.app.ui.MainActivity
import com.humotron.app.ui.onboarding.fragments.GoogleAuth.Companion.googleSignIn
import com.humotron.app.ui.onboarding.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginMethodFragment : BaseFragment(R.layout.fragment_login_method) {

    private lateinit var binding: FragmentLoginMethodBinding
    private val viewModel: LoginViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginMethodBinding.bind(view)

        binding.btnEmail.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        binding.btnGoogle.setOnClickListener {
            googleSignIn(requireContext(), lifecycleScope) { token ->
                viewModel.loginWithGoogle(
                    LoginParam(
                        mode = "GOOGLE",
                        googleToken = token,
                        platform = "android"
                    )
                )
            }
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver() {
        viewModel.loginData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
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


class GoogleAuth {

    companion object {
        fun googleSignIn(
            context: Context,
            scope: CoroutineScope,
            onLogin: (String) -> Unit
        ) {

            val credentialManager = CredentialManager.create(context)

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions())
                .build()

            scope.launch {
                try {

                    val result = credentialManager.getCredential(context, request)
                    when (result.credential) {
                        is CustomCredential -> {
                            if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(result.credential.data)
                                val googleTokenId = googleIdTokenCredential.idToken
                                onLogin(googleTokenId)

                            }
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "googleSignIn: ", e)

                }
            }
        }

        /*fun getCredentialOptions(): CredentialOption {

            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId("376945095750-qg9easanekribira9ljd4r01q25kpu1c.apps.googleusercontent.com")
                .build()
        }*/

        fun getCredentialOptions(): GetGoogleIdOption {
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("YOUR_SERVER_CLIENT_ID")
                .build()
        }
    }
}