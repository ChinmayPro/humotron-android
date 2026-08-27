package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetRecipesByMetricReadingResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("windowDays")
    val windowDays: Int? = null,

    @SerializedName("data")
    val data: RecipesByMetricData? = null
) : Parcelable

@Parcelize
data class RecipesByMetricData(
    @SerializedName("metric")
    val metric: RecipeMetricDetails? = null,

    @SerializedName("recipe")
    val recipe: Map<String, List<RecipeItemDetail>>? = null
) : Parcelable

@Parcelize
data class RecipeMetricDetails(
    @SerializedName("latestReading")
    val latestReading: Double? = null,

    @SerializedName("metricInterpretationContext")
    val metricInterpretationContext: String? = null,

    @SerializedName("metricUserFacingName")
    val metricUserFacingName: String? = null,

    @SerializedName("metricDeltaInfo")
    val metricDeltaInfo: MetricDeltaInfo? = null,

    @SerializedName("fontColor")
    val fontColor: String? = null,

    @SerializedName("deviceId")
    val deviceId: String? = null,

    @SerializedName("metricDeltaValue")
    val metricDeltaValue: Double? = null,

    @SerializedName("deviceName")
    val deviceName: String? = null,

    @SerializedName("deviceSlug")
    val deviceSlug: String? = null,

    @SerializedName("metricReadingUnit")
    val metricReadingUnit: String? = null,

    @SerializedName("metricDelta")
    val metricDelta: String? = null,

    @SerializedName("latestReadingAt")
    val latestReadingAt: String? = null,

    @SerializedName("boxColor")
    val boxColor: String? = null,

    @SerializedName("metricSlug")
    val metricSlug: String? = null,

    @SerializedName("metricName")
    val metricName: String? = null,

    @SerializedName("metricReading")
    val metricReading: String? = null,

    @SerializedName("metricDuration")
    val metricDuration: String? = null,

    @SerializedName("availableData")
    val availableData: Int? = null,

    @SerializedName("metricInterpretation")
    val metricInterpretation: String? = null,

    @SerializedName("metricId")
    val metricId: String? = null
) : Parcelable

@Parcelize
data class MetricDeltaInfo(
    @SerializedName("recentAverage")
    val recentAverage: Double? = null,

    @SerializedName("baselineAverage")
    val baselineAverage: Double? = null,

    @SerializedName("runWeeks")
    val runWeeks: Int? = null,

    @SerializedName("windowAverage")
    val windowAverage: Double? = null,

    @SerializedName("basis")
    val basis: String? = null,

    @SerializedName("runDirection")
    val runDirection: String? = null,

    @SerializedName("band")
    val band: String? = null,

    @SerializedName("text")
    val text: String? = null,

    @SerializedName("recentDays")
    val recentDays: Int? = null,

    @SerializedName("moveDirection")
    val moveDirection: String? = null,

    @SerializedName("changePercent")
    val changePercent: Double? = null,

    @SerializedName("direction")
    val direction: String? = null
) : Parcelable

@Parcelize
data class RecipeItemDetail(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("recipeName")
    val recipeName: String? = null,

    @SerializedName("recipe_name")
    val recipeNameAlt: String? = null,

    @SerializedName("short_description")
    val shortDescription: String? = null,

    @SerializedName("ingredients")
    val ingredients: String? = null,

    @SerializedName("cooking_instructions")
    val cookingInstructions: String? = null,

    @SerializedName("cooking_time")
    val cookingTime: String? = null,

    @SerializedName("complexity")
    val complexity: String? = null,

    @SerializedName("calories_per_serving")
    val caloriesPerServing: Int? = null,

    @SerializedName("recipeImage")
    val recipeImage: String? = null,

    @SerializedName("mealType")
    val mealType: String? = null,

    @SerializedName("meal_type")
    val mealTypeAlt: String? = null,

    @SerializedName("taste_profile")
    val tasteProfile: String? = null,

    @SerializedName("cuisine_type")
    val cuisineType: String? = null,

    @SerializedName("dietary_filters")
    val dietaryFilters: List<String>? = null,

    @SerializedName("allergens_excluded")
    val allergensExcluded: List<String>? = null,

    @SerializedName("pairing_suggestions")
    val pairingSuggestions: String? = null,

    @SerializedName("nutrition_info")
    val nutritionInfo: String? = null,

    @SerializedName("whyYou")
    val whyYou: String? = null,

    @SerializedName("whyThis")
    val whyThis: String? = null,

    @SerializedName("whyNow")
    val whyNow: String? = null,

    @SerializedName("why_reasons")
    val whyReasons: List<WhyReasonItem>? = null,

    @SerializedName("consumedCount")
    val consumedCount: Int? = null,

    @SerializedName("lastConsumedAt")
    val lastConsumedAt: String? = null,

    @SerializedName("interaction")
    val interaction: String? = null,

    @SerializedName("bundleId")
    val bundleId: String? = null,

    @SerializedName("bundleName")
    val bundleName: String? = null,

    @SerializedName("bundleDescription")
    val bundleDescription: String? = null,

    @SerializedName("ruleId")
    val ruleId: String? = null,

    @SerializedName("image_midjourney_prompt")
    val imageMidjourneyPrompt: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable

@Parcelize
data class WhyReasonItem(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null
) : Parcelable
