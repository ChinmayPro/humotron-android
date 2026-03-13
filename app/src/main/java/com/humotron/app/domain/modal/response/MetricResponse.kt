package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetricResponse(

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: MetricData? = null,

    ) : Parcelable


@Parcelize
data class MetricData(

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("metricName")
    val metricName: String? = null,

    @SerializedName("metricUnit")
    val metricUnit: String? = null,

    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,

    @SerializedName("metricDescription")
    val metricDescription: String? = null,

    @SerializedName("deviceId")
    val deviceId: String? = null,

    @SerializedName("metricWhat")
    val metricWhat: String? = null,

    @SerializedName("metricWhy")
    val metricWhy: String? = null,

    @SerializedName("observationLens")
    val observationLens: String? = null,

    @SerializedName("metricOrder")
    val metricOrder: Int? = null,

    @SerializedName("timeFormat")
    val timeFormat: String? = null,

    @SerializedName("deviceName")
    val deviceName: String? = null,

    @SerializedName("metricReading")
    val metricReading: String? = null,

    @SerializedName("metricDuration")
    val metricDuration: String? = null,

    @SerializedName("recommendationMetricName")
    val recommendationMetricName: String? = null,

    @SerializedName("fontColor")
    val fontColor: String? = null,

    @SerializedName("boxColor")
    val boxColor: String? = null,

    @SerializedName("recipes")
    val recipes: Recipes? = null,

    @SerializedName("shortDescription")
    val shortDescription: String? = null,

    @SerializedName("supplements")
    val supplements: List<Supplement>? = null,

    @SerializedName("recommendations")
    val recommendations: Recommendations? = null,

    @SerializedName("metricReadingSubText")
    val metricReadingSubText: String? = null,

    @SerializedName("metricReadingFrequency")
    val metricReadingFrequency: Int? = null,

    @SerializedName("metricReadingUnit")

    val metricReadingUnit: String? = null,

    @SerializedName("availableData")
    val availableData: Int? = null,

    @SerializedName("insight")
    val insight: List<Insight>? = null,

    @SerializedName("haveRules")
    val haveRules: Boolean? = null,

    ) : Parcelable

@Parcelize
data class Insight(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("metricName")
    val metricName: String? = null,

    @SerializedName("insightRange")
    val insightRange: String? = null,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("hypothesis")
    val hypothesis: Hypothesis? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("observational_lens")
    val observationalLens: String? = null,
) : Parcelable

@Parcelize
data class Hypothesis(
    @SerializedName("narrative")
    val narrative: String? = null,

    @SerializedName("reasons")
    val reasons: List<String>? = null,
) : Parcelable

@Parcelize
data class Recipes(
    @SerializedName("items")
    val items: List<RecipeBundle>? = null,

    ) : Parcelable

@Parcelize
data class RecipeBundle(
    @SerializedName("recipeBundleName")
    val recipeBundleName: String? = null,

    @SerializedName("recipeBundleId")
    val recipeBundleId: String? = null,

    @SerializedName("recipeBundleDesc")
    val recipeBundleDesc: String? = null,

    @SerializedName("recipeBundleHelps")
    val recipeBundleHelps: String? = null,

    @SerializedName("chatPrompt")
    val chatPrompt: ChatPrompt? = null,

    @SerializedName("recipeBundleImage")
    val recipeBundleImage: String? = null,

    @SerializedName("recipeCount")
    val recipeCount: Int? = null,
) : Parcelable


@Parcelize
data class Recommendations(

    @SerializedName("items")
    val items: List<RecommendationItem>? = null,

    @SerializedName("chatPrompts")
    val chatPrompts: List<ChatPrompt>? = null,

    ) : Parcelable


@Parcelize
data class RecommendationItem(

    @SerializedName("recommendationsTag")
    val recommendationsTag: String? = null,

    @SerializedName("recommendationsShort")
    val recommendationsShort: String? = null,

    @SerializedName("recommendationsLong")
    val recommendationsLong: String? = null,

    @SerializedName("isPreview")
    val isPreview: Boolean? = null,

    ) : Parcelable


@Parcelize
data class ChatPrompt(

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("type")
    val type: String? = null,

    ) : Parcelable

@Parcelize
data class Supplement(
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

    @SerializedName("chatPrompt")
    val chatPrompt: ChatPrompt? = null,

    @SerializedName("productImage")
    val productImage: String? = null,
) : Parcelable