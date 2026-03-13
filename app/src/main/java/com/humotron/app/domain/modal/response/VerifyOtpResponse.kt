package com.humotron.app.domain.modal.response


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class VerifyOtpResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?
) : Parcelable

@Parcelize
data class Data(
    @SerializedName("token")
    val token: String?,
    @SerializedName("user")
    val user: User?
) : Parcelable

@Parcelize
data class User(
    @SerializedName("appleAuthCode")
    val appleAuthCode: String?,
    @SerializedName("appleUid")
    val appleUid: String?,
    @SerializedName("avgWakeUpTime")
    val avgWakeUpTime: String?,
    @SerializedName("birthDate")
    val birthDate: String?,
    @SerializedName("cart")
    val cart: List<@RawValue Any>?,
    @SerializedName("contactNo")
    val contactNo: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("dailyReport")
    val dailyReport: String?,
    @SerializedName("deliveryMethodId")
    val deliveryMethodId: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("emailInvoice")
    val emailInvoice: Boolean?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("heartHealthIndex")
    val heartHealthIndex: Int?,
    @SerializedName("height")
    val height: String?,
    @SerializedName("heightUnit")
    val heightUnit: String?,
    @SerializedName("_id")
    val id: String?,
    @SerializedName("isDeleted")
    val isDeleted: Boolean?,
    @SerializedName("isOnBoarding")
    val isOnBoarding: Boolean?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("mode")
    val mode: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("profileImages")
    val profileImages: String?,
    @SerializedName("recoveryEmail")
    val recoveryEmail: String?,
    @SerializedName("ringSize")
    val ringSize: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("userType")
    val userType: String?,
    @SerializedName("vatNumber")
    val vatNumber: String?,
    @SerializedName("verified")
    val verified: Boolean?,
    @SerializedName("verifyToken")
    val verifyToken: String?,
    @SerializedName("weeklyReport")
    val weeklyReport: String?,
    @SerializedName("weight")
    val weight: String?,
    @SerializedName("weightUnit")
    val weightUnit: String?
) : Parcelable
