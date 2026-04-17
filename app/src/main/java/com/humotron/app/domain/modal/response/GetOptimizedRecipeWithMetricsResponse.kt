package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetOptimizedRecipeWithMetricsResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: OptimizedRecipeData? = null
) : Parcelable

@Parcelize
data class OptimizedRecipeData(
    @SerializedName("supplements")
    val supplements: List<SupplementItem>? = null,

    @SerializedName("recipes")
    val recipes: List<RecipeItem>? = null,

    @SerializedName("recommendations")
    val recommendations: List<OptimizedRecommendationItem>? = null
) : Parcelable

@Parcelize
data class SupplementItem(
    @SerializedName("productName")
    val productName: String? = null,
    @SerializedName("productId")
    val productId: String? = null,
    @SerializedName("productDesc")
    val productDesc: String? = null,
    @SerializedName("productHelps")
    val productHelps: String? = null,
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("productImage")
    val productImage: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricDelta")
    val metricDelta: String? = null,
    @SerializedName("metricReading")
    val metricReading: String? = null,
    @SerializedName("productChatPrompt")
    val productChatPrompt: ChatPrompt? = null,
) : Parcelable

@Parcelize
data class RecipeItem(
    @SerializedName("recipeBundleName")
    val recipeBundleName: String? = null,
    @SerializedName("recipeBundleId")
    val recipeBundleId: String? = null,
    @SerializedName("recipeBundleDesc")
    val recipeBundleDesc: String? = null,
    @SerializedName("recipeBundleHelps")
    val recipeBundleHelps: String? = null,
    @SerializedName("recipeBundleImage")
    val recipeBundleImage: String? = null,
    @SerializedName("metricName")
    val metricName: String? = null,
    @SerializedName("metricDelta")
    val metricDelta: String? = null,
    @SerializedName("metricReading")
    val metricReading: String? = null,
    @SerializedName("chatPrompt")
    val chatPrompt: ChatPrompt? = null,
) : Parcelable

@Parcelize
data class OptimizedRecommendationItem(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("subtitle")
    val subtitle: String? = null,
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
) : Parcelable
