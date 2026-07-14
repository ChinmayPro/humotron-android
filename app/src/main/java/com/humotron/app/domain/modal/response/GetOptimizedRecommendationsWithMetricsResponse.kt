package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetOptimizedRecommendationsWithMetricsResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<RecommendationMetricItem>? = null
) : Parcelable

@Parcelize
data class RecommendationMetricItem(
    @SerializedName("type")
    val type: String? = null, // supplement, recipeBundle, lifestyle

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

    @SerializedName("metricId")
    val metricId: String? = null,

    @SerializedName("metricName")
    val metricName: String? = null,

    @SerializedName("metricDelta")
    val metricDelta: String? = null,

    @SerializedName("metricReading")
    val metricReading: String? = null,

    @SerializedName("recommendationMetricName")
    val recommendationMetricName: String? = null,

    @SerializedName("recipeCount")
    val recipeCount: Int? = null
) : Parcelable
