package com.humotron.app.ui.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.UUID

class RecipesViewModel : ViewModel() {

    private val _metricCapsules = MutableLiveData<List<MetricCapsule>>()
    val metricCapsules: LiveData<List<MetricCapsule>> = _metricCapsules

    private val _selectedMetric = MutableLiveData<MetricCapsule?>()
    val selectedMetric: LiveData<MetricCapsule?> = _selectedMetric

    private val _recipeCategories = MutableLiveData<List<RecipeCategory>>()
    val recipeCategories: LiveData<List<RecipeCategory>> = _recipeCategories

    private val _recipeLogs = MutableLiveData<List<RecipeLog>>()
    val recipeLogs: LiveData<List<RecipeLog>> = _recipeLogs

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val capsules = listOf(
            MetricCapsule(
                id = "sbp",
                title = "Systolic BP",
                value = "128",
                unit = " mmHg",
                isSelected = true,
                isImproving = true,
                isUpwardTrend = false,
                trendValue = "3%",
                replacedMeals = 5,
                targetMeals = 20,
                sparkData = listOf(134f, 133f, 132f, 130f, 131f, 129f, 128f)
            ),
            MetricCapsule(
                id = "hrv",
                title = "HRV",
                value = "48",
                unit = " ms",
                isSelected = false,
                isImproving = true,
                isUpwardTrend = true,
                trendValue = "6%",
                replacedMeals = 2,
                targetMeals = 20,
                sparkData = listOf(42f, 44f, 43f, 45f, 46f, 47f, 48f)
            ),
            MetricCapsule(
                id = "recovery",
                title = "Recovery",
                value = "65",
                unit = "%",
                isSelected = false,
                isImproving = true,
                isUpwardTrend = true,
                trendValue = "11%",
                replacedMeals = 8,
                targetMeals = 20,
                sparkData = listOf(52f, 55f, 57f, 60f, 62f, 64f, 65f)
            )
        )
        _metricCapsules.value = capsules
        _selectedMetric.value = capsules.find { it.isSelected }

        val macros = listOf(
            "18" to "Protein",
            "20" to "Fat",
            "8" to "Carbs",
            "5" to "Omega-3"
        )
        val meta = listOf(
            "Dinner" to "Meal type",
            "25 minutes" to "Cooking time",
            "Spicy" to "Taste profile",
            "Vegan" to "Dietary preference",
            "Indian" to "Cuisine type"
        )
        val tags = listOf("Dairy-free", "Gluten-free")
        val steps = listOf(
            "Sauté the kale with garlic until just wilted.",
            "Bake the tofu with almonds and a drizzle of olive oil.",
            "Plate the kale, top with tofu, finish with toasted almonds."
        )
        val benefits = listOf(
            "Omega-3 support" to "Tofu offers omega-3s that support healthy blood flow.",
            "Magnesium-rich" to "Almonds provide magnesium for nerve and muscle function."
        )

        val breakfastRecipes = listOf(
            RecipeCard(
                id = "r1",
                title = "Berry Protein Bowl",
                imageUrl = "https://images.unsplash.com/photo-1490474418585-ba9f527d29dd?auto=format&fit=crop&q=80&w=400",
                timeMinutes = 10,
                metricPillText = "Recovery",
                difficulty = "Easy",
                kcalPerServing = 310,
                ingredients = listOf("Berries", "Yoghurt", "Chia"),
                logsCount = 0,
                isFavorite = false,
                macros = macros,
                meta = meta,
                tags = tags,
                steps = steps,
                benefits = benefits
            ),
            RecipeCard(
                id = "r2",
                title = "Almond Spinach Omelette",
                imageUrl = "https://images.unsplash.com/photo-1510693206972-df098062cb71?auto=format&fit=crop&q=80&w=400",
                timeMinutes = 15,
                metricPillText = "Systolic BP",
                difficulty = "Easy",
                kcalPerServing = 250,
                ingredients = listOf("Spinach", "Almonds", "Flaxseed"),
                logsCount = 5,
                isFavorite = true,
                macros = macros,
                meta = meta,
                tags = tags,
                steps = steps,
                benefits = benefits
            )
        )

        val lunchRecipes = listOf(
            RecipeCard(
                id = "r3",
                title = "Quinoa Veggie Bowl",
                imageUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&q=80",
                timeMinutes = 20,
                metricPillText = "Recovery",
                difficulty = "Medium",
                kcalPerServing = 380,
                ingredients = listOf("Quinoa", "Broccoli", "Carrots"),
                logsCount = 0,
                isFavorite = false,
                macros = macros,
                meta = meta,
                tags = tags,
                steps = steps,
                benefits = benefits
            )
        )

