package com.humotron.app.ui.onboarding.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.humotron.app.databinding.FragmentVerifyOtpBinding
import com.humotron.app.domain.modal.param.SendOtpParam
import com.humotron.app.domain.modal.param.VerifyOtpParam
import com.humotron.app.ui.MainActivity
import com.humotron.app.ui.onboarding.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VerifyOtpFragment : BaseFragment(R.layout.fragment_verify_otp) {

    private lateinit var binding: FragmentVerifyOtpBinding
    private val viewModel: LoginViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null

    private val totalTime = 30000L // 60 seconds
    private val interval = 1000L // 1 second

    companion object {
        var timerStart: Long? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVerifyOtpBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (timerStart == null) {
            timerStart = System.currentTimeMillis()
        }

        startResendTimer()

        val email = arguments?.getString("email") ?: ""
        binding.tvDesc.text = getString(R.string.we_ve_sent_a_6_digit_code_to_s, email)
        binding.otpView.setOnFinishListener {
            viewModel.verifyOtp(VerifyOtpParam(email, it))
        }

        binding.tvTimer.setOnClickListener {
            if (binding.tvTimer.text == getString(R.string.resend)) {
                viewModel.sendOtp(SendOtpParam(email))
            }
        }

        binding.tvEdit.setOnClickListener {
            findNavController().popBackStack()
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver() {
        viewModel.verifyOtpData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data ?: return@observe
                    if (data.data != null) {
                        prefUtils.setAuthToken(data.data.token ?: "")
                        prefUtils.setString(
                            Preference.LOGIN_USER_EMAIL,
                            data.data.user?.email ?: ""
                        )
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
                    Toast.makeText(
                        requireContext(),
                        networkStatus.error?.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
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
                    startResendTimer()

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

    private fun startResendTimer() {
        countDownTimer?.cancel() // cancel if already running
        countDownTimer = object : CountDownTimer(totalTime, interval) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                binding.tvTimer.text = getString(R.string.send_again_s, formattedTime)
            }

            override fun onFinish() {
                binding.tvTimer.text = getString(R.string.resend)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()

    }


}