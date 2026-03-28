package com.example.greenloop.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.api.OpenRouterManager
import com.example.greenloop.data.model.GeneratedRecipe
import com.example.greenloop.data.model.GeneratedRecipeList
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.IngredientRepository
import com.example.greenloop.data.repository.RecipeRepository
import com.example.greenloop.data.repository.UserRepository
import com.squareup.moshi.Moshi
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

    private val _selectedIngredients = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIngredients: StateFlow<Set<Int>> = _selectedIngredients.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedRecipes = MutableStateFlow<List<GeneratedRecipe>>(emptyList())
    val generatedRecipes: StateFlow<List<GeneratedRecipe>> = _generatedRecipes.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listAdapter = moshi.adapter(GeneratedRecipeList::class.java)

    fun toggleIngredientSelection(id: Int) {
        _selectedIngredients.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun generateAiRecipes() {
        val selectedIds = _selectedIngredients.value
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val selectedNames = inventory.value
                    .filter { it.id in selectedIds }
                    .joinToString(", ") { it.name }

                val systemPrompt = """
                    You are a creative Zero-Waste Chef. Your task is to generate exactly THREE recipes using the provided ingredients to prevent them from going to waste.
                    
                    Return ONLY a JSON object with this structure:
                    {
                      "recipes": [
                        {
                          "recipeName": "string",
                          "prepTimeMinutes": integer,
                          "difficulty": "Easy/Medium/Hard",
                          "steps": ["step 1", "step 2", ...]
                        }
                      ]
                    }
                """.trimIndent()

                val prompt = "Generate 3 recipes using these ingredients: $selectedNames"

                val responseText = OpenRouterManager.analyzeText(prompt, systemPrompt) ?: ""
                
                val jsonString = responseText.substringAfter("```json").substringBeforeLast("```").trim()
                val finalJson = if (jsonString.isEmpty()) responseText.trim() else jsonString
                
                val result = listAdapter.fromJson(finalJson)
                _generatedRecipes.value = result?.recipes ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun completeRecipe(recipe: GeneratedRecipe) {
        val selectedIds = _selectedIngredients.value

        viewModelScope.launch {
            // Save to history
            val history = UpcycleHistory(
                recipeId = -1,
                recipeTitle = recipe.recipeName,
                co2Saved = selectedIds.size * 0.5 
            )
            historyRepository.insertHistory(history)

            // Remove used ingredients from inventory
            inventory.value.filter { it.id in selectedIds }.forEach {
                ingredientRepository.deleteIngredient(it)
            }

            // Reset state
            _generatedRecipes.value = emptyList()
            _selectedIngredients.value = emptySet()
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
