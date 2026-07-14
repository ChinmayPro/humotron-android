package com.humotron.app.domain.modal.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetOptimizedRecommendationDetailResponse(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: RecommendationDetailData? = null
) : Parcelable

@Parcelize
data class RecommendationDetailData(
    @SerializedName("_id")
    val internalId: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null, // supplement, recipeBundle, lifestyle, device, service
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("short")
    val short: String? = null,
    @SerializedName("image")
    val image: String? = null,
    
    // Supplement specific
    @SerializedName("product_name")
    val productName: String? = null,
    @SerializedName("brand_or_provider")
    val brandOrProvider: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("sub_category")
    val subCategory: String? = null,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    @SerializedName("bottom_line")
    val bottomLine: String? = null,
    @SerializedName("best_for")
    val bestFor: String? = null,
    @SerializedName("not_for")
    val notFor: String? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("price_note")
    val priceNote: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("cta_primary")
    val ctaPrimary: String? = null,
    @SerializedName("faqs_json")
    val faqsJson: List<DetailFaqItem>? = null,
    @SerializedName("attributes")
    val attributes: SupplementDetailAttributes? = null,
    
    // Recipe Bundle specific
    @SerializedName("bundle_name")
    val bundleName: String? = null,
    @SerializedName("bundle_short_description")
    val bundleShortDescription: String? = null,
    @SerializedName("why_pillars")
    val whyPillars: List<WhyPillar>? = null,
    @SerializedName("recipes")
    val recipes: List<RecipeDetailItem>? = null
) : Parcelable

@Parcelize
data class DetailFaqItem(
    @SerializedName("q")
    val question: String? = null,
    @SerializedName("a")
    val answer: String? = null
) : Parcelable

@Parcelize
data class WhyPillar(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null
) : Parcelable

@Parcelize
data class SupplementDetailAttributes(
    @SerializedName("type_specific")
    val typeSpecific: TypeSpecificAttributes? = null,
    @SerializedName("match_confidence")
    val matchConfidence: String? = null,
    @SerializedName("benefit")
    val benefits: List<String>? = null,
    @SerializedName("usage")
    val usage: List<String>? = null,
    @SerializedName("contraindications_or_caveats")
    val caveats: List<String>? = null,
    @SerializedName("why_this")
    val whyThis: String? = null,
    @SerializedName("track_statement")
    val trackStatement: String? = null,
    @SerializedName("honesty_shape")
    val honestyShape: String? = null
) : Parcelable

@Parcelize
data class TypeSpecificAttributes(
    @SerializedName("primary_mechanism")
    val primaryMechanism: String? = null,
    @SerializedName("mechanism_detail")
    val mechanismDetail: String? = null,
    @SerializedName("why_this_form")
    val whyThisForm: String? = null,
    @SerializedName("key_ingredients_json")
    val keyIngredientsJson: String? = null, // JSON String
    @SerializedName("inactive_ingredients")
    val inactiveIngredients: String? = null,
    @SerializedName("serving_size")
    val servingSize: String? = null,
    @SerializedName("course_length")
    val courseLength: String? = null,
    @SerializedName("pack_size_servings")
    val packSizeServings: String? = null,
    @SerializedName("drug_interactions")
    val drugInteractions: String? = null,
    @SerializedName("side_effects")
    val sideEffects: String? = null,
    @SerializedName("allergen_flags")
    val allergenFlags: String? = null
) : Parcelable

@Parcelize
data class RecipeDetailItem(
    @SerializedName("_id")
    val recipeId: String? = null,
    @SerializedName("recipe_name")
    val recipeName: String? = null,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    @SerializedName("calories_per_serving")
    val caloriesPerServing: Int? = null,
    @SerializedName("ingredients")
    val ingredients: String? = null,
    @SerializedName("nutrition_info")
    val nutritionInfo: String? = null,
    @SerializedName("cooking_instructions")
    val cookingInstructions: String? = null,
    @SerializedName("cooking_time")
    val cookingTime: String? = null,
    @SerializedName("complexity")
    val complexity: String? = null,
    @SerializedName("why_reasons")
    val whyReasons: List<WhyPillar>? = null,
    @SerializedName("dietary_filters")
    val dietaryFilters: List<String>? = null
) : Parcelable
