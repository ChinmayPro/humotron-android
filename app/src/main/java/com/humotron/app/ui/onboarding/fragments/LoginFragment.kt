package com.humotron.app.ui.onboarding.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentLoginBinding
import com.humotron.app.domain.modal.param.LoginParam
import com.humotron.app.domain.modal.param.SendOtpParam
import com.humotron.app.ui.onboarding.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.etEmail.setText(prefUtils.getString(Preference.LOGIN_USER_EMAIL) ?: "")

        binding.btnSubmit.setOnClickListener {
            viewModel.loginUser(
                LoginParam(
                    userType = "USER",
                    mode = "NORMAL",
                    loginType = "Mobile",
                    email = binding.etEmail.text.toString(),
                )
            )
        }

        binding.btnRegister.setOnClickListener {
            findNavController().popBackStack(R.id.registerFragment, false)
        }
        binding.btnLoginOption.setOnClickListener {
            findNavController().popBackStack(R.id.loginMethodFragment, false)
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver() {
        viewModel._validationError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loginData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    viewModel.sendOtp(SendOtpParam(binding.etEmail.text.toString()))

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