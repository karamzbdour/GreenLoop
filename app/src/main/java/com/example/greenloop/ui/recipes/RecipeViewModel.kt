package com.example.greenloop.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.api.OpenRouterManager
import com.example.greenloop.data.model.GeneratedRecipe
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.model.Recipe
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.IngredientRepository
import com.example.greenloop.data.repository.RecipeRepository
import com.example.greenloop.data.repository.UserRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val historyRepository: HistoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val inventory: StateFlow<List<Ingredient>> = ingredientRepository.allIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedRecipes: StateFlow<List<Recipe>> = recipeRepository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedIngredients = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIngredients: StateFlow<Set<Int>> = _selectedIngredients.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedRecipes = MutableStateFlow<List<GeneratedRecipe>>(emptyList())
    val generatedRecipes: StateFlow<List<GeneratedRecipe>> = _generatedRecipes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, GeneratedRecipe::class.java)
    private val adapter = moshi.adapter<List<GeneratedRecipe>>(listType)

    fun toggleIngredientSelection(id: Int) {
        _selectedIngredients.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun generateAiRecipe() {
        val selectedIds = _selectedIngredients.value
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _isGenerating.value = true
            _errorMessage.value = null
            try {
                val selectedItems = inventory.value
                    .filter { it.id in selectedIds && (it.quantity?.removePrefix("x")?.toIntOrNull() ?: 1) > 0 }
                
                val selectedNames = selectedItems.joinToString(", ") { it.name }

                if (selectedNames.isEmpty()) {
                    _errorMessage.value = "Selected ingredients are out of stock."
                    return@launch
                }

                val prompt = """
                    Generate at least 3 simple waste-reducing recipes using these ingredients: ${selectedNames}.
                    The recipes must be optimized to use these items from being wasted.
                    Response MUST be a strict JSON array of objects with this structure:
                    [
                      {
                        "recipeName": "String",
                        "prepTimeMinutes": Int,
                        "difficulty": "Easy/Medium/Hard",
                        "steps": ["Step 1", "Step 2", ...],
                        "calories": Int (per serving),
                        "protein": Int (grams per serving)
                      }
                    ]
                    Only return the JSON array.
                """.trimIndent()

                val responseText = OpenRouterManager.generateContent(prompt) ?: ""
                
                val jsonString = responseText.substringAfter("```json").substringBeforeLast("```").trim()
                val finalJson = if (jsonString.isEmpty()) responseText.trim() else jsonString
                
                val recipes = adapter.fromJson(finalJson) ?: emptyList()
                if (recipes.isEmpty()) {
                    _errorMessage.value = "No recipes could be generated. Please try again."
                } else {
                    _generatedRecipes.value = recipes
                    // Increment the counter in DataStore
                    userRepository.incrementRecipesGenerated()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error generating recipes: ${e.localizedMessage}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun useRecipe(recipe: GeneratedRecipe) {
        viewModelScope.launch {
            val selectedIds = _selectedIngredients.value
            val selectedIngredients = inventory.value.filter { it.id in selectedIds }
            val ingredientNames = selectedIngredients.joinToString(", ") { it.name }
            val co2Saved = selectedIds.size * 0.5

            val newRecipe = Recipe(
                title = recipe.recipeName,
                description = "Custom upcycled recipe using $ingredientNames",
                ingredients = ingredientNames,
                steps = recipe.steps.joinToString("\n"),
                preparationTime = recipe.prepTimeMinutes,
                difficulty = recipe.difficulty,
                co2Saved = co2Saved,
                isWasteReducing = true,
                calories = recipe.calories,
                protein = recipe.protein
            )
            recipeRepository.insertRecipe(newRecipe)
            
            _generatedRecipes.update { current -> current.filter { it.recipeName != recipe.recipeName } }
        }
    }

    fun completeRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val recipeIngredients = recipe.ingredients.split(",").map { it.trim().lowercase() }
            
            var moneySavedFromRecipe = 0.0
            
            inventory.value.forEach { item ->
                if (recipeIngredients.any { it.contains(item.name.lowercase()) || item.name.lowercase().contains(it) }) {
                    val currentQty = item.quantity?.removePrefix("x")?.toIntOrNull() ?: 0
                    if (currentQty > 0) {
                        ingredientRepository.updateIngredient(item.copy(
                            quantity = "x${currentQty - 1}",
                            wasRemovedManually = currentQty <= 1
                        ))
                        moneySavedFromRecipe += (item.price ?: 0.0)
                    }
                }
            }

            historyRepository.insertHistory(UpcycleHistory(
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                co2Saved = recipe.co2Saved,
                moneySaved = moneySavedFromRecipe
            ))
        }
    }

    fun deleteSavedRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    class Factory(
        private val recipeRepository: RecipeRepository,
        private val ingredientRepository: IngredientRepository,
        private val historyRepository: HistoryRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(recipeRepository, ingredientRepository, historyRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
