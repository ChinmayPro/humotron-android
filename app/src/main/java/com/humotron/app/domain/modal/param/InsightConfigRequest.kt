package com.humotron.app.domain.modal.param

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class InsightConfigRequest(
    @SerializedName("preferences")
    val preferences: Preferences? = null
) : Parcelable {

    @Parcelize
    data class Preferences(
        @SerializedName("frequency")
        val frequency: @RawValue Any? = null,
        @SerializedName("triggers")
        val triggers: String? = null,
        @SerializedName("insightStyle")
        val insightStyle: String? = null,
        @SerializedName("focusAreas")
        val focusAreas: List<String>? = null
    ) : Parcelable
}
