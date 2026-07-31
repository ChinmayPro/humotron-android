package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProviderResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Data? = null
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("providers")
        val providers: List<Provider>? = null
    ) : Parcelable {

        @Parcelize
        data class Provider(
            @SerializedName("_id")
            val id: String? = null,
            @SerializedName("providerName")
            val providerName: String? = null,
            @SerializedName("providerValue")
            val providerValue: String? = null,
            @SerializedName("isRecommended")
            val isRecommended: Boolean? = null,
            @SerializedName("isDisplayOnApp")
            val isDisplayOnApp: Boolean? = null,
            @SerializedName("bgColorCode")
            val bgColorCode: String? = null,
            @SerializedName("txtColorCode")
            val txtColorCode: String? = null,
            @SerializedName("subTitle")
            val subTitle: String? = null,
            @SerializedName("isDeleted")
            val isDeleted: Boolean? = null,
            @SerializedName("connectionType")
            val connectionType: String? = null,
            @SerializedName("backendProvider")
            val backendProvider: String? = null,
            @SerializedName("isConnected")
            val isConnected: Boolean? = null
        ) : Parcelable
    }
}
