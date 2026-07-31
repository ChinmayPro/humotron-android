package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlanResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: Data?
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("plan")
        val plan: List<Plan>?,
        @SerializedName("totalRecords")
        val totalRecords: Int?
    ) : Parcelable

    @Parcelize
    data class Plan(
        @SerializedName("_id")
        val id: String?,
        @SerializedName("planId")
        val planId: String?,
        @SerializedName("planName")
        val planName: String?,
        @SerializedName("planDescription")
        val planDescription: String?,
        @SerializedName("priceModel")
        val priceModel: String?,
        @SerializedName("price")
        val price: Double?,
        @SerializedName("heroTitle")
        val heroTitle: String?,
        @SerializedName("heroBullets")
        val heroBullets: List<String>?,
        @SerializedName("iosProductId")
        val iosProductId: String?,
        @SerializedName("androidProductId")
        val androidProductId: String?,
        @SerializedName("isActive")
        val isActive: Boolean?
    ) : Parcelable {
        val displayName: String get() = planName ?: ""
        val displayDescription: String get() = planDescription ?: ""

        val displayPriceFallback: String get() = if (price != null && price > 0) {
            val formatted = String.format("%.2f", price)
            if (formatted.endsWith(".00")) {
                "£" + String.format("%.0f", price)
            } else {
                "£$formatted"
            }
        } else {
            "Free"
        }

        // Play Store product ID matches androidProductId from backend API (falling back to humotron_premium for Pro Plan if empty)
        val playStoreProductId: String get() =
            androidProductId.takeIf { !it.isNullOrEmpty() }
                ?: if (planId == "Pro_Plan" || displayName.equals("Pro", ignoreCase = true)) "humotron_premium" else ""
    }
}