        val dinnerCategory = RecipeCategory(
            id = "dinner",
            title = "Dinner",
            recipes = listOf(
                RecipeCard(
                    id = "d1",
                    title = "Grilled Salmon Salad",
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80",
                    timeMinutes = 35,
                    metricPillText = "Systolic BP",
                    difficulty = "Easy",
                    kcalPerServing = 420,
                    ingredients = listOf("Salmon", "Lettuce", "Lemon"),
                    logsCount = 0,
                    isFavorite = true,
                    macros = macros,
                    meta = meta,
                    tags = tags,
                    steps = steps,
                    benefits = benefits
                ),
                RecipeCard(
                    id = "d2",
                    title = "Miso Tofu & Greens",
                    imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&q=80&w=400",
                    timeMinutes = 22,
                    metricPillText = "HRV",
                    difficulty = "Medium",
                    kcalPerServing = 330,
                    ingredients = listOf("Tofu", "Bok choi", "Miso"),
                    logsCount = 2,
                    isFavorite = false,
                    macros = macros,
                    meta = meta,
                    tags = tags,
                    steps = steps,
                    benefits = benefits
                )
            )
        )

        _recipeCategories.value = listOf(
            RecipeCategory("c1", "Breakfast", breakfastRecipes),
            RecipeCategory("c2", "Lunch", lunchRecipes),
            dinnerCategory
        )

        _recipeLogs.value = listOf(
            RecipeLog("l1", "Almond Spinach Omelette", "Breakfast", "Oct 12", "Systolic BP"),
            RecipeLog("l2", "Quinoa Veggie Bowl", "Lunch", "Oct 11", "Recovery"),
            RecipeLog("l3", "Berry Protein Bowl", "Breakfast", "Oct 10", "HRV")
        )
    }

    fun selectMetric(id: String) {
        val currentList = _metricCapsules.value ?: return
        val updatedList = currentList.map {
            it.copy(isSelected = it.id == id)
        }
        _metricCapsules.value = updatedList
        _selectedMetric.value = updatedList.find { it.isSelected }
    }

    fun logRecipe(recipeId: String) {
        val currentCategories = _recipeCategories.value ?: return
        var foundRecipe: RecipeCard? = null
        var foundCategory: String = "Meal"

        val updatedCategories = currentCategories.map { category ->
            val updatedRecipes = category.recipes.map { recipe ->
                if (recipe.id == recipeId) {
                    val newCount = recipe.logsCount + 1
                    foundRecipe = recipe.copy(logsCount = newCount)
                    foundCategory = category.title
                    foundRecipe!!
                } else {
                    recipe
                }
            }
            category.copy(recipes = updatedRecipes)
        }

        foundRecipe?.let { recipe ->
            _recipeCategories.value = updatedCategories

            // Increment replacedMeals in matching capsule
            val currentCapsules = _metricCapsules.value ?: return
            val updatedCapsules = currentCapsules.map { capsule ->
                if (capsule.title.equals(recipe.metricPillText, ignoreCase = true)) {
                    capsule.copy(replacedMeals = capsule.replacedMeals + 1)
                } else {
                    capsule
                }
            }
            _metricCapsules.value = updatedCapsules
            _selectedMetric.value = updatedCapsules.find { it.isSelected }

            // Add journal log
            val currentLogs = _recipeLogs.value.orEmpty().toMutableList()
            currentLogs.add(0, RecipeLog(
                id = UUID.randomUUID().toString(),
                recipeName = recipe.title,
                mealType = foundCategory,
                logTime = "today",
                metricName = recipe.metricPillText
            ))
            _recipeLogs.value = currentLogs
        }
    }

    fun toggleFavorite(recipeId: String) {
        val currentCategories = _recipeCategories.value ?: return
        val updatedCategories = currentCategories.map { category ->
            val updatedRecipes = category.recipes.map { recipe ->
                if (recipe.id == recipeId) {
                    recipe.copy(isFavorite = !recipe.isFavorite)
                } else {
                    recipe
                }
            }
            category.copy(recipes = updatedRecipes)
        }
        _recipeCategories.value = updatedCategories
    }

    fun getRecipeById(recipeId: String): RecipeCard? {
        return _recipeCategories.value?.flatMap { it.recipes }?.find { it.id == recipeId }
    }
}
