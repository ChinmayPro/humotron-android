package com.humotron.app.ui.onboarding.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.AuthRepository
import com.humotron.app.domain.modal.param.LoginParam
import com.humotron.app.domain.modal.param.SendOtpParam
import com.humotron.app.domain.modal.param.VerifyOtpParam
import com.humotron.app.domain.modal.response.LoginResponse
import com.humotron.app.domain.modal.response.VerifyOtpResponse
import com.humotron.app.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val loginLiveData: SingleLiveEvent<Resource<LoginResponse>> =
        SingleLiveEvent()

    fun loginData(): SingleLiveEvent<Resource<LoginResponse>> {
        return loginLiveData
    }

    val _validationError: MutableLiveData<String> = MutableLiveData()

    fun loginUser(loginParam: LoginParam) {
        viewModelScope.launch {
            if (isValidate(loginParam)) {
                repository.loginUser(loginParam).onEach { state ->
                    loginLiveData.value = state
                }.launchIn(viewModelScope)
            }
        }
    }


    fun loginWithGoogle(loginParam: LoginParam) {
        viewModelScope.launch {
            repository.loginUser(loginParam).onEach { state ->
                loginLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    fun isValidate(loginParam: LoginParam): Boolean {
        return when {
            loginParam.email.isNullOrEmpty() -> {
                _validationError.value = "Please Enter Email"
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(loginParam.email).matches() -> {
                _validationError.value = "Please Enter valid Email"
                return false
            }

            else -> true
        }
    }

    private val sendOtpLiveData: SingleLiveEvent<Resource<Any>> =
        SingleLiveEvent()

    fun sendOtp(): SingleLiveEvent<Resource<Any>> {
        return sendOtpLiveData
    }

    fun sendOtp(param: SendOtpParam) {
        if (isValidEmail(param)) {
            repository.sendOtp(param).onEach { state ->
                sendOtpLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    fun isValidEmail(param: SendOtpParam): Boolean {
        return when {
            param.email.isNullOrEmpty() -> {
                _validationError.value = "Please Enter Email"
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(param.email).matches() -> {
                _validationError.value = "Please Enter valid Email"
                return false
            }


            else -> true
        }
    }

    private val verifyOtpLiveData: MutableLiveData<Resource<VerifyOtpResponse>> =
        MutableLiveData()

    fun verifyOtpData(): LiveData<Resource<VerifyOtpResponse>> {
        return verifyOtpLiveData
    }

    fun verifyOtp(param: VerifyOtpParam) {
        repository.verifyOtp(param).onEach { state ->
            verifyOtpLiveData.value = state
        }.launchIn(viewModelScope)
    }


}