package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BloodTestServicesResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: BloodTestServicesData? = null
) : Parcelable

@Parcelize
data class BloodTestServicesData(
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("services")
    val services: List<BloodTestServiceItem>? = null
) : Parcelable

@Parcelize
data class BloodTestServiceItem(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("shortDescription")
    val shortDescription: String? = null,
    @SerializedName("longDescription")
    val longDescription: String? = null,
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    @SerializedName("turnaroundText")
    val turnaroundText: String? = null,
    @SerializedName("prepInstructions")
    val prepInstructions: String? = null,
    @SerializedName("howItWorks")
    val howItWorks: List<String>? = null,
    @SerializedName("whatsIncluded")
    val whatsIncluded: List<String>? = null,
    @SerializedName("price")
    val price: ServicePrice? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("requires")
    val requires: ServiceRequirements? = null,
    @SerializedName("serviceType")
    val serviceType: String? = null,
    @SerializedName("serviceTypeLabel")
    val serviceTypeLabel: String? = null
) : Parcelable

@Parcelize
data class ServicePrice(
    @SerializedName("amount")
    val amount: Double? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("symbol")
    val symbol: String? = null,
    @SerializedName("formatted")
    val formatted: String? = null
) : Parcelable

@Parcelize
data class ServiceRequirements(
    @SerializedName("address")
    val address: Boolean? = null,
    @SerializedName("schedule")
    val schedule: Boolean? = null,
    @SerializedName("lab")
    val lab: Boolean? = null
) : Parcelable
