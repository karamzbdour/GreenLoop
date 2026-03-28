package com.example.greenloop.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.data.model.Recipe
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.RecipeRepository
import com.example.greenloop.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val historyRepository: HistoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val recipes: StateFlow<List<Recipe>> = combine(
        recipeRepository.allRecipes,
        _searchQuery,
        userRepository.allergies
    ) { allRecipes, query, allergies ->
        allRecipes.filter { recipe ->
            val matchesQuery = recipe.title.contains(query, ignoreCase = true) ||
                               recipe.biowasteType.contains(query, ignoreCase = true)
            val isSafe = allergies.split(",").all { allergy ->
                allergy.isBlank() || !recipe.description.contains(allergy.trim(), ignoreCase = true)
            }
            matchesQuery && isSafe
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun completeUpcycle(recipe: Recipe) {
        viewModelScope.launch {
            val history = UpcycleHistory(
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                co2Saved = recipe.co2Saved
            )
            historyRepository.insertHistory(history)
        }
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
        }
    }

    class Factory(
        private val recipeRepository: RecipeRepository,
        private val historyRepository: HistoryRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(recipeRepository, historyRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
