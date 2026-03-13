package com.humotron.app.domain.modal.param

import com.google.gson.annotations.SerializedName

data class AddHardware(
    @SerializedName("hardwareType")
    val type: String,
    @SerializedName("userHardwareUUID")
    val hardwareId: String
)
