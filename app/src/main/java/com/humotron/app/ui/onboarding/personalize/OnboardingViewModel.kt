package com.humotron.app.ui.onboarding.personalize

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.repository.OnBoardingRepository
import com.humotron.app.domain.modal.param.CompleteOnboardingParam
import com.humotron.app.domain.modal.param.SubmitPersonalInfoParam
import com.humotron.app.domain.modal.param.WeightHeightParam
import com.humotron.app.domain.modal.response.UseCaseResponse
import com.humotron.app.domain.modal.response.VerifyOtpResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnBoardingRepository,
) : ViewModel() {


    private val onBoardingLiveData: MutableLiveData<Resource<VerifyOtpResponse>> =
        MutableLiveData()

    fun onBoardingData(): LiveData<Resource<VerifyOtpResponse>> {
        return onBoardingLiveData
    }

    val _validationError: MutableLiveData<String> = MutableLiveData()


    fun submitPersonalInfo(param: SubmitPersonalInfoParam) {
        if (isValidate(param)) {
            repository.submitPersonalInfo(param).onEach { state ->
                onBoardingLiveData.value = state
            }.launchIn(viewModelScope)
        }
    }

    private fun isValidate(
        param: SubmitPersonalInfoParam
    ): Boolean {
        return when {
            param.name.isNullOrEmpty() -> {
                _validationError.value = "Please Enter Name"
                return false
            }

            param.birthdate.isNullOrEmpty() -> {
                _validationError.value = "Please select birth date"
                return false
            }

            param.gender.isNullOrEmpty() -> {
                _validationError.value = "Please select gender"
                return false
            }

            else -> true
        }
    }

    fun submitWeightHeight(param: WeightHeightParam) {
        repository.submitWeightHeight(param).onEach { state ->
            onBoardingLiveData.value = state
        }.launchIn(viewModelScope)
    }


    private val useCaseLiveData: MutableLiveData<Resource<UseCaseResponse>> =
        MutableLiveData()

    fun useCaseData(): LiveData<Resource<UseCaseResponse>> {
        return useCaseLiveData
    }

    fun getInterests() {
        repository.getInterests().onEach { state ->
            useCaseLiveData.value = state
        }.launchIn(viewModelScope)
    }

    fun completeOnboarding(param: CompleteOnboardingParam) {
        repository.completeOnboarding(param).onEach { state ->
            onBoardingLiveData.value = state
        }.launchIn(viewModelScope)
    }
}