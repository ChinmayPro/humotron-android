package com.humotron.app.ui.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.data.repository.ProfileRepository
import com.humotron.app.domain.modal.response.GetRecipesByMetricReadingResponse
import com.humotron.app.domain.modal.response.RecipeItemDetail
import com.humotron.app.domain.modal.response.RecipesByMetricData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _metricCapsules = MutableLiveData<List<MetricCapsule>>()
    val metricCapsules: LiveData<List<MetricCapsule>> = _metricCapsules

    private val _selectedMetric = MutableLiveData<MetricCapsule?>()
    val selectedMetric: LiveData<MetricCapsule?> = _selectedMetric

    private val _recipeCategories = MutableLiveData<List<RecipeCategory>>()
    val recipeCategories: LiveData<List<RecipeCategory>> = _recipeCategories

    private val _recipeLogs = MutableLiveData<List<RecipeLog>>()
    val recipeLogs: LiveData<List<RecipeLog>> = _recipeLogs

    private val _recipesResponseState = MutableLiveData<Resource<GetRecipesByMetricReadingResponse>>()
    val recipesResponseState: LiveData<Resource<GetRecipesByMetricReadingResponse>> = _recipesResponseState

    init {
        loadMockData()
    }

    fun fetchRecipesByMetric(metricId: String) {
        if (metricId.isBlank()) return

        repository.getRecipesByMetricReading(metricId).onEach { resource ->
            _recipesResponseState.value = resource

            if (resource.status == Status.SUCCESS) {
                resource.data?.data?.let { responseData ->
                    processApiResponseData(responseData, metricId)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun processApiResponseData(data: RecipesByMetricData, selectedId: String) {
        val apiMetric = data.metric

        if (apiMetric != null) {
            val title = apiMetric.metricUserFacingName ?: apiMetric.metricName ?: "Metric"
            val value = apiMetric.metricReading ?: apiMetric.latestReading?.toString() ?: "0"
            val unit = apiMetric.metricReadingUnit ?: ""
            val isImproving = apiMetric.metricInterpretation?.equals("Normal", ignoreCase = true) == true ||
                    apiMetric.metricDeltaInfo?.direction?.equals("improving", ignoreCase = true) == true
            val isUpward = apiMetric.metricDeltaInfo?.direction?.equals("up", ignoreCase = true) == true
            val trendVal = apiMetric.metricDelta ?: "0%"

            val updatedCapsule = MetricCapsule(
                id = apiMetric.metricId ?: selectedId,
                title = title,
                value = value,
                unit = if (unit.isNotBlank()) " $unit" else "",
                isSelected = true,
                isImproving = isImproving,
                isUpwardTrend = isUpward,
                trendValue = trendVal,
                replacedMeals = 5,
                targetMeals = 20,
                sparkData = listOf(75f, 74f, 73f, 72f, 71f, 71f, 71f)
            )

            // Update capsule list ensuring selected capsule is focused
            val currentCapsules = _metricCapsules.value.orEmpty()
            val existingIndex = currentCapsules.indexOfFirst { it.id == updatedCapsule.id }

            val newCapsules = if (existingIndex >= 0) {
                currentCapsules.map {
                    if (it.id == updatedCapsule.id) updatedCapsule else it.copy(isSelected = false)
                }
            } else {
                val resetCapsules = currentCapsules.map { it.copy(isSelected = false) }
                listOf(updatedCapsule) + resetCapsules
            }

            _metricCapsules.value = newCapsules
            _selectedMetric.value = updatedCapsule
        }

        // Process Recipe categories from map
        val categoryOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Anytime")
        val recipeMap = data.recipe.orEmpty()

        val parsedCategories = mutableListOf<RecipeCategory>()

        // Add ordered categories first
        for (catName in categoryOrder) {
            val recipeList = recipeMap[catName]
            if (!recipeList.isNullOrEmpty()) {
                val categoryId = catName.lowercase()
                val cards = recipeList.map { detail ->
                    mapDetailToRecipeCard(detail, apiMetric?.metricUserFacingName ?: apiMetric?.metricName ?: "Metric")
                }
                parsedCategories.add(RecipeCategory(categoryId, catName, cards))
            }
        }

        // Add any remaining categories not in standard order
        for ((catName, recipeList) in recipeMap) {
            if (!categoryOrder.contains(catName) && !recipeList.isNullOrEmpty()) {
                val categoryId = catName.lowercase()
                val cards = recipeList.map { detail ->
                    mapDetailToRecipeCard(detail, apiMetric?.metricUserFacingName ?: apiMetric?.metricName ?: "Metric")
                }
                parsedCategories.add(RecipeCategory(categoryId, catName, cards))
            }
        }

        if (parsedCategories.isNotEmpty()) {
            _recipeCategories.value = parsedCategories
        }
    }

    private fun mapDetailToRecipeCard(detail: RecipeItemDetail, metricTitle: String): RecipeCard {
        val title = detail.recipeName ?: detail.recipeNameAlt ?: "Recipe"
        val image = detail.recipeImage ?: "https://humotron-images.s3.eu-west-2.amazonaws.com/recipesImage/Beef+Chicken+Soup+2.png"
        val timeMins = parseCookingTime(detail.cookingTime)
        val difficulty = detail.complexity ?: "Easy"
        val calories = detail.caloriesPerServing ?: 350
        val ingredientsList = parseIngredients(detail.ingredients)
        val logsCount = detail.consumedCount ?: 0
        val isFav = detail.interaction.equals("like", ignoreCase = true)

        val macros = mutableListOf<Pair<String, String>>()
        if (!detail.nutritionInfo.isNullOrBlank()) {
            parseNutritionInfo(detail.nutritionInfo).forEach { (k, v) ->
                macros.add(v to k)
            }
        } else {
            macros.addAll(listOf("18" to "Protein", "20" to "Fat", "8" to "Carbs", "5" to "Omega-3"))
        }

        val meta = listOf(
            (detail.mealType ?: detail.mealTypeAlt ?: "Meal") to "Meal type",
            (detail.cookingTime ?: "10 mins") to "Cooking time",
            (detail.tasteProfile ?: "Savoury") to "Taste profile",
            (detail.dietaryFilters?.firstOrNull() ?: "Omnivore") to "Dietary preference",
            (detail.cuisineType ?: "Global") to "Cuisine type"
        )

        val tags = detail.dietaryFilters.orEmpty()
        val steps = parseCookingInstructions(detail.cookingInstructions)

        val benefits = mutableListOf<Pair<String, String>>()
        detail.whyReasons?.forEach { reason ->
            if (!reason.name.isNullOrBlank() && !reason.description.isNullOrBlank()) {
                benefits.add(reason.name to reason.description)
            }
        }
        if (benefits.isEmpty() && !detail.whyThis.isNullOrBlank()) {
            benefits.add("Why this meal" to detail.whyThis)
        }

        return RecipeCard(
            id = detail.id ?: UUID.randomUUID().toString(),
            title = title,
            imageUrl = image,
            timeMinutes = timeMins,
            metricPillText = metricTitle,
            difficulty = difficulty,
            kcalPerServing = calories,
            ingredients = ingredientsList,
            logsCount = logsCount,
            isFavorite = isFav,
            macros = macros,
            meta = meta,
            tags = tags,
            steps = steps,
            benefits = benefits
        )
    }

    private fun parseCookingTime(timeStr: String?): Int {
        if (timeStr.isNullOrBlank()) return 10
        val regex = "(\\d+)".toRegex()
        val match = regex.find(timeStr)
        return match?.value?.toIntOrNull() ?: 10
    }

    private fun parseIngredients(ingredientsStr: String?): List<String> {
        if (ingredientsStr.isNullOrBlank()) return emptyList()
        return ingredientsStr.split(";").map { raw ->
            val parts = raw.split(":")
            if (parts.size >= 2) {
                parts[0].trim()
            } else {
                raw.trim()
            }
        }.filter { it.isNotBlank() }
    }

    private fun parseCookingInstructions(instructionsStr: String?): List<String> {
        if (instructionsStr.isNullOrBlank()) {
            return listOf("Prepare fresh ingredients.", "Combine according to taste and serve.")
        }
        val sentences = instructionsStr.split(".").map { it.trim() }.filter { it.isNotBlank() }
        return if (sentences.isNotEmpty()) sentences else listOf(instructionsStr)
    }

    private fun parseNutritionInfo(nutritionStr: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        try {
            val parts = nutritionStr.replace("Per serving - ", "").split(";")
            for (part in parts) {
                val kv = part.split(":")
                if (kv.size == 2) {
                    val key = kv[0].trim()
                    val value = kv[1].trim()
                    result.add(key to value)
                }
            }
        } catch (_: Exception) { }
        return result
    }

    private fun loadMockData() {
        val capsules = listOf(
            MetricCapsule(
                id = "698ea56c286301cbff27cd0a",
                title = "Resting Heart Rate",
                value = "71",
                unit = " bpm",
                isSelected = true,
                isImproving = true,
                isUpwardTrend = false,
                trendValue = "0.0",
                replacedMeals = 5,
                targetMeals = 20,
                sparkData = listOf(74f, 73f, 72f, 71f, 71f, 71f, 71f)
            ),
            MetricCapsule(
                id = "sbp",
                title = "Systolic BP",
                value = "128",
                unit = " mmHg",
                isSelected = false,
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

        // Fetch recipes from backend API for the selected metric ID
        fetchRecipesByMetric(id)
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
