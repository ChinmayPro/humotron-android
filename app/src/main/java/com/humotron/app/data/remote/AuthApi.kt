package com.humotron.app.data.remote

import com.humotron.app.domain.modal.param.LoginParam
import com.humotron.app.domain.modal.param.SendOtpParam
import com.humotron.app.domain.modal.param.VerifyOtpParam
import com.humotron.app.domain.modal.response.LoginResponse
import com.humotron.app.domain.modal.response.RegisterUserResponse
import com.humotron.app.domain.modal.response.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("user/loginUser")
    suspend fun loginUser(@Body param: LoginParam): Response<LoginResponse>

    @POST("user/verifyUser")
    suspend fun sendOtp(@Body param: SendOtpParam): Response<RegisterUserResponse>

    @POST("user/verifyUserOtp")
    suspend fun verifyOtp(@Body param: VerifyOtpParam): Response<VerifyOtpResponse>
}