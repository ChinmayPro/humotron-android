package com.humotron.app.ui.recipes

data class MetricCapsule(
    val id: String,
    val title: String,
    val value: String,
    val unit: String = "",
    val isSelected: Boolean = false,
    val isImproving: Boolean = true,
    val isUpwardTrend: Boolean = true,
    val trendValue: String = "0%",
    val replacedMeals: Int = 0,
    val targetMeals: Int = 20,
    val sparkData: List<Float> = emptyList()
)

data class RecipeLog(
    val id: String,
    val recipeName: String,
    val mealType: String,
    val logTime: String,
    val metricName: String
)

data class RecipeCategory(
    val id: String,
    val title: String,
    val recipes: List<RecipeCard>
)

data class RecipeCard(
    val id: String,
    val title: String,
    val imageUrl: String,
    val timeMinutes: Int,
    val metricPillText: String,
    val difficulty: String,
    val kcalPerServing: Int,
    val ingredients: List<String>,
    val logsCount: Int = 0,
    val isFavorite: Boolean = false,
    val macros: List<Pair<String, String>> = emptyList(),
    val meta: List<Pair<String, String>> = emptyList(),
    val tags: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val benefits: List<Pair<String, String>> = emptyList()
)
