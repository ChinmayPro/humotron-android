package com.humotron.app.ui.onboarding.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
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