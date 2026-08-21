package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GenerateMetricResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: GenerateMetricData? = null
) : Parcelable

@Parcelize
data class GenerateMetricData(
    @SerializedName("metricData")
    val metricData: List<MetricReadingData>? = null,
    @SerializedName("pdfData")
    val pdfData: PdfReportData? = null
) : Parcelable

@Parcelize
data class MetricReadingData(
    @SerializedName("metricId")
    val metricId: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricReading")
    val metricReading: String? = null,
    @SerializedName("metricReadingDescription")
    val metricReadingDescription: String? = null,
    @SerializedName("metricDeviceName")
    val metricDeviceName: String? = null,
    @SerializedName("metricDeviceType")
    val metricDeviceType: String? = null,
    @SerializedName("metricLastReadingDate")
    val metricLastReadingDate: String? = null,
    @SerializedName("supplementCount")
    val supplementCount: Int? = null,
    @SerializedName("recipeBundleCount")
    val recipeBundleCount: Int? = null,
    @SerializedName("metricRecommendation")
    val metricRecommendation: MetricRecommendation? = null
) : Parcelable

@Parcelize
data class MetricRecommendation(
    @SerializedName("products")
    val products: List<MetricRecommendationItem>? = null,
    @SerializedName("recipeBundles")
    val recipeBundles: List<MetricRecommendationItem>? = null,
    @SerializedName("chatPrompts")
    val chatPrompts: List<ChatPrompt>? = null,
    @SerializedName("recommendation")
    val recommendation: List<MetricRecommendationItem>? = null
) : Parcelable

@Parcelize
data class MetricRecommendationItem(
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("short")
    val short: String? = null,
    @SerializedName("helps")
    val helps: List<String>? = null,
    @SerializedName("why_this")
    val whyThis: String? = null,
    @SerializedName("why_you")
    val whyYou: String? = null,
    @SerializedName("why_now")
    val whyNow: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("category")
    val category: List<String>? = null,
    @SerializedName("sub_category")
    val subCategory: List<String>? = null,
    @SerializedName("chatPrompt")
    val chatPrompt: ChatPrompt? = null,
    @SerializedName("priority_rank")
    val priorityRank: Int? = null,
    @SerializedName("recipeCount")
    val recipeCount: Int? = null
) : Parcelable
