package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HealthProfileConfigRequest(
    @SerializedName("healthGoals")
    val healthGoals: List<String>? = null,
    @SerializedName("medicalConditions")
    val medicalConditions: List<String>? = null
) : Parcelable
