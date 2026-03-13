package com.humotron.app.domain.modal.param


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginParam(
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("mode")
    val mode: String? = null,
    @SerializedName("loginType")
    val loginType: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("userType")
    val userType: String? = null,
    @SerializedName("googleToken")
    val googleToken: String? = null,
    @SerializedName("platform")
    val platform: String? = null
) : Parcelable


@Parcelize
data class SendOtpParam(
    @SerializedName("email")
    val email: String?
) : Parcelable

@Parcelize
data class VerifyOtpParam(
    @SerializedName("email")
    val email: String?,
    @SerializedName("otp")
    val otp: String?
) : Parcelable


@Parcelize
data class SubmitPersonalInfoParam(
    @SerializedName("name")
    val name: String?,
    @SerializedName("birthDate")
    val birthdate: String?,
    @SerializedName("gender")
    val gender: String?
) : Parcelable

@Parcelize
data class WeightHeightParam(
    @SerializedName("heightUnit")
    val heightUnit: String?,
    @SerializedName("height")
    val height: String?,
    @SerializedName("weightUnit")
    val weightUnit: String?,
    @SerializedName("weight")
    val weight: String?
) : Parcelable

@Parcelize
data class CompleteOnboardingParam(
    @SerializedName("isOnBoarding")
    val isOnBoarding: Boolean
) : Parcelable