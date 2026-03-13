package com.humotron.app.data.repository

import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.ResponseHandler
import com.humotron.app.data.network.exceptions.ValidationException
import com.humotron.app.data.remote.AppApi
import com.humotron.app.domain.modal.param.CompleteOnboardingParam
import com.humotron.app.domain.modal.param.SubmitPersonalInfoParam
import com.humotron.app.domain.modal.param.WeightHeightParam
import com.humotron.app.domain.modal.response.UseCaseResponse
import com.humotron.app.domain.modal.response.VerifyOtpResponse
import com.humotron.app.util.PrefUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class OnBoardingRepository(
    private val api: AppApi,
    private val responseHandler: ResponseHandler,
    private val prefUtils: PrefUtils,
) {


    fun submitPersonalInfo(param: SubmitPersonalInfoParam): Flow<Resource<VerifyOtpResponse>> =
        flow {
            emit(Resource.loading())
            try {
                val response =
                    responseHandler.handleResponse(
                        api.submitPersonalInfo(
                            prefUtils.getLoginResponse().id!!,
                            param
                        ), false
                    )

                emit(response)
            } catch (e: Exception) {
                emit(responseHandler.handleException(e))
                e.printStackTrace()
            }
        }.catch {
            emit(responseHandler.handleException(ValidationException(it.message)))
        }


    fun submitWeightHeight(param: WeightHeightParam): Flow<Resource<VerifyOtpResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.submitWeightHeight(
                        prefUtils.getLoginResponse().id!!,
                        param
                    ), false
                )

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun getInterests(): Flow<Resource<UseCaseResponse>> = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.getInterests(), false
                )

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }

    fun completeOnboarding(param: CompleteOnboardingParam) = flow {
        emit(Resource.loading())
        try {
            val response =
                responseHandler.handleResponse(
                    api.completeOnboarding(
                        prefUtils.getLoginResponse().id!!,
                        param
                    ), false
                )

            emit(response)
        } catch (e: Exception) {
            emit(responseHandler.handleException(e))
            e.printStackTrace()
        }
    }.catch {
        emit(responseHandler.handleException(ValidationException(it.message)))
    }


}